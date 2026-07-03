package com.example.onlinefooddeliverysystem.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatUtils {
    public static String price(int price) {
        return "￥" + price;
    }

    public static String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(new Date());
    }
}
