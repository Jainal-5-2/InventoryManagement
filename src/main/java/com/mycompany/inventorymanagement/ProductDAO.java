package com.mycompany.inventorymanagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // ---- Reads ----
    public List<Product> findAll() {
        String sql = "SELECT p.id, p.name, p.category_id, c.name AS category_name, "
                + "p.quantity, p.price "
                + "FROM products p LEFT JOIN categories c ON p.category_id = c.id "
                + "ORDER BY p.id";
        return query(sql, null);
    }

    public List<Product> search(String keyword) {
        String sql = "SELECT p.id, p.name, p.category_id, c.name AS category_name, "
                + "p.quantity, p.price "
                + "FROM products p LEFT JOIN categories c ON p.category_id = c.id "
                + "WHERE p.name LIKE ? OR c.name LIKE ? "
                + "ORDER BY p.id";
        String like = "%" + keyword + "%";
        return query(sql, new Object[]{like, like});
    }

    public List<Product> findByCategory(int categoryId) {
        String sql = "SELECT p.id, p.name, p.category_id, c.name AS category_name, "
                + "p.quantity, p.price "
                + "FROM products p LEFT JOIN categories c ON p.category_id = c.id "
                + "WHERE p.category_id = ? "
                + "ORDER BY p.id";
        return query(sql, new Object[]{categoryId});
    }

    public List<Product> searchByCategoryAndKeyword(int categoryId, String keyword) {
        String sql = "SELECT p.id, p.name, p.category_id, c.name AS category_name, "
                + "p.quantity, p.price "
                + "FROM products p LEFT JOIN categories c ON p.category_id = c.id "
                + "WHERE p.category_id = ? AND (p.name LIKE ? OR c.name LIKE ?) "
                + "ORDER BY p.id";
        String like = "%" + keyword + "%";
        return query(sql, new Object[]{categoryId, like, like});
    }

    public Product findById(int id) {
        String sql = "SELECT p.id, p.name, p.category_id, c.name AS category_name, "
                + "p.quantity, p.price "
                + "FROM products p LEFT JOIN categories c ON p.category_id = c.id "
                + "WHERE p.id = ?";
        List<Product> results = query(sql, new Object[]{id});
        return results.isEmpty() ? null : results.get(0);
    }

    // ---- Writes ----
    public boolean insert(Product p) {
        String sql = "INSERT INTO products (name, category_id, quantity, price) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getName());
            if (p.getCategoryId() > 0) {
                ps.setInt(2, p.getCategoryId());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setInt(3, p.getQuantity());
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(p.getPrice()));

            int rows = ps.executeUpdate();
            if (rows == 0) {
                return false;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setId(keys.getInt(1));
                }
            }
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Product p) {
        String sql = "UPDATE products SET name = ?, category_id = ?, quantity = ?, "
                + "price = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getName());
            if (p.getCategoryId() > 0) {
                ps.setInt(2, p.getCategoryId());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setInt(3, p.getQuantity());
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(p.getPrice()));
            ps.setInt(5, p.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---- Categories ----
    public List<Category> findAllCategories() {
        String sql = "SELECT id, name FROM categories ORDER BY name";
        List<Category> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Category(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ---- Stats ----
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM products";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countLowStock() {
        String sql = "SELECT COUNT(*) FROM products WHERE quantity <= ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Product.LOW_STOCK_THRESHOLD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double totalInventoryValue() {
        String sql = "SELECT COALESCE(SUM(quantity * price), 0) FROM products";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Product> findLowStock() {
        String sql = "SELECT p.id, p.name, p.category_id, c.name AS category_name, "
                + "p.quantity, p.price "
                + "FROM products p LEFT JOIN categories c ON p.category_id = c.id "
                + "WHERE p.quantity <= ? "
                + "ORDER BY p.quantity ASC";
        return query(sql, new Object[]{Product.LOW_STOCK_THRESHOLD});
    }

    // ---- Internal ----
    private List<Product> query(String sql, Object[] params) {
        List<Product> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product();
                    p.setId(rs.getInt("id"));
                    p.setName(rs.getString("name"));
                    p.setCategoryId(rs.getInt("category_id"));
                    p.setCategoryName(rs.getString("category_name"));
                    p.setQuantity(rs.getInt("quantity"));
                    p.setPrice(rs.getDouble("price"));
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public java.util.Map<String, Double> valueByCategory() {
        String sql = "SELECT COALESCE(c.name, 'Uncategorized') AS cat, "
                + "COALESCE(SUM(p.quantity * p.price), 0) AS total "
                + "FROM products p LEFT JOIN categories c ON p.category_id = c.id "
                + "GROUP BY c.name ORDER BY total DESC";
        java.util.Map<String, Double> map = new java.util.LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("cat"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public java.util.Map<String, Integer> stockByCategory() {
        String sql = "SELECT COALESCE(c.name, 'Uncategorized') AS cat, "
                + "COALESCE(SUM(p.quantity), 0) AS qty "
                + "FROM products p LEFT JOIN categories c ON p.category_id = c.id "
                + "GROUP BY c.name ORDER BY qty DESC";
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("cat"), rs.getInt("qty"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    // ---- Test ----
    public static void main(String[] args) {
        ProductDAO dao = new ProductDAO();

        System.out.println("All products:");
        for (Product p : dao.findAll()) {
            System.out.println("  " + p);
        }

        System.out.println("\nTotal products:  " + dao.countAll());
        System.out.println("Low stock count: " + dao.countLowStock());
        System.out.println("Total value:     " + Currency.format(dao.totalInventoryValue()));

        System.out.println("\nLow stock items:");
        for (Product p : dao.findLowStock()) {
            System.out.println("  " + p);
        }

        System.out.println("\nBy category (id=1):");
        for (Product p : dao.findByCategory(1)) {
            System.out.println("  " + p);
        }

        System.out.println("\nSearch 'lap':");
        for (Product p : dao.search("lap")) {
            System.out.println("  " + p);
        }
    }
}
