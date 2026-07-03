package com.example.onlinefooddeliverysystem.data;

import java.util.ArrayList;

public class AiRecommendationResult {
    private final String shopName;
    private final ArrayList<String> foodNames;
    private final String reason;
    private final String rawText;

    public AiRecommendationResult(String shopName, ArrayList<String> foodNames, String reason, String rawText) {
        this.shopName = shopName;
        this.foodNames = foodNames;
        this.reason = reason;
        this.rawText = rawText;
    }

    public String getShopName() {
        return shopName;
    }

    public ArrayList<String> getFoodNames() {
        return foodNames;
    }

    public String getReason() {
        return reason;
    }

    public String getRawText() {
        return rawText;
    }
}
