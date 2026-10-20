package com.mycompany.inventorymanagement;

public class Product {
    /** Fallback used only when a row has no per-product threshold. */
    public static final int DEFAULT_LOW_STOCK_THRESHOLD = 5;

    private int id;
    private String name;
    private int categoryId;
    private String categoryName;
    private int quantity;
    private double price;
    private int lowStockThreshold = DEFAULT_LOW_STOCK_THRESHOLD;

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

    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = Math.max(0, lowStockThreshold);
    }

    public double getTotalValue() { return quantity * price; }

    public boolean isLowStock() { return quantity <= lowStockThreshold; }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) qty=%d price=%s low<=%d",
                id, name, categoryName, quantity,
                Currency.format(price), lowStockThreshold);
    }
}