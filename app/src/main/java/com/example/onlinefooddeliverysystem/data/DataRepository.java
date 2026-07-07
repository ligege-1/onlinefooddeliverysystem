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
                new FoodBean(101, 1, "招牌麻辣香锅", "香辣", "牛肉丸、土豆、藕片和青菜一起翻炒，酱香厚实，特别下饭。", 26, 238, R.drawable.food_spicy_hotpot),
                new FoodBean(102, 1, "藤椒鸡腿饭", "微辣", "整块鸡腿肉鲜嫩入味，藤椒香气明显，配米饭很稳。", 22, 196, R.drawable.food_chicken_leg_rice),
                new FoodBean(103, 1, "冰镇柠檬茶", "清爽", "酸甜平衡，冰感足，适合解辣也适合配炸物。", 8, 320, R.drawable.food_iced_lemon_tea)
        ));
        SHOPS.add(new ShopBean(1, "川味小馆", "川湘菜", "满35减5，学生证可送饮品。", "28分钟", 4, 18, 4.8f,
                R.drawable.shop_spicy, "适合想吃辣、预算中等的同学", spicyFoods));

        ArrayList<FoodBean> lightFoods = new ArrayList<>(Arrays.asList(
                new FoodBean(201, 2, "番茄牛腩饭", "酸甜", "番茄汤底浓郁顺口，牛腩软烂，适合想吃热饭热菜的时候。", 24, 180, R.drawable.food_tomato_beef_rice),
                new FoodBean(202, 2, "玉米排骨汤", "清淡", "清甜玉米和排骨小火慢炖，喝起来舒服不腻。", 18, 132, R.drawable.food_corn_ribs_soup),
                new FoodBean(203, 2, "蒸蛋套餐", "清淡", "蒸蛋细嫩顺滑，搭配青菜和米饭，口味很温和。", 16, 109, R.drawable.food_steamed_egg_rice)
        ));
        SHOPS.add(new ShopBean(2, "暖心简餐", "简餐套餐", "工作日午餐高峰提前下单更快。", "20分钟", 3, 15, 4.6f,
                R.drawable.shop_light, "适合清淡、快捷的一人餐", lightFoods));

        ArrayList<FoodBean> noodleFoods = new ArrayList<>(Arrays.asList(
                new FoodBean(301, 3, "红烧牛肉面", "咸香", "手工面筋道有嚼劲，牛肉块大，汤底浓郁。", 21, 260, R.drawable.food_beef_noodle),
                new FoodBean(302, 3, "酸辣粉", "酸辣", "红薯粉顺滑弹牙，酸辣口更开胃，夜宵也很合适。", 15, 210, R.drawable.food_sour_spicy_noodles),
                new FoodBean(303, 3, "煎蛋加菜套餐", "家常", "经典主食搭配煎蛋和时蔬，简单但饱腹。", 9, 88, R.drawable.food_fried_egg_rice)
        ));
        SHOPS.add(new ShopBean(3, "一碗热面", "粉面馆", "夜宵时段 21:30 前可下单。", "18分钟", 2, 12, 4.7f,
                R.drawable.shop_noodle, "适合预算较低、想吃热乎主食", noodleFoods));

        ArrayList<FoodBean> westernFoods = new ArrayList<>(Arrays.asList(
                new FoodBean(401, 4, "黑椒鸡排饭", "黑椒", "鸡排外酥里嫩，搭配黑椒酱和配菜，整体很有饱足感。", 25, 156, R.drawable.food_chicken_cutlet_rice),
                new FoodBean(402, 4, "奥尔良鸡肉卷", "微辣", "外皮柔软，鸡肉卷香气足，适合赶课路上吃。", 18, 143, R.drawable.food_orleans_chicken_wrap),
                new FoodBean(403, 4, "水果酸奶杯", "甜品", "酸奶顺滑，水果清爽，饭后收尾很舒服。", 12, 117, R.drawable.food_fruit_yogurt_cup)
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

    public static ShopBean findShopById(int shopId) {
        for (ShopBean shop : SHOPS) {
            if (shop.getId() == shopId) {
                return shop;
            }
        }
        return null;
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

    public static String buildFoodHighlight(FoodBean food) {
        switch (food.getId()) {
            case 101:
                return "招牌重口款，酱香和辣香都很足，配米饭特别稳。";
            case 102:
                return "鸡腿肉鲜嫩不柴，藤椒香很提味，属于回购率很高的一款。";
            case 103:
                return "冰爽解辣型饮品，适合和香辣菜搭配。";
            case 201:
                return "番茄酸甜柔和，适合想吃热饭又不想太重口的时候。";
            case 202:
                return "热汤清甜，晚饭或夜自习前来一份都很舒服。";
            case 203:
                return "口味温和、饱腹感好，适合清淡一餐。";
            case 301:
                return "牛肉块大、汤底浓，属于很有满足感的面食。";
            case 302:
                return "酸辣开胃，顺滑不腻，夜宵很受欢迎。";
            case 303:
                return "家常感很强，价格友好，适合日常填饱肚子。";
            case 401:
                return "鸡排厚实，黑椒味明显，整体偏高蛋白饱腹型。";
            case 402:
                return "鸡肉卷方便拿着吃，适合赶时间的轻食一餐。";
            case 403:
                return "清爽收尾款，饭后或者下午茶都合适。";
            default:
                return food.getDescription();
        }
    }

    public static String buildFoodDetail(FoodBean food) {
        switch (food.getId()) {
            case 101:
                return "这份麻辣香锅用了牛肉丸、土豆、藕片和青菜做主料，香锅酱汁厚实，入口先是辣香，后面能吃到蔬菜和丸子的层次感。整体更适合喜欢重口、想认真配一份米饭的人。";
            case 102:
                return "整块鸡腿经过腌制后再烹调，肉质会更嫩，藤椒香气比单纯辣味更突出。搭配热米饭吃起来不会太腻，适合想吃带点刺激感又不想太重辣的人。";
            case 103:
                return "柠檬茶主打清爽解腻，入口有明显果酸和冰感，甜度不会压过茶味。无论是搭配辣菜还是单独喝，都比较解口。";
            case 201:
                return "番茄牛腩饭的核心是浓郁番茄底和软烂牛腩，酸甜感柔和，不会太刺激。它更像一份稳妥的热饭，适合午饭或晚饭想吃得舒服一点的时候。";
            case 202:
                return "玉米排骨汤走的是清甜路线，排骨和玉米的味道都比较自然，喝起来不油。配米饭一起吃会更完整，也适合天气热时来一份轻一点的套餐。";
            case 203:
                return "蒸蛋套餐口感细嫩顺滑，搭配青菜和米饭后不会单调。整体味型温和，适合不想吃太辣、太油或者想吃得更家常一些的时候。";
            case 301:
                return "红烧牛肉面的汤底偏浓香型，牛肉块存在感很足，手工面条也更有嚼劲。它的整体满足感很强，适合想吃热乎主食并且希望分量扎实一点的人。";
            case 302:
                return "酸辣粉主打酸、辣、香三种味道的平衡，红薯粉本身弹滑，吃起来很顺。作为夜宵或者想开胃的时候都很合适，整体属于轻负担但有味道的一类。";
            case 303:
                return "煎蛋加菜套餐是比较朴素但耐吃的组合，胜在价格友好、饱腹稳定。适合上课间隙快速吃一顿，也适合预算有限的时候。";
            case 401:
                return "黑椒鸡排饭以鸡排为主角，外层微酥，里面仍然保留汁水，黑椒酱会把整体风味拉得更浓。适合喜欢西式快餐口味，又想吃得更扎实一点的人。";
            case 402:
                return "奥尔良鸡肉卷口味偏轻快，鸡肉带一点甜辣感，卷饼柔软，适合边走边吃。它的优点是方便、清爽、不会太撑。";
            case 403:
                return "水果酸奶杯以酸奶为底，水果负责增加清爽感和层次感。饭后吃会比较解腻，也适合作为轻食搭配里的补充甜品。";
            default:
                return food.getDescription();
        }
    }

    public static String buildFoodTags(FoodBean food) {
        switch (food.getId()) {
            case 101:
                return "香辣  ·  招牌推荐  ·  下饭  ·  食材丰富";
            case 102:
                return "微辣  ·  鸡腿肉  ·  米饭搭子  ·  回购高";
            case 103:
                return "冰饮  ·  清爽  ·  解辣  ·  搭餐推荐";
            case 201:
                return "热饭热菜  ·  酸甜  ·  牛腩软烂  ·  饱腹";
            case 202:
                return "热汤  ·  清淡  ·  晚餐友好  ·  轻负担";
            case 203:
                return "蒸蛋  ·  温和  ·  套餐  ·  家常";
            case 301:
                return "面食  ·  浓汤  ·  牛肉块大  ·  夜宵友好";
            case 302:
                return "酸辣  ·  开胃  ·  弹滑  ·  热门单品";
            case 303:
                return "实惠  ·  家常  ·  饱腹  ·  学生党";
            case 401:
                return "鸡排  ·  黑椒  ·  高蛋白  ·  饱腹";
            case 402:
                return "轻食  ·  卷饼  ·  赶课友好  ·  微辣";
            case 403:
                return "甜品  ·  酸奶  ·  清爽  ·  饭后推荐";
            default:
                return food.getTaste();
        }
    }

    public static String buildFoodPairing(FoodBean food) {
        switch (food.getId()) {
            case 101:
                return "推荐搭配：一份米饭或冰镇柠檬茶，能把香辣味拉得更平衡。";
            case 102:
                return "推荐搭配：清爽饮品或一份蔬菜小食，口感会更完整。";
            case 103:
                return "推荐搭配：麻辣香锅、鸡腿饭这类重口主食。";
            case 201:
                return "推荐搭配：玉米排骨汤或清爽小菜，晚饭会更舒服。";
            case 202:
                return "推荐搭配：番茄牛腩饭，汤饭组合很适合一顿完整正餐。";
            case 203:
                return "推荐搭配：热汤或水果酸奶杯，清淡但不单调。";
            case 301:
                return "推荐搭配：酸辣粉双拼或一杯冰饮，夜宵感更足。";
            case 302:
                return "推荐搭配：煎蛋加菜套餐或冰饮，既开胃也能更饱。";
            case 303:
                return "推荐搭配：玉米排骨汤或酸辣粉，丰俭都能搭。";
            case 401:
                return "推荐搭配：水果酸奶杯，能让整体更清爽。";
            case 402:
                return "推荐搭配：水果酸奶杯或轻饮，适合当一顿轻食。";
            case 403:
                return "推荐搭配：鸡排饭、鸡肉卷这类主食后作为饭后甜点。";
            default:
                return "推荐搭配：根据口味选择主食或饮品。";
        }
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
