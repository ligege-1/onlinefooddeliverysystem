package com.example.onlinefooddeliverysystem.data;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
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

        executor.execute(() -> {
            try {
                String responseText = requestRecommendation(userPrompt);
                AiRecommendationResult result = parseResult(responseText);
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
        return "你是校园外卖 App 的智能点餐助手。只能从用户给出的菜单里选择，不允许编造店铺或菜品。" +
                "你必须只返回一个 JSON 对象，不要出现 user、assistant、Markdown、代码块或额外解释。" +
                "必须使用英文双引号，不能使用中文引号，不能漏掉数组和对象的结束符。" +
                "reason 中不要出现双引号。JSON 格式必须完全符合：" +
                "{\"shopName\":\"店铺名\",\"foods\":[\"菜品1\",\"菜品2\"],\"reason\":\"80字以内推荐理由\"}" +
                "foods 中的菜品名称必须和菜单里的菜品名称完全一致。";
    }

    private JSONObject message(String role, String content) throws JSONException {
        JSONObject message = new JSONObject();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private AiRecommendationResult parseResult(String content) throws JSONException {
        String jsonText = extractJsonObject(content);
        try {
            JSONObject json = new JSONObject(jsonText);
            String shopName = json.optString("shopName", "");
            String reason = json.optString("reason", "已根据你的需求生成推荐。");
            JSONArray foodsArray = json.optJSONArray("foods");
            ArrayList<String> foodNames = new ArrayList<>();
            if (foodsArray != null) {
                for (int i = 0; i < foodsArray.length(); i++) {
                    String name = foodsArray.optString(i);
                    if (!name.trim().isEmpty()) {
                        foodNames.add(name);
                    }
                }
            }
            return new AiRecommendationResult(shopName, foodNames, reason, content);
        } catch (JSONException ignored) {
            return parseLooseResult(jsonText, content);
        }
    }

    private String extractJsonObject(String content) throws JSONException {
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

    private AiRecommendationResult parseLooseResult(String jsonText, String rawText) {
        String shopName = extractValue(jsonText, "shopName");
        if (shopName.isEmpty()) {
            shopName = extractValue(jsonText, "shopNameName");
        }
        String reason = extractValue(jsonText, "reason");
        ArrayList<String> foodNames = new ArrayList<>();

        for (String food : DataRepository.allFoodNames()) {
            if (jsonText.contains(food) || rawText.contains(food)) {
                foodNames.add(food);
            }
        }

        if (shopName.isEmpty()) {
            for (String shop : DataRepository.allShopNames()) {
                if (jsonText.contains(shop) || rawText.contains(shop)) {
                    shopName = shop;
                    break;
                }
            }
        }
        if (reason.isEmpty()) {
            reason = buildReason(shopName, foodNames);
        }
        return new AiRecommendationResult(shopName, foodNames, reason, rawText);
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

    private String buildReason(String shopName, ArrayList<String> foodNames) {
        StringBuilder builder = new StringBuilder();
        builder.append("AI 根据你的用餐需求");
        if (!shopName.isEmpty()) {
            builder.append("推荐 ").append(shopName);
        }
        if (!foodNames.isEmpty()) {
            builder.append("，搭配 ");
            for (int i = 0; i < foodNames.size(); i++) {
                if (i > 0) {
                    builder.append("、");
                }
                builder.append(foodNames.get(i));
            }
        }
        builder.append("，价格和口味更适合当前需求。");
        return builder.toString();
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
