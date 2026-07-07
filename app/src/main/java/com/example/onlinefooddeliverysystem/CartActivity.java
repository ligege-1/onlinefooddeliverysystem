package com.example.onlinefooddeliverysystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onlinefooddeliverysystem.data.AddressManager;
import com.example.onlinefooddeliverysystem.data.CartManager;
import com.example.onlinefooddeliverysystem.db.OrderDbHelper;
import com.example.onlinefooddeliverysystem.model.CartItem;
import com.example.onlinefooddeliverysystem.model.OrderBean;
import com.example.onlinefooddeliverysystem.model.ShopBean;
import com.example.onlinefooddeliverysystem.util.DeliveryUtils;
import com.example.onlinefooddeliverysystem.util.FormatUtils;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {
    private final ArrayList<CartItem> cartItems = new ArrayList<>();
    private ImageView ivShop;
    private TextView tvShopName;
    private TextView tvShopInfo;
    private LinearLayout llCartItems;
    private TextView tvAddress;
    private TextView tvDeliveryTime;
    private TextView tvTotal;
    private TextView tvGoodsTotal;
    private TextView tvDeliveryFee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        if (!CartManager.getInstance().hasItems()) {
            finish();
            return;
        }
        initView();
        refreshCart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCart();
    }

    private void initView() {
        TextView tvBack = findViewById(R.id.tv_back);
        TextView tvSubmit = findViewById(R.id.tv_submit_order);
        TextView tvChangeAddress = findViewById(R.id.tv_change_address);
        ivShop = findViewById(R.id.iv_shop);
        tvShopName = findViewById(R.id.tv_shop_name);
        tvShopInfo = findViewById(R.id.tv_shop_info);
        llCartItems = findViewById(R.id.ll_cart_items);
        tvAddress = findViewById(R.id.tv_address);
        tvDeliveryTime = findViewById(R.id.tv_delivery_time);
        tvTotal = findViewById(R.id.tv_total);
        tvGoodsTotal = findViewById(R.id.tv_goods_total);
        tvDeliveryFee = findViewById(R.id.tv_delivery_fee);

        tvBack.setOnClickListener(v -> finish());
        tvChangeAddress.setOnClickListener(v -> startActivity(new Intent(this, AddressManageActivity.class)));
        tvSubmit.setOnClickListener(v -> submitOrder());
    }

    private void refreshCart() {
        cartItems.clear();
        cartItems.addAll(CartManager.getInstance().getItems());
        if (cartItems.isEmpty()) {
            finish();
            return;
        }

        renderCartHeader();
        renderCartItems();

        String address = AddressManager.getInstance().getCurrentAddress(this);
        tvAddress.setText(address);

        ShopBean primaryShop = CartManager.getInstance().getCurrentShop();
        if (primaryShop != null) {
            tvDeliveryTime.setText(DeliveryUtils.buildCheckoutDeliveryText(primaryShop, address));
        } else {
            tvDeliveryTime.setText("配送至 " + address);
        }

        int goodsTotal = CartManager.getInstance().getTotalPrice();
        int deliveryTotal = CartManager.getInstance().getDeliveryFeeTotal();
        int total = goodsTotal + deliveryTotal;
        tvGoodsTotal.setText(FormatUtils.price(goodsTotal));
        tvDeliveryFee.setText(FormatUtils.price(deliveryTotal));
        tvTotal.setText("合计 " + FormatUtils.price(total));
    }

    private void renderCartHeader() {
        ShopBean primaryShop = CartManager.getInstance().getCurrentShop();
        int shopCount = CartManager.getInstance().getShopsInCart().size();
        if (primaryShop != null) {
            ivShop.setImageResource(primaryShop.getImageResId());
        }
        tvShopName.setText(CartManager.getInstance().getShopSummary());
        if (primaryShop == null) {
            tvShopInfo.setText("");
            return;
        }
        if (shopCount <= 1) {
            tvShopInfo.setText(primaryShop.getCategory() + " | 评分 " + primaryShop.getScore());
        } else {
            tvShopInfo.setText("已选 " + shopCount + " 家店铺商品");
        }
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

            ShopBean itemShop = com.example.onlinefooddeliverysystem.data.DataRepository.findShopById(item.getFood().getShopId());
            ivFood.setImageResource(item.getFood().getImageResId());
            tvName.setText(item.getFood().getName());
            String shopName = itemShop == null ? "" : itemShop.getName() + " | ";
            tvDesc.setText(shopName + item.getFood().getTaste() + " | " + item.getFood().getDescription());
            tvPrice.setText(FormatUtils.price(item.getTotalPrice()));
            tvCount.setText("x" + item.getCount());
            btnMinus.setOnClickListener(v -> {
                CartManager.getInstance().decreaseFood(item.getFood());
                refreshCart();
            });
            btnAdd.setOnClickListener(v -> {
                CartManager.getInstance().addFood(itemShop, item.getFood());
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
        String address = AddressManager.getInstance().getCurrentAddress(this);
        int total = CartManager.getInstance().getTotalPrice() + CartManager.getInstance().getDeliveryFeeTotal();
        OrderBean order = new OrderBean(0, CartManager.getInstance().getShopSummary(), address, total,
                CartManager.getInstance().getDeliveryFeeTotal(), "待支付", FormatUtils.now(), CartManager.getInstance().getItems());
        new OrderDbHelper(this).saveOrder(order);
        CartManager.getInstance().clear();
        Intent intent = new Intent(this, PayActivity.class);
        intent.putExtra("order", order);
        startActivity(intent);
        finish();
    }
}
