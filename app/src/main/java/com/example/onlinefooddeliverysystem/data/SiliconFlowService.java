package com.example.onlinefooddeliverysystem.data;

import android.os.Handler;
import android.os.Looper;

import com.example.onlinefooddeliverysystem.model.FoodBean;
import com.example.onlinefooddeliverysystem.model.ShopBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SiliconFlowService {
    public interface Callback {
        void onSuccess(AiRecommendationResult result);

        void onError(String message);
    }

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void recommend(String userPrompt, Callback callback) {
        if (SiliconFlowConfig.API_KEY.trim().isEmpty()) {
            callback.onError("请先配置 SiliconFlow API Key");
            return;
        }

        AiRecommendationResult localResult = buildRuleBasedRecommendation(userPrompt);
        if (localResult != null) {
            callback.onSuccess(localResult);
            return;
        }

        executor.execute(() -> {
            try {
                String responseText = requestRecommendation(userPrompt);
                AiRecommendationResult result = parseResult(userPrompt, responseText);
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("AI 服务暂时不可用：" + e.getMessage()));
            }
        });
    }

    private String requestRecommendation(String userPrompt) throws IOException, JSONException {
        JSONObject body = new JSONObject();
        body.put("model", SiliconFlowConfig.MODEL);
        body.put("temperature", 0.2);
        body.put("max_tokens", 500);

        JSONArray messages = new JSONArray();
        messages.put(message("system", buildSystemPrompt()));
        messages.put(message("user", "用户需求：" + userPrompt + "\n\n可选菜单：\n" + DataRepository.buildMenuForPrompt()));
        body.put("messages", messages);

        Request request = new Request.Builder()
                .url(SiliconFlowConfig.API_URL)
                .header("Authorization", "Bearer " + SiliconFlowConfig.API_KEY)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String text = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                throw new IOException("HTTP " + response.code() + " " + text);
            }
            JSONObject json = new JSONObject(text);
            return json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        }
    }

    private String buildSystemPrompt() {
        return "你是校园外卖 App 的智能点餐助手。"
                + "你只能从用户给出的菜单里选择店铺和菜品，不能编造不存在的店铺或菜品。"
                + "如果用户只说清淡、健身、减脂、高蛋白、便宜、夜宵这类需求，你要按菜单内容做合理匹配。"
                + "你必须只返回一个合法 JSON 对象，不能返回 Markdown、代码块、说明文字或多余字段。"
                + "JSON 只允许包含 shopName、foods、reason 三个字段。"
                + "foods 里的每个菜名必须和菜单中的原始菜名完全一致。"
                + "reason 必须是真实推荐理由，不能写模板词、占位词，不能出现“80字以内推荐理由”这类句子。"
                + "如果拿不准，请返回最匹配的一家店和 1 到 3 个真实菜品。"
                + "返回格式："
                + "{\"shopName\":\"真实店铺名\",\"foods\":[\"真实菜品名1\",\"真实菜品名2\"],\"reason\":\"真实推荐理由\"}";
    }

    private JSONObject message(String role, String content) throws JSONException {
        JSONObject message = new JSONObject();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private AiRecommendationResult parseResult(String userPrompt, String content) throws JSONException {
        String jsonText = extractJsonObject(content);
        try {
            JSONObject json = new JSONObject(jsonText);
            String shopName = json.optString("shopName", "").trim();
            String reason = json.optString("reason", "").trim();
            JSONArray foodsArray = json.optJSONArray("foods");
            ArrayList<String> foodNames = new ArrayList<>();
            ShopBean shop = DataRepository.findShopByName(shopName);

            if (foodsArray != null && shop != null) {
                for (int i = 0; i < foodsArray.length(); i++) {
                    String name = foodsArray.optString(i).trim();
                    if (!name.isEmpty() && belongsToShop(shop, name) && !foodNames.contains(name)) {
                        foodNames.add(name);
                    }
                }
            }

            if (shop == null || foodNames.isEmpty() || isPlaceholderReason(reason)) {
                return parseLooseResult(userPrompt, jsonText, content);
            }
            return new AiRecommendationResult(shop.getName(), foodNames, reason, content);
        } catch (JSONException ignored) {
            return parseLooseResult(userPrompt, jsonText, content);
        }
    }

    private String extractJsonObject(String content) {
        String clean = content == null ? "" : content.trim();
        clean = clean.replace("```json", "").replace("```", "").trim();

        int shopNameIndex = clean.indexOf("\"shopName\"");
        if (shopNameIndex < 0) {
            shopNameIndex = clean.indexOf("'shopName'");
        }

        int start = shopNameIndex >= 0 ? clean.lastIndexOf('{', shopNameIndex) : clean.indexOf('{');
        if (start >= 0) {
            int end = findMatchingBrace(clean, start);
            if (end > start) {
                return clean.substring(start, end + 1);
            }
        }
        return clean;
    }

    private AiRecommendationResult parseLooseResult(String userPrompt, String jsonText, String rawText) {
        String shopName = extractValue(jsonText, "shopName");
        String reason = extractValue(jsonText, "reason");
        ShopBean shop = DataRepository.findShopByName(shopName);
        if (shop == null) {
            for (String knownShop : DataRepository.allShopNames()) {
                if (jsonText.contains(knownShop) || rawText.contains(knownShop)) {
                    shop = DataRepository.findShopByName(knownShop);
                    break;
                }
            }
        }

        if (shop == null) {
            AiRecommendationResult localResult = buildRuleBasedRecommendation(userPrompt);
            if (localResult != null) {
                return localResult;
            }
            shop = DataRepository.firstShop();
        }

        ArrayList<String> foodNames = new ArrayList<>();
        for (int i = 0; i < shop.getFoods().size(); i++) {
            String foodName = shop.getFoods().get(i).getName();
            if ((jsonText.contains(foodName) || rawText.contains(foodName)) && !foodNames.contains(foodName)) {
                foodNames.add(foodName);
            }
        }

        if (foodNames.isEmpty() && !shop.getFoods().isEmpty()) {
            foodNames.add(shop.getFoods().get(0).getName());
        }

        if (isPlaceholderReason(reason)) {
            reason = buildReason(shop.getName(), foodNames);
        }
        return new AiRecommendationResult(shop.getName(), foodNames, reason, rawText);
    }

    private String extractValue(String text, String key) {
        String marker = "\"" + key + "\":\"";
        int start = text.indexOf(marker);
        if (start < 0) {
            return "";
        }
        start += marker.length();
        int end = text.indexOf("\"", start);
        if (end <= start) {
            end = text.length();
        }
        return text.substring(start, end).trim();
    }

    private boolean isPlaceholderReason(String reason) {
        String safe = reason == null ? "" : reason.trim().toLowerCase(Locale.ROOT);
        return safe.isEmpty()
                || safe.contains("推荐理由")
                || safe.contains("字以内")
                || safe.contains("80")
                || safe.contains("both8");
    }

    private boolean belongsToShop(ShopBean shop, String foodName) {
        for (int i = 0; i < shop.getFoods().size(); i++) {
            if (shop.getFoods().get(i).getName().equals(foodName)) {
                return true;
            }
        }
        return false;
    }

    private String buildReason(String shopName, ArrayList<String> foodNames) {
        StringBuilder builder = new StringBuilder("这份推荐更贴合你当前的口味需求");
        if (!shopName.isEmpty()) {
            builder.append("，店铺选择了 ").append(shopName);
        }
        if (!foodNames.isEmpty()) {
            builder.append("，菜品包含 ");
            for (int i = 0; i < foodNames.size(); i++) {
                if (i > 0) {
                    builder.append("、");
                }
                builder.append(foodNames.get(i));
            }
        }
        builder.append("。");
        return builder.toString();
    }

    private AiRecommendationResult buildRuleBasedRecommendation(String userPrompt) {
        String prompt = userPrompt == null ? "" : userPrompt.trim().toLowerCase(Locale.ROOT);
        if (prompt.isEmpty()) {
            return null;
        }

        if (containsAny(prompt, "健身", "减脂", "增肌", "高蛋白", "轻食", "低脂")) {
            return buildResult("元气轻食站",
                    new String[]{"黑椒鸡排饭", "奥尔良鸡肉卷", "水果酸奶杯"},
                    "更偏轻食和高蛋白，比较适合健身或控制饮食。");
        }

        if (containsAny(prompt, "清淡", "养胃", "热汤", "不辣")) {
            return buildResult("暖心简餐",
                    new String[]{"蒸蛋套餐", "玉米排骨汤", "番茄牛腩饭"},
                    "口味更温和，汤饭搭配也更适合想吃清淡一点的时候。");
        }

        if (containsAny(prompt, "夜宵", "面", "粉")) {
            return buildResult("一碗热面",
                    new String[]{"红烧牛肉面", "酸辣粉"},
                    "热面和粉类更适合夜宵，吃起来也更有饱腹感。");
        }

        if (containsAny(prompt, "辣", "重口", "下饭")) {
            return buildResult("川味小馆",
                    new String[]{"招牌麻辣香锅", "藤椒鸡腿饭"},
                    "这家整体偏香辣重口，比较符合想吃辣和下饭的需求。");
        }

        return null;
    }

    private boolean containsAny(String prompt, String... keywords) {
        for (String keyword : keywords) {
            if (prompt.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private AiRecommendationResult buildResult(String shopName, String[] candidateFoods, String reason) {
        ShopBean shop = DataRepository.findShopByName(shopName);
        if (shop == null) {
            return null;
        }
        ArrayList<String> foods = new ArrayList<>();
        for (String candidate : candidateFoods) {
            if (belongsToShop(shop, candidate)) {
                foods.add(candidate);
            }
        }
        if (foods.isEmpty() && !shop.getFoods().isEmpty()) {
            foods.add(shop.getFoods().get(0).getName());
        }
        return new AiRecommendationResult(shop.getName(), foods, reason, "");
    }

    private int findMatchingBrace(String text, int start) {
        int depth = 0;
        boolean inString = false;
        char quote = 0;
        boolean escaped = false;

        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (inString) {
                if (c == quote) {
                    inString = false;
                }
                continue;
            }
            if (c == '"' || c == '\'') {
                inString = true;
                quote = c;
                continue;
            }
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
}
