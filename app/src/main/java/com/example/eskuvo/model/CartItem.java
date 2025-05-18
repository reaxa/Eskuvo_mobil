package com.example.eskuvo.model;

public class CartItem {
    private Decoration decoration;
    private int quantity;

    public CartItem(Decoration decoration, int quantity) {
        this.decoration = decoration;
        this.quantity = quantity;
    }

    public Decoration getDecoration() { return decoration; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
