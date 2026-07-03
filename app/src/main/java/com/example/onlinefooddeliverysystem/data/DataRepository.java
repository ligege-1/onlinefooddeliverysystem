package com.example.onlinefooddeliverysystem.data;

import com.example.onlinefooddeliverysystem.R;
import com.example.onlinefooddeliverysystem.model.FoodBean;
import com.example.onlinefooddeliverysystem.model.ShopBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class DataRepository {
    private static final ArrayList<ShopBean> SHOPS = new ArrayList<>();

    static {
        ArrayList<FoodBean> spicyFoods = new ArrayList<>(Arrays.asList(
                new FoodBean(101, 1, "招牌麻辣香锅", "香辣", "牛肉丸、土豆、藕片和青菜组合，适合下饭。", 26, 238, R.drawable.food_spicy_hotpot),
                new FoodBean(102, 1, "藤椒鸡腿饭", "微辣", "鸡腿肉鲜嫩，配藤椒酱汁和时蔬。", 22, 196, R.drawable.food_chicken_leg_rice),
                new FoodBean(103, 1, "冰柠檬茶", "清爽", "解辣搭配，少糖也好喝。", 8, 320, R.drawable.food_iced_lemon_tea)
        ));
        SHOPS.add(new ShopBean(1, "川味小馆", "川湘菜", "满35减5，学生证可送饮品。", "28分钟", 4, 20, 4.8f,
                R.drawable.shop_spicy, "适合想吃辣、预算中等的同学", spicyFoods));

        ArrayList<FoodBean> lightFoods = new ArrayList<>(Arrays.asList(
                new FoodBean(201, 2, "番茄牛腩饭", "酸甜", "番茄汤底浓郁，牛腩炖得软烂。", 24, 180, R.drawable.food_tomato_beef_rice),
                new FoodBean(202, 2, "玉米排骨汤", "清淡", "热汤搭配米饭，适合晚自习前吃。", 18, 132, R.drawable.food_corn_ribs_soup),
                new FoodBean(203, 2, "蒸蛋套餐", "清淡", "鸡蛋羹、青菜和米饭，口味温和。", 16, 109, R.drawable.food_steamed_egg_rice)
        ));
        SHOPS.add(new ShopBean(2, "暖心简餐", "简餐套餐", "工作日午餐高峰提前下单更快。", "20分钟", 3, 15, 4.6f,
                R.drawable.shop_light, "适合清淡、快捷的一人餐", lightFoods));

        ArrayList<FoodBean> noodleFoods = new ArrayList<>(Arrays.asList(
                new FoodBean(301, 3, "红烧牛肉面", "咸香", "大块牛肉和手工面，汤底浓。", 21, 260, R.drawable.food_beef_noodle),
                new FoodBean(302, 3, "酸辣粉", "酸辣", "红薯粉劲道，酸辣开胃。", 15, 210, R.drawable.food_sour_spicy_noodles),
                new FoodBean(303, 3, "煎蛋加菜套餐", "家常", "面食搭配煎蛋和青菜。", 9, 88, R.drawable.food_fried_egg_rice)
        ));
        SHOPS.add(new ShopBean(3, "一碗热面", "粉面馆", "夜宵时段21:30前可下单。", "18分钟", 2, 12, 4.7f,
                R.drawable.shop_noodle, "适合预算较低、想吃热乎主食", noodleFoods));

        ArrayList<FoodBean> westernFoods = new ArrayList<>(Arrays.asList(
                new FoodBean(401, 4, "黑椒鸡排饭", "黑椒", "鸡排外酥里嫩，搭配黑椒汁。", 25, 156, R.drawable.food_chicken_cutlet_rice),
                new FoodBean(402, 4, "奥尔良鸡肉卷", "微辣", "适合边走边吃的轻餐。", 18, 143, R.drawable.food_orleans_chicken_wrap),
                new FoodBean(403, 4, "水果酸奶杯", "甜品", "饭后甜品，清爽不腻。", 12, 117, R.drawable.food_fruit_yogurt_cup)
        ));
        SHOPS.add(new ShopBean(4, "元气轻食站", "轻食西餐", "高蛋白套餐热卖中。", "25分钟", 5, 18, 4.5f,
                R.drawable.shop_western, "适合健身、轻食和不想吃太油", westernFoods));
    }

    public static ArrayList<ShopBean> getShops() {
        return new ArrayList<>(SHOPS);
    }

    public static ArrayList<ShopBean> searchShops(String keyword) {
        String key = safe(keyword);
        if (key.isEmpty()) {
            return getShops();
        }
        ArrayList<ShopBean> result = new ArrayList<>();
        for (ShopBean shop : SHOPS) {
            if (matches(shop, key)) {
                result.add(shop);
            }
        }
        return result;
    }

    public static ShopBean findShopByName(String shopName) {
        String key = safe(shopName);
        for (ShopBean shop : SHOPS) {
            if (safe(shop.getName()).contains(key) || key.contains(safe(shop.getName()))) {
                return shop;
            }
        }
        return null;
    }

    public static FoodBean findFoodByName(String foodName) {
        String key = safe(foodName);
        for (ShopBean shop : SHOPS) {
            for (FoodBean food : shop.getFoods()) {
                String name = safe(food.getName());
                if (name.contains(key) || key.contains(name)) {
                    return food;
                }
            }
        }
        return null;
    }

    public static String buildMenuForPrompt() {
        StringBuilder builder = new StringBuilder();
        for (ShopBean shop : SHOPS) {
            builder.append("店铺：").append(shop.getName())
                    .append("；分类：").append(shop.getCategory())
                    .append("；评分：").append(shop.getScore())
                    .append("；配送费：").append(shop.getDeliveryFee())
                    .append("元；起送：").append(shop.getMinPrice()).append("元。\n");
            for (FoodBean food : shop.getFoods()) {
                builder.append("菜品：")
                        .append(food.getName())
                        .append("；口味：").append(food.getTaste())
                        .append("；价格：").append(food.getPrice())
                        .append("元；销量：").append(food.getSales())
                        .append("；说明：").append(food.getDescription())
                        .append("\n");
            }
        }
        return builder.toString();
    }

    public static ShopBean firstShop() {
        return SHOPS.get(0);
    }

    public static ArrayList<String> allShopNames() {
        ArrayList<String> names = new ArrayList<>();
        for (ShopBean shop : SHOPS) {
            names.add(shop.getName());
        }
        return names;
    }

    public static ArrayList<String> allFoodNames() {
        ArrayList<String> names = new ArrayList<>();
        for (ShopBean shop : SHOPS) {
            for (FoodBean food : shop.getFoods()) {
                names.add(food.getName());
            }
        }
        return names;
    }

    private static boolean matches(ShopBean shop, String key) {
        if (safe(shop.getName()).contains(key) || safe(shop.getCategory()).contains(key) || safe(shop.getReason()).contains(key)) {
            return true;
        }
        for (FoodBean food : shop.getFoods()) {
            if (safe(food.getName()).contains(key) || safe(food.getTaste()).contains(key) || safe(food.getDescription()).contains(key)) {
                return true;
            }
        }
        return false;
    }

    private static String safe(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
    }
}
