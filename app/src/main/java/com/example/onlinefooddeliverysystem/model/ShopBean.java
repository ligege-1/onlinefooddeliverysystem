package com.example.onlinefooddeliverysystem.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ShopBean implements Serializable {
    private final int id;
    private final String name;
    private final String category;
    private final String notice;
    private final String deliveryTime;
    private final int deliveryFee;
    private final int minPrice;
    private final float score;
    private final int imageResId;
    private final String reason;
    private final ArrayList<FoodBean> foods;

    public ShopBean(int id, String name, String category, String notice, String deliveryTime,
                    int deliveryFee, int minPrice, float score, int imageResId, String reason,
                    List<FoodBean> foods) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.notice = notice;
        this.deliveryTime = deliveryTime;
        this.deliveryFee = deliveryFee;
        this.minPrice = minPrice;
        this.score = score;
        this.imageResId = imageResId;
        this.reason = reason;
        this.foods = new ArrayList<>(foods);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getNotice() {
        return notice;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public int getDeliveryFee() {
        return deliveryFee;
    }

    public int getMinPrice() {
        return minPrice;
    }

    public float getScore() {
        return score;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getReason() {
        return reason;
    }

    public ArrayList<FoodBean> getFoods() {
        return foods;
    }
}
