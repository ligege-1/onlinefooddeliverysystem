package com.example.onlinefooddeliverysystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onlinefooddeliverysystem.data.CartManager;
import com.example.onlinefooddeliverysystem.db.OrderDbHelper;
import com.example.onlinefooddeliverysystem.model.CartItem;
import com.example.onlinefooddeliverysystem.model.OrderBean;
import com.example.onlinefooddeliverysystem.model.ShopBean;
import com.example.onlinefooddeliverysystem.util.FormatUtils;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {
    private ShopBean shop;
    private final ArrayList<CartItem> cartItems = new ArrayList<>();
    private LinearLayout llCartItems;
    private TextView tvTotal;
    private TextView tvGoodsTotal;
    private TextView tvDeliveryFee;
    private EditText etAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        shop = (ShopBean) getIntent().getSerializableExtra("shop");
        if (shop == null) {
            finish();
            return;
        }
        initView();
        refreshCart();
    }

    private void initView() {
        TextView tvBack = findViewById(R.id.tv_back);
        TextView tvSubmit = findViewById(R.id.tv_submit_order);
        ImageView ivShop = findViewById(R.id.iv_shop);
        TextView tvShopName = findViewById(R.id.tv_shop_name);
        TextView tvShopInfo = findViewById(R.id.tv_shop_info);
        TextView tvDeliveryTime = findViewById(R.id.tv_delivery_time);
        llCartItems = findViewById(R.id.ll_cart_items);
        tvTotal = findViewById(R.id.tv_total);
        tvGoodsTotal = findViewById(R.id.tv_goods_total);
        tvDeliveryFee = findViewById(R.id.tv_delivery_fee);
        etAddress = findViewById(R.id.et_address);

        tvBack.setOnClickListener(v -> finish());
        ivShop.setImageResource(shop.getImageResId());
        tvShopName.setText(shop.getName());
        tvShopInfo.setText(shop.getCategory() + " · " + shop.getDeliveryTime() + " · 评分 " + shop.getScore());
        tvDeliveryTime.setText("现在下单，预计 " + shop.getDeliveryTime() + " 送达");
        tvDeliveryFee.setText(FormatUtils.price(shop.getDeliveryFee()));
        tvSubmit.setOnClickListener(v -> submitOrder());
    }

    private void refreshCart() {
        cartItems.clear();
        cartItems.addAll(CartManager.getInstance().getItems());
        renderCartItems();

        int goodsTotal = CartManager.getInstance().getTotalPrice();
        int total = goodsTotal + shop.getDeliveryFee();
        tvGoodsTotal.setText(FormatUtils.price(goodsTotal));
        tvTotal.setText("合计 " + FormatUtils.price(total));
    }

    private void renderCartItems() {
        llCartItems.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (CartItem item : cartItems) {
            View itemView = inflater.inflate(R.layout.item_cart, llCartItems, false);
            ImageView ivFood = itemView.findViewById(R.id.iv_cart_food);
            TextView tvName = itemView.findViewById(R.id.tv_cart_name);
            TextView tvDesc = itemView.findViewById(R.id.tv_cart_desc);
            TextView tvPrice = itemView.findViewById(R.id.tv_cart_price);
            TextView tvCount = itemView.findViewById(R.id.tv_cart_count);
            ImageButton btnMinus = itemView.findViewById(R.id.btn_cart_minus);
            ImageButton btnAdd = itemView.findViewById(R.id.btn_cart_add);

            ivFood.setImageResource(item.getFood().getImageResId());
            tvName.setText(item.getFood().getName());
            tvDesc.setText(item.getFood().getTaste() + " · " + item.getFood().getDescription());
            tvPrice.setText(FormatUtils.price(item.getTotalPrice()));
            tvCount.setText("x" + item.getCount());
            btnMinus.setOnClickListener(v -> {
                CartManager.getInstance().decreaseFood(item.getFood());
                refreshCart();
            });
            btnAdd.setOnClickListener(v -> {
                CartManager.getInstance().addFood(item.getFood());
                refreshCart();
            });

            llCartItems.addView(itemView);
        }
    }

    private void submitOrder() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "购物车为空", Toast.LENGTH_SHORT).show();
            return;
        }
        String address = etAddress.getText().toString().trim();
        if (address.isEmpty()) {
            Toast.makeText(this, "请填写收货地址", Toast.LENGTH_SHORT).show();
            return;
        }
        int total = CartManager.getInstance().getTotalPrice() + shop.getDeliveryFee();
        OrderBean order = new OrderBean(0, shop.getName(), address, total, shop.getDeliveryFee(),
                "待支付", FormatUtils.now(), CartManager.getInstance().getItems());
        new OrderDbHelper(this).saveOrder(order);
        CartManager.getInstance().clear();
        Intent intent = new Intent(this, PayActivity.class);
        intent.putExtra("order", order);
        startActivity(intent);
        finish();
    }
}
