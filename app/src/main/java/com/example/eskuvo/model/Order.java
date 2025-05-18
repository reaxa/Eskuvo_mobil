package com.example.eskuvo.model;

import java.util.List;

public class Order {
    private List<CartItem> items;
    private double totalPrice;
    private long timestamp;

    public Order(List<CartItem> items, double totalPrice, long timestamp) {
        this.items = items;
        this.totalPrice = totalPrice;
        this.timestamp = timestamp;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
