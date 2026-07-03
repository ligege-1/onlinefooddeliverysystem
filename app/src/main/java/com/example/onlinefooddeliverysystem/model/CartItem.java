package com.example.onlinefooddeliverysystem.model;

import java.io.Serializable;

public class CartItem implements Serializable {
    private final FoodBean food;
    private int count;

    public CartItem(FoodBean food, int count) {
        this.food = food;
        this.count = count;
    }

    public FoodBean getFood() {
        return food;
    }

    public int getCount() {
        return count;
    }

    public void increase() {
        count++;
    }

    public void decrease() {
        if (count > 0) {
            count--;
        }
    }

    public int getTotalPrice() {
        return food.getPrice() * count;
    }
}
