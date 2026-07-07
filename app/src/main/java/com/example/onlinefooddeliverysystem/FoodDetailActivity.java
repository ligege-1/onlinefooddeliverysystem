package com.example.onlinefooddeliverysystem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onlinefooddeliverysystem.data.CartManager;
import com.example.onlinefooddeliverysystem.data.DataRepository;
import com.example.onlinefooddeliverysystem.data.UserManager;
import com.example.onlinefooddeliverysystem.model.FoodBean;
import com.example.onlinefooddeliverysystem.model.ShopBean;
import com.example.onlinefooddeliverysystem.util.FormatUtils;

public class FoodDetailActivity extends AppCompatActivity {
    private FoodBean food;
    private ShopBean shop;
    private TextView tvCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);
        food = (FoodBean) getIntent().getSerializableExtra("food");
        shop = (ShopBean) getIntent().getSerializableExtra("shop");
        if (food == null) {
            finish();
            return;
        }
        if (shop == null) {
            shop = DataRepository.findShopById(food.getShopId());
        }
        initView();
        refreshCount();
    }

    private void initView() {
        TextView tvBack = findViewById(R.id.tv_back);
        ImageView ivFood = findViewById(R.id.iv_food_big);
        TextView tvName = findViewById(R.id.tv_food_name);
        TextView tvTaste = findViewById(R.id.tv_food_taste);
        TextView tvHighlight = findViewById(R.id.tv_food_highlight);
        TextView tvTags = findViewById(R.id.tv_food_tags);
        TextView tvIntro = findViewById(R.id.tv_food_desc);
        TextView tvPairing = findViewById(R.id.tv_food_pairing);
        TextView tvPrice = findViewById(R.id.tv_food_price);
        TextView tvAdd = findViewById(R.id.tv_add_cart);
        tvCount = findViewById(R.id.tv_cart_count);

        tvBack.setOnClickListener(v -> finish());
        ivFood.setImageResource(food.getImageResId());
        tvName.setText(food.getName());
        tvTaste.setText(food.getTaste() + " · 月售 " + food.getSales());
        tvHighlight.setText(DataRepository.buildFoodHighlight(food));
        tvTags.setText(DataRepository.buildFoodTags(food));
        tvIntro.setText(DataRepository.buildFoodDetail(food));
        tvPairing.setText(DataRepository.buildFoodPairing(food));
        tvPrice.setText(FormatUtils.price(food.getPrice()));
        tvAdd.setOnClickListener(v -> {
            if (!UserManager.getInstance().isLoggedIn(this)) {
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }
            if (shop == null || !CartManager.getInstance().addFood(shop, food)) {
                Toast.makeText(this, "购物车里已有其他店铺商品，请先结算或清空购物车", Toast.LENGTH_SHORT).show();
                return;
            }
            refreshCount();
        });
    }

    private void refreshCount() {
        tvCount.setText("购物车中已加入 " + CartManager.getInstance().getCount(food) + " 份");
    }
}
