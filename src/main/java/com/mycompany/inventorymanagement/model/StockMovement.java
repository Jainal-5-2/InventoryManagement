package com.mycompany.inventorymanagement.model;

import java.sql.Timestamp;

public class StockMovement {
    private int id;
    private int productId;
    private String productName;   // populated by joins, not stored
    private int changeQty;
    private String reason;
    private Integer userId;       // nullable
    private String userName;      // populated by joins, not stored
    private Timestamp movedAt;

    public StockMovement() {}

    public StockMovement(int productId, int changeQty, String reason, Integer userId) {
        this.productId = productId;
        this.changeQty = changeQty;
        this.reason = reason;
        this.userId = userId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getChangeQty() { return changeQty; }
    public void setChangeQty(int changeQty) { this.changeQty = changeQty; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Timestamp getMovedAt() { return movedAt; }
    public void setMovedAt(Timestamp movedAt) { this.movedAt = movedAt; }

    @Override
    public String toString() {
        return String.format("[%d] product=%d change=%+d reason=%s by=%s",
                id, productId, changeQty, reason, userName);
    }
}