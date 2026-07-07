package com.example.onlinefooddeliverysystem.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.onlinefooddeliverysystem.data.UserManager;
import com.example.onlinefooddeliverysystem.model.CartItem;
import com.example.onlinefooddeliverysystem.model.OrderBean;
import com.example.onlinefooddeliverysystem.util.FormatUtils;

import java.util.ArrayList;

public class OrderDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "orders.db";
    private static final int DB_VERSION = 2;
    private static final String TABLE_ORDER = "order_record";

    private final Context appContext;

    public OrderDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.appContext = context.getApplicationContext();
        migrateLegacyOrdersForCurrentUser();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_ORDER + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_key TEXT," +
                "shop_name TEXT," +
                "address TEXT," +
                "total_price INTEGER," +
                "delivery_fee INTEGER," +
                "status TEXT," +
                "created_at TEXT," +
                "items_text TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_ORDER + " ADD COLUMN user_key TEXT");
        }
    }

    public long saveOrder(OrderBean order) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_key", currentUserKey());
        values.put("shop_name", order.getShopName());
        values.put("address", order.getAddress());
        values.put("total_price", order.getTotalPrice());
        values.put("delivery_fee", order.getDeliveryFee());
        values.put("status", order.getStatus());
        values.put("created_at", order.getCreatedAt());
        values.put("items_text", toItemsText(order.getItems()));
        long id = db.insert(TABLE_ORDER, null, values);
        order.setId(id);
        return id;
    }

    public ArrayList<OrderBean> getOrders() {
        ArrayList<OrderBean> orders = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_ORDER,
                null,
                "user_key=?",
                new String[]{currentUserKey()},
                null,
                null,
                "_id DESC"
        );
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
            String shopName = cursor.getString(cursor.getColumnIndexOrThrow("shop_name"));
            String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
            int totalPrice = cursor.getInt(cursor.getColumnIndexOrThrow("total_price"));
            int deliveryFee = cursor.getInt(cursor.getColumnIndexOrThrow("delivery_fee"));
            String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
            String createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
            orders.add(new OrderBean(id, shopName, address, totalPrice, deliveryFee, status, createdAt, new ArrayList<CartItem>()));
        }
        cursor.close();
        return orders;
    }

    public void updateStatus(long orderId, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        db.update(TABLE_ORDER, values, "_id=? AND user_key=?", new String[]{String.valueOf(orderId), currentUserKey()});
    }

    public void clearOrders() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_ORDER, "user_key=?", new String[]{currentUserKey()});
    }

    private void migrateLegacyOrdersForCurrentUser() {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_key", currentUserKey());
        db.update(TABLE_ORDER, values, "user_key IS NULL OR user_key=''", null);
    }

    private String currentUserKey() {
        return UserManager.getInstance().getCurrentUserStorageKey(appContext);
    }

    private String toItemsText(ArrayList<CartItem> items) {
        StringBuilder builder = new StringBuilder();
        for (CartItem item : items) {
            if (builder.length() > 0) {
                builder.append("、");
            }
            builder.append(item.getFood().getName())
                    .append(" x")
                    .append(item.getCount())
                    .append(" ")
                    .append(FormatUtils.price(item.getTotalPrice()));
        }
        return builder.toString();
    }
}
