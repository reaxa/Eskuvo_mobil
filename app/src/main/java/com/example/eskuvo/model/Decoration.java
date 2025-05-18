package com.example.eskuvo.model;

public class Decoration {
    private String id;         // Egyedi azonosító, pl. Firebase kulcs
    private String name;       // Termék neve
    private String description;// Termék leírása
    private double price;      // Ár
    private String imageUrl;   // Kép URL-je

    // Üres konstruktor Firebase-hez
    public Decoration() {
    }

    // Teljes konstruktor (minden mezőhöz)
    public Decoration(String id, String name, String description, double price, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    // Egyszerűsített konstruktor csak név és ár alapján – ezt használd a CartItem példányosításához
    public Decoration(String name, double price) {
        this.name = name;
        this.price = price;
    }

    // Getterek és setterek
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
