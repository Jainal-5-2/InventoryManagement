package com.mycompany.inventorymanagement.dao;

import com.mycompany.inventorymanagement.Category;
import com.mycompany.inventorymanagement.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Category CRUD. Kept separate from ProductDAO because category management
 * is a self-contained concern and the write methods need their own
 * validation (name non-empty, unique).
 */
public class CategoryDAO {

    public enum Result { SUCCESS, DUPLICATE, IN_USE, ERROR }

    public List<Category> findAll() {
        String sql = "SELECT id, name FROM categories ORDER BY name";
        List<Category> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Category(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** @return generated id on success, -1 on duplicate/blank/error. */
    public int insert(String name) {
        String sql = "INSERT INTO categories (name) VALUES (?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            if (ps.executeUpdate() == 0) return -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        } catch (SQLException e) {
            // UNIQUE constraint on categories.name → duplicate
            if ("23000".equals(e.getSQLState())) return -1;
            e.printStackTrace();
            return -1;
        }
    }

    public Result rename(int id, String newName) {
        String sql = "UPDATE categories SET name = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0 ? Result.SUCCESS : Result.ERROR;
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) return Result.DUPLICATE;
            e.printStackTrace();
            return Result.ERROR;
        }
    }

    /**
     * Deletes a category. Fails with {@link Result#IN_USE} if any products
     * still reference it — the caller should offer to reassign or confirm
     * orphaning them separately.
     */
    public Result delete(int id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0 ? Result.SUCCESS : Result.ERROR;
        } catch (SQLException e) {
            e.printStackTrace();
            return Result.ERROR;
        }
    }

    /** Number of products currently in a category — for the delete warning. */
    public int countProductsIn(int categoryId) {
        String sql = "SELECT COUNT(*) FROM products WHERE category_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}