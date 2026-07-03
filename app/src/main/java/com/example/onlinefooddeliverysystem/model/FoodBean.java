package com.example.onlinefooddeliverysystem.model;

import java.io.Serializable;

public class FoodBean implements Serializable {
    private final int id;
    private final int shopId;
    private final String name;
    private final String taste;
    private final String description;
    private final int price;
    private final int sales;
    private final int imageResId;

    public FoodBean(int id, int shopId, String name, String taste, String description, int price, int sales, int imageResId) {
        this.id = id;
        this.shopId = shopId;
        this.name = name;
        this.taste = taste;
        this.description = description;
        this.price = price;
        this.sales = sales;
        this.imageResId = imageResId;
    }

    public int getId() {
        return id;
    }

    public int getShopId() {
        return shopId;
    }

    public String getName() {
        return name;
    }

    public String getTaste() {
        return taste;
    }

    public String getDescription() {
        return description;
    }

    public int getPrice() {
        return price;
    }

    public int getSales() {
        return sales;
    }

    public int getImageResId() {
        return imageResId;
    }
}
