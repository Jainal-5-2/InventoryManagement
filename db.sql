-- ============================================
-- Inventory Management System - Database Schema
-- Single-role version
-- ============================================

CREATE DATABASE IF NOT EXISTS inventory_db;
USE inventory_db;

-- ---------- USERS ----------
CREATE TABLE IF NOT EXISTS users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,       -- plaintext for now, hash later
    full_name   VARCHAR(100) NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---------- CATEGORIES ----------
CREATE TABLE IF NOT EXISTS categories (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(50) NOT NULL UNIQUE
);

-- ---------- PRODUCTS ----------
CREATE TABLE IF NOT EXISTS products (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    category_id   INT,
    quantity      INT NOT NULL DEFAULT 0,
    price         DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    low_stock_threshold INT NOT NULL DEFAULT 5,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_category
        FOREIGN KEY (category_id) REFERENCES categories(id)
        ON DELETE SET NULL,
    CONSTRAINT chk_quantity CHECK (quantity >= 0),
    CONSTRAINT chk_price    CHECK (price >= 0)
);

-- ---------- STOCK MOVEMENTS (audit trail) ----------
CREATE TABLE IF NOT EXISTS stock_movements (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    product_id  INT NOT NULL,
    change_qty  INT NOT NULL,
    reason      VARCHAR(100),
    user_id     INT,
    moved_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_movement_product
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_movement_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================
-- SEED DATA
-- ============================================

INSERT INTO users (username, password, full_name) VALUES
('admin', 'admin123', 'System Administrator');

INSERT INTO categories (name) VALUES
('Electronics'), ('Accessories'), ('Office Supplies'), ('Furniture');

INSERT INTO products (name, category_id, quantity, price, low_stock_threshold) VALUES
('Laptop',      1, 10, 899.99, 3),
('Mouse',       2, 50,  19.99, 10),
('Keyboard',    2, 30,  49.99, 5),
('Notebook',    3, 100,  2.50, 20),
('Office Chair',4,  8, 129.00, 2);

INSERT INTO stock_movements (product_id, change_qty, reason, user_id) VALUES
(1, 10, 'Initial stock', 1),
(2, 50, 'Initial stock', 1),
(3, 30, 'Initial stock', 1);

-- Verify
SELECT 'users' AS table_name, COUNT(*) AS rows_count FROM users
UNION ALL SELECT 'categories', COUNT(*) FROM categories
UNION ALL SELECT 'products',   COUNT(*) FROM products;