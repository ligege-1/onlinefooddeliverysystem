package com.example.onlinefooddeliverysystem.data;

import com.example.onlinefooddeliverysystem.model.CartItem;
import com.example.onlinefooddeliverysystem.model.FoodBean;

import java.util.ArrayList;

public class CartManager {
    private static final CartManager INSTANCE = new CartManager();
    private final ArrayList<CartItem> items = new ArrayList<>();

    private CartManager() {
    }

    public static CartManager getInstance() {
        return INSTANCE;
    }

    public void addFood(FoodBean food) {
        for (CartItem item : items) {
            if (item.getFood().getId() == food.getId()) {
                item.increase();
                return;
            }
        }
        items.add(new CartItem(food, 1));
    }

    public void decreaseFood(FoodBean food) {
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            if (item.getFood().getId() == food.getId()) {
                item.decrease();
                if (item.getCount() == 0) {
                    items.remove(i);
                }
                return;
            }
        }
    }

    public int getCount(FoodBean food) {
        for (CartItem item : items) {
            if (item.getFood().getId() == food.getId()) {
                return item.getCount();
            }
        }
        return 0;
    }

    public ArrayList<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public int getTotalPrice() {
        int total = 0;
        for (CartItem item : items) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public int getTotalCount() {
        int count = 0;
        for (CartItem item : items) {
            count += item.getCount();
        }
        return count;
    }

    public void clear() {
        items.clear();
    }
}
