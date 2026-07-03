package com.example.onlinefooddeliverysystem.model;

import java.io.Serializable;
import java.util.ArrayList;

public class OrderBean implements Serializable {
    private long id;
    private final String shopName;
    private final String address;
    private final int totalPrice;
    private final int deliveryFee;
    private final String status;
    private final String createdAt;
    private final ArrayList<CartItem> items;

    public OrderBean(long id, String shopName, String address, int totalPrice, int deliveryFee,
                     String status, String createdAt, ArrayList<CartItem> items) {
        this.id = id;
        this.shopName = shopName;
        this.address = address;
        this.totalPrice = totalPrice;
        this.deliveryFee = deliveryFee;
        this.status = status;
        this.createdAt = createdAt;
        this.items = items;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getShopName() {
        return shopName;
    }

    public String getAddress() {
        return address;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public int getDeliveryFee() {
        return deliveryFee;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public ArrayList<CartItem> getItems() {
        return items;
    }
}
