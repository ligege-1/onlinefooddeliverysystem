package com.example.onlinefooddeliverysystem;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onlinefooddeliverysystem.data.CartManager;
import com.example.onlinefooddeliverysystem.model.FoodBean;
import com.example.onlinefooddeliverysystem.util.FormatUtils;

public class FoodDetailActivity extends AppCompatActivity {
    private FoodBean food;
    private TextView tvCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);
        food = (FoodBean) getIntent().getSerializableExtra("food");
        if (food == null) {
            finish();
            return;
        }
        initView();
        refreshCount();
    }

    private void initView() {
        TextView tvBack = findViewById(R.id.tv_back);
        ImageView ivFood = findViewById(R.id.iv_food_big);
        TextView tvName = findViewById(R.id.tv_food_name);
        TextView tvTaste = findViewById(R.id.tv_food_taste);
        TextView tvDesc = findViewById(R.id.tv_food_desc);
        TextView tvPrice = findViewById(R.id.tv_food_price);
        TextView tvAdd = findViewById(R.id.tv_add_cart);
        tvCount = findViewById(R.id.tv_cart_count);

        tvBack.setOnClickListener(v -> finish());
        ivFood.setImageResource(food.getImageResId());
        tvName.setText(food.getName());
        tvTaste.setText(food.getTaste() + " · 月售 " + food.getSales());
        tvDesc.setText(food.getDescription());
        tvPrice.setText(FormatUtils.price(food.getPrice()));
        tvAdd.setOnClickListener(v -> {
            CartManager.getInstance().addFood(food);
            refreshCount();
        });
    }

    private void refreshCount() {
        tvCount.setText("购物车中：" + CartManager.getInstance().getCount(food) + " 份");
    }
}
