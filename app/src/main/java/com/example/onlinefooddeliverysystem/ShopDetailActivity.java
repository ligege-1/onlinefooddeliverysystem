package com.example.onlinefooddeliverysystem;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlinefooddeliverysystem.adapter.FoodAdapter;
import com.example.onlinefooddeliverysystem.data.AddressManager;
import com.example.onlinefooddeliverysystem.data.CartManager;
import com.example.onlinefooddeliverysystem.model.FoodBean;
import com.example.onlinefooddeliverysystem.model.ShopBean;
import com.example.onlinefooddeliverysystem.util.DeliveryUtils;
import com.example.onlinefooddeliverysystem.util.FormatUtils;

public class ShopDetailActivity extends AppCompatActivity {
    private ShopBean shop;
    private FoodAdapter adapter;
    private TextView tvShopInfo;
    private TextView tvCartInfo;
    private TextView tvCheckout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);
        shop = (ShopBean) getIntent().getSerializableExtra("shop");
        if (shop == null) {
            finish();
            return;
        }
        initView();
        refreshHeader();
        refreshCartBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        refreshHeader();
        refreshCartBar();
    }

    private void initView() {
        TextView tvBack = findViewById(R.id.tv_back);
        TextView tvShopName = findViewById(R.id.tv_shop_name);
        TextView tvNotice = findViewById(R.id.tv_notice);
        TextView tvClear = findViewById(R.id.tv_clear_cart);
        ImageView ivShop = findViewById(R.id.iv_shop_header);
        RecyclerView rvFood = findViewById(R.id.rv_food);
        tvShopInfo = findViewById(R.id.tv_shop_info);
        tvCartInfo = findViewById(R.id.tv_cart_info);
        tvCheckout = findViewById(R.id.tv_checkout);

        tvBack.setOnClickListener(v -> finish());
        ivShop.setImageResource(shop.getImageResId());
        tvShopName.setText(shop.getName());
        tvNotice.setText(shop.getNotice() + "  起送" + FormatUtils.price(shop.getMinPrice()) + " | 配送" + FormatUtils.price(shop.getDeliveryFee()));

        rvFood.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FoodAdapter(shop, shop.getFoods(), new FoodAdapter.OnFoodActionListener() {
            @Override
            public void onFoodClick(FoodBean food) {
                Intent intent = new Intent(ShopDetailActivity.this, FoodDetailActivity.class);
                intent.putExtra("food", food);
                intent.putExtra("shop", shop);
                startActivity(intent);
            }

            @Override
            public void onCartChanged() {
                refreshCartBar();
            }

            @Override
            public void onCrossShopBlocked() {
                Toast.makeText(ShopDetailActivity.this, "购物车里已有其他店铺商品，请先结算或清空购物车", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRequireLogin() {
                startActivity(new Intent(ShopDetailActivity.this, LoginActivity.class));
            }
        });
        rvFood.setAdapter(adapter);

        tvClear.setOnClickListener(v -> showClearDialog());
        tvCheckout.setOnClickListener(v -> {
            if (CartManager.getInstance().getTotalCount() == 0) {
                Toast.makeText(this, "购物车还是空的", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, CartActivity.class);
            intent.putExtra("shop", shop);
            startActivity(intent);
        });
    }

    private void refreshHeader() {
        String address = AddressManager.getInstance().getCurrentAddress(this);
        tvShopInfo.setText(shop.getCategory() + " | 送至 " + AddressManager.getInstance().getShortAddress(this)
                + " | " + DeliveryUtils.buildListDeliveryText(shop, address));
    }

    private void refreshCartBar() {
        int count = CartManager.getInstance().getTotalCount();
        int total = CartManager.getInstance().getTotalPrice();
        tvCartInfo.setText(count == 0 ? "购物车为空" : count + " 件商品 | " + FormatUtils.price(total));
        tvCheckout.setText(total >= shop.getMinPrice() ? "去结算" : "还差" + FormatUtils.price(shop.getMinPrice() - total) + "起送");
    }

    private void showClearDialog() {
        new AlertDialog.Builder(this)
                .setTitle("确认清空购物车？")
                .setNegativeButton("取消", null)
                .setPositiveButton("清空", (dialog, which) -> {
                    CartManager.getInstance().clear();
                    adapter.notifyDataSetChanged();
                    refreshCartBar();
                })
                .show();
    }
}
