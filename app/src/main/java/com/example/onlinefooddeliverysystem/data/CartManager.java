package com.example.onlinefooddeliverysystem.data;

import com.example.onlinefooddeliverysystem.model.CartItem;
import com.example.onlinefooddeliverysystem.model.FoodBean;
import com.example.onlinefooddeliverysystem.model.ShopBean;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public class CartManager {
    private static final CartManager INSTANCE = new CartManager();

    private final ArrayList<CartItem> items = new ArrayList<>();

    private CartManager() {
    }

    public static CartManager getInstance() {
        return INSTANCE;
    }

    public boolean canUseShop(ShopBean shop) {
        return shop != null;
    }

    public boolean addFood(ShopBean shop, FoodBean food) {
        if (shop == null || food == null) {
            return false;
        }
        for (CartItem item : items) {
            if (item.getFood().getId() == food.getId()) {
                item.increase();
                return true;
            }
        }
        items.add(new CartItem(food, 1));
        return true;
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

    public boolean hasItems() {
        return !items.isEmpty();
    }

    public ShopBean getCurrentShop() {
        if (items.isEmpty()) {
            return null;
        }
        return DataRepository.findShopById(items.get(0).getFood().getShopId());
    }

    public ArrayList<ShopBean> getShopsInCart() {
        LinkedHashSet<Integer> shopIds = new LinkedHashSet<>();
        ArrayList<ShopBean> shops = new ArrayList<>();
        for (CartItem item : items) {
            shopIds.add(item.getFood().getShopId());
        }
        for (Integer shopId : shopIds) {
            ShopBean shop = DataRepository.findShopById(shopId);
            if (shop != null) {
                shops.add(shop);
            }
        }
        return shops;
    }

    public int getDeliveryFeeTotal() {
        int total = 0;
        for (ShopBean shop : getShopsInCart()) {
            total += shop.getDeliveryFee();
        }
        return total;
    }

    public String getShopSummary() {
        ArrayList<ShopBean> shops = getShopsInCart();
        if (shops.isEmpty()) {
            return "";
        }
        if (shops.size() == 1) {
            return shops.get(0).getName();
        }
        return "多个商家";
    }

    public void clear() {
        items.clear();
    }
}
