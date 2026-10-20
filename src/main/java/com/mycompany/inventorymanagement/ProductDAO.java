package com.mycompany.inventorymanagement;

import com.mycompany.inventorymanagement.model.StockMovement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    private static final String SELECT_COLS
            = "SELECT p.id, p.name, p.category_id, c.name AS category_name, "
            + "p.quantity, p.price, p.low_stock_threshold "
            + "FROM products p LEFT JOIN categories c ON p.category_id = c.id ";

    // ---- Reads ----
    public List<Product> findAll() {
        return query(SELECT_COLS + "ORDER BY p.id", null);
    }

    public List<Product> search(String keyword) {
        String like = "%" + keyword + "%";
        return query(SELECT_COLS
                + "WHERE p.name LIKE ? OR c.name LIKE ? ORDER BY p.id",
                new Object[]{like, like});
    }

    public List<Product> findByCategory(int categoryId) {
        return query(SELECT_COLS + "WHERE p.category_id = ? ORDER BY p.id",
                new Object[]{categoryId});
    }

    public List<Product> searchByCategoryAndKeyword(int categoryId, String keyword) {
        String like = "%" + keyword + "%";
        return query(SELECT_COLS
                + "WHERE p.category_id = ? AND (p.name LIKE ? OR c.name LIKE ?) "
                + "ORDER BY p.id",
                new Object[]{categoryId, like, like});
    }

    public Product findById(int id) {
        List<Product> results = query(SELECT_COLS + "WHERE p.id = ?",
                new Object[]{id});
        return results.isEmpty() ? null : results.get(0);
    }

    // ---- Writes (each opens one transaction covering product + movement) ----
    public boolean insert(Product p, Integer userId) {
        String insertProduct = "INSERT INTO products "
                + "(name, category_id, quantity, price, low_stock_threshold) "
                + "VALUES (?, ?, ?, ?, ?)";
        String insertMovement = "INSERT INTO stock_movements "
                + "(product_id, change_qty, reason, user_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int newId;
                try (PreparedStatement ps = conn.prepareStatement(insertProduct,
                        Statement.RETURN_GENERATED_KEYS)) {
                    bindProduct(ps, p, false);
                    if (ps.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            conn.rollback();
                            return false;
                        }
                        newId = keys.getInt(1);
                    }
                }
                p.setId(newId);

                if (p.getQuantity() != 0) {
                    try (PreparedStatement ps = conn.prepareStatement(insertMovement)) {
                        ps.setInt(1, newId);
                        ps.setInt(2, p.getQuantity());
                        ps.setString(3, "Initial stock");
                        if (userId == null) {
                            ps.setNull(4, java.sql.Types.INTEGER);
                        } else {
                            ps.setInt(4, userId);
                        }
                        ps.executeUpdate();
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates the product and, if {@code quantity} changed, writes a movement
     * row with the delta. Pass the reason (e.g. "Manual edit").
     */
    public boolean update(Product p, String reason, Integer userId) {
        String updateProduct = "UPDATE products SET name = ?, category_id = ?, "
                + "quantity = ?, price = ?, low_stock_threshold = ? WHERE id = ?";
        String insertMovement = "INSERT INTO stock_movements "
                + "(product_id, change_qty, reason, user_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int oldQty;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT quantity FROM products WHERE id = ? FOR UPDATE")) {
                    ps.setInt(1, p.getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                        oldQty = rs.getInt(1);
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(updateProduct)) {
                    bindProduct(ps, p, true);
                    if (ps.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }

                int delta = p.getQuantity() - oldQty;
                if (delta != 0) {
                    try (PreparedStatement ps = conn.prepareStatement(insertMovement)) {
                        ps.setInt(1, p.getId());
                        ps.setInt(2, delta);
                        ps.setString(3, reason == null ? "Manual edit" : reason);
                        if (userId == null) {
                            ps.setNull(4, java.sql.Types.INTEGER);
                        } else {
                            ps.setInt(4, userId);
                        }
                        ps.executeUpdate();
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        // stock_movements.product_id has ON DELETE CASCADE — rows vanish with
        // the product. Matches the existing README note about deletion.
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
        return scalarInt("SELECT COUNT(*) FROM products");
    }

    public int countLowStock() {
        return scalarInt("SELECT COUNT(*) FROM products "
                + "WHERE quantity <= low_stock_threshold");
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
        return query(SELECT_COLS + "WHERE p.quantity <= p.low_stock_threshold "
                + "ORDER BY p.quantity ASC", null);
    }

    // ---- Movements ----
    public List<StockMovement> findMovementsByProduct(int productId) {
        String sql = "SELECT m.id, m.product_id, m.change_qty, m.reason, "
                + "m.user_id, u.full_name AS user_name, m.moved_at "
                + "FROM stock_movements m "
                + "LEFT JOIN users u ON m.user_id = u.id "
                + "WHERE m.product_id = ? ORDER BY m.moved_at DESC, m.id DESC";
        return queryMovements(sql, new Object[]{productId});
    }

    public List<StockMovement> findRecentMovements(int limit) {
        String sql = "SELECT m.id, m.product_id, m.change_qty, m.reason, "
                + "m.user_id, u.full_name AS user_name, m.moved_at, "
                + "p.name AS product_name "
                + "FROM stock_movements m "
                + "LEFT JOIN users u ON m.user_id = u.id "
                + "LEFT JOIN products p ON m.product_id = p.id "
                + "ORDER BY m.moved_at DESC, m.id DESC LIMIT ?";
        return queryMovements(sql, new Object[]{limit});
    }

    // ---- Internal ----
    private int scalarInt(String sql) {
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

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
                    p.setLowStockThreshold(rs.getInt("low_stock_threshold"));
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void bindProduct(PreparedStatement ps, Product p, boolean includeId)
            throws SQLException {
        ps.setString(1, p.getName());
        if (p.getCategoryId() > 0) {
            ps.setInt(2, p.getCategoryId());
        } else {
            ps.setNull(2, java.sql.Types.INTEGER);
        }
        ps.setInt(3, p.getQuantity());
        ps.setBigDecimal(4, java.math.BigDecimal.valueOf(p.getPrice()));
        ps.setInt(5, p.getLowStockThreshold());
        if (includeId) {
            ps.setInt(6, p.getId());
        }
    }

    private List<StockMovement> queryMovements(String sql, Object[] params) {
        List<StockMovement> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StockMovement m = new StockMovement();
                    m.setId(rs.getInt("id"));
                    m.setProductId(rs.getInt("product_id"));
                    m.setChangeQty(rs.getInt("change_qty"));
                    m.setReason(rs.getString("reason"));
                    int uid = rs.getInt("user_id");
                    m.setUserId(rs.wasNull() ? null : uid);
                    m.setUserName(rs.getString("user_name"));
                    m.setMovedAt(rs.getTimestamp("moved_at"));
                    try {
                        m.setProductName(rs.getString("product_name"));
                    } catch (SQLException ignored) {
                        /* not selected in this query */ }
                    list.add(m);
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

    /**
     * The N products with the lowest quantity, regardless of whether they are
     * below their threshold. Used by the dashboard's "Low Stock" card as a
     * fallback when nothing is actually low — so the card still shows the
     * closest candidates instead of an empty table.
     */
    public List<Product> findLowestStock(int limit) {
        return query(SELECT_COLS + "ORDER BY p.quantity ASC, p.name ASC LIMIT ?",
                new Object[]{limit});
    }
}
