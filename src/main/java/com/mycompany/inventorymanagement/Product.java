package com.mycompany.inventorymanagement;

public class Product {
    public static final int LOW_STOCK_THRESHOLD = 5;

    private int id;
    private String name;
    private int categoryId;
    private String categoryName;
    private int quantity;
    private double price;

    public Product() {}

    public Product(String name, int categoryId, int quantity, double price) {
        this.name = name;
        this.categoryId = categoryId;
        this.quantity = quantity;
        this.price = price;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getTotalValue() { return quantity * price; }

    public boolean isLowStock() { return quantity <= LOW_STOCK_THRESHOLD; }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) qty=%d price=%s",
                id, name, categoryName, quantity, Currency.format(price));
    }
}