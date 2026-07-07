package com.example.onlinefooddeliverysystem.util;

import com.example.onlinefooddeliverysystem.model.ShopBean;

public class DeliveryUtils {
    public static int estimateDeliveryMinutes(ShopBean shop, String address) {
        int baseMinutes = extractMinutes(shop.getDeliveryTime());
        return baseMinutes + resolveAddressOffset(address);
    }

    public static String buildListDeliveryText(ShopBean shop, String address) {
        return "约 " + estimateDeliveryMinutes(shop, address) + " 分钟送达";
    }

    public static String buildCheckoutDeliveryText(ShopBean shop, String address) {
        return "配送至 " + address + "，预计 " + estimateDeliveryMinutes(shop, address) + " 分钟送达";
    }

    private static int extractMinutes(String text) {
        if (text == null) {
            return 25;
        }
        String digits = text.replaceAll("\\D+", "");
        if (digits.isEmpty()) {
            return 25;
        }
        return Integer.parseInt(digits);
    }

    private static int resolveAddressOffset(String address) {
        if (address == null) {
            return 6;
        }
        if (address.contains("西北门")) {
            return 4;
        }
        if (address.contains("南门")) {
            return 7;
        }
        if (address.contains("西门")) {
            return 5;
        }
        if (address.contains("西南门")) {
            return 9;
        }
        return 6;
    }
}
