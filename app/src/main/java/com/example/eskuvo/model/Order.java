package com.example.eskuvo.model;

import java.util.List;

public class Order {
    private String orderId; // <-- EZ AZ ÚJ MEZŐ
    private List<CartItem> items;
    private double totalPrice;
    private long timestamp;

    public Order() {
        // Üres konstruktor a Firebase számára
    }

    // Konstruktor a Firebase-ből való olvasáshoz (az OrderId nélkül, majd azt külön beállítjuk)
    public Order(List<CartItem> items, double totalPrice, long timestamp) {
        this.items = items;
        this.totalPrice = totalPrice;
        this.timestamp = timestamp;
    }

    // Getterek és Setterek
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) { // <-- EZ AZ ÚJ SETTER
        this.orderId = orderId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) { // Hozzáadva a setItems a teljes deserializálásért
        this.items = items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) { // Hozzáadva a setTotalPrice
        this.totalPrice = totalPrice;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) { // Hozzáadva a setTimestamp
        this.timestamp = timestamp;
    }
}