package com.example.eskuvo.service;
import com.example.eskuvo.model.Decoration;
import com.example.eskuvo.model.CartItem; // ha CartItem is a service csomagban van
import java.util.ArrayList;
import java.util.List;
public class CartManager {
    private static CartManager instance;
    private List<CartItem> cartItems = new ArrayList<>();

    private CartManager() {}

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addToCart(Decoration decoration) {
        for (CartItem item : cartItems) {
            if (item.getDecoration().getId().equals(decoration.getId())) {
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }
        cartItems.add(new CartItem(decoration, 1));
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public double getTotalPrice() {
        double sum = 0;
        for (CartItem item : cartItems) {
            sum += item.getDecoration().getPrice() * item.getQuantity();
        }
        return sum;
    }

    public void clearCart() {
        cartItems.clear();
    }
}