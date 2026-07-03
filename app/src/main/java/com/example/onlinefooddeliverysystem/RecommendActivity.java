package com.example.onlinefooddeliverysystem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.onlinefooddeliverysystem.data.AiRecommendationResult;
import com.example.onlinefooddeliverysystem.data.CartManager;
import com.example.onlinefooddeliverysystem.data.DataRepository;
import com.example.onlinefooddeliverysystem.data.SiliconFlowService;
import com.example.onlinefooddeliverysystem.model.FoodBean;
import com.example.onlinefooddeliverysystem.model.ShopBean;
import com.example.onlinefooddeliverysystem.util.FormatUtils;

import java.util.ArrayList;

public class RecommendActivity extends AppCompatActivity {
    private final SiliconFlowService aiService = new SiliconFlowService();
    private final ArrayList<FoodBean> recommendedFoods = new ArrayList<>();
    private EditText etPrompt;
    private TextView tvResult;
    private TextView tvRun;
    private ShopBean recommendedShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);
        initView();
    }

    private void initView() {
        TextView tvBack = findViewById(R.id.tv_back);
        TextView tvAdd = findViewById(R.id.tv_add_recommend);
        etPrompt = findViewById(R.id.et_prompt);
        tvResult = findViewById(R.id.tv_result);
        tvRun = findViewById(R.id.tv_run_recommend);

        tvBack.setOnClickListener(v -> finish());
        tvRun.setOnClickListener(v -> runRecommend());
        tvAdd.setOnClickListener(v -> addRecommendationToCart());
        tvResult.setText("输入你的用餐需求，例如：两个人，想吃辣，预算50。AI 会从当前菜单中推荐店铺和菜品。");
    }

    private void runRecommend() {
        String prompt = etPrompt.getText().toString().trim();
        if (prompt.isEmpty()) {
            Toast.makeText(this, "请先输入点餐需求", Toast.LENGTH_SHORT).show();
            return;
        }

        tvRun.setEnabled(false);
        tvRun.setText("AI 正在推荐...");
        tvResult.setText("AI 正在思考，请稍等...");

        aiService.recommend(prompt, new SiliconFlowService.Callback() {
            @Override
            public void onSuccess(AiRecommendationResult result) {
                tvRun.setEnabled(true);
                tvRun.setText("生成推荐");
                showResult(result);
            }

            @Override
            public void onError(String message) {
                tvRun.setEnabled(true);
                tvRun.setText("生成推荐");
                tvResult.setText(message);
            }
        });
    }

    private void showResult(AiRecommendationResult result) {
        recommendedFoods.clear();
        recommendedShop = DataRepository.findShopByName(result.getShopName());
        if (recommendedShop == null) {
            recommendedShop = DataRepository.firstShop();
        }

        int total = 0;
        StringBuilder builder = new StringBuilder();
        builder.append("推荐店铺：").append(recommendedShop.getName()).append("\n\n");
        builder.append("推荐理由：").append(buildDisplayReason(result)).append("\n\n");
        builder.append("推荐菜品：\n");
        for (String foodName : result.getFoodNames()) {
            FoodBean food = DataRepository.findFoodByName(foodName);
            if (food != null) {
                recommendedFoods.add(food);
                total += food.getPrice();
                builder.append("- ").append(food.getName()).append("  ").append(FormatUtils.price(food.getPrice())).append("\n");
            } else {
                builder.append("- ").append(foodName).append("（菜单中未匹配）\n");
            }
        }
        builder.append("\n预计合计：").append(FormatUtils.price(total));
        tvResult.setText(builder.toString());
    }

    private String buildDisplayReason(AiRecommendationResult result) {
        String reason = result.getReason();
        if (reason == null || reason.trim().isEmpty()) {
            reason = "AI 已根据你的需求筛选出更合适的店铺和菜品。";
        }
        StringBuilder builder = new StringBuilder(reason.trim());
        if (!recommendedFoods.isEmpty()) {
            builder.append("\n这组推荐包含 ");
            for (int i = 0; i < recommendedFoods.size(); i++) {
                if (i > 0) {
                    builder.append("、");
                }
                builder.append(recommendedFoods.get(i).getName());
            }
            builder.append("，可直接加入购物车。");
        }
        return builder.toString();
    }

    private void addRecommendationToCart() {
        if (recommendedFoods.isEmpty()) {
            Toast.makeText(this, "请先生成可加入购物车的推荐", Toast.LENGTH_SHORT).show();
            return;
        }
        for (FoodBean food : recommendedFoods) {
            CartManager.getInstance().addFood(food);
        }
        Toast.makeText(this, "已加入购物车", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, ShopDetailActivity.class);
        intent.putExtra("shop", recommendedShop);
        startActivity(intent);
    }
}
