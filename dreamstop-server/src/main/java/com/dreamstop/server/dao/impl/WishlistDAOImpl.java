package com.dreamstop.server.dao.impl;

import com.dreamstop.common.dto.ItemDTO;
import com.dreamstop.common.dto.WishlistItemDTO;
import com.dreamstop.server.dao.WishlistDAO;
import com.dreamstop.server.database.DatabaseManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Standard JDBC implementation of {@link WishlistDAO}.
 */
public class WishlistDAOImpl implements WishlistDAO {

    public WishlistDAOImpl() {
    }

    public WishlistDAOImpl(DatabaseManager db) {
    }

    private static final String SELECT_JOIN =
            "SELECT w.id, w.user_id, w.item_id, w.notes, w.priority, w.target_amount, w.current_paid_amount, w.is_completed, " +
            "       i.name AS item_name, i.description AS item_desc, i.category AS item_cat, i.price AS item_price, i.image_url AS item_img " +
            "FROM wishlist_items w JOIN items i ON w.item_id = i.id ";

    @Override
    public List<WishlistItemDTO> getWishlistByUserId(int userId) throws SQLException {
        List<WishlistItemDTO> list = new ArrayList<>();
        String sql = SELECT_JOIN + "WHERE w.user_id = ? ORDER BY w.created_at DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapWishlistItem(rs));
                }
            }
        }
        return list;
    }

    @Override
    public Optional<WishlistItemDTO> getWishlistItemById(int wishlistItemId) throws SQLException {
        String sql = SELECT_JOIN + "WHERE w.id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wishlistItemId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapWishlistItem(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public WishlistItemDTO addItem(int userId, int itemId, BigDecimal targetAmount, String notes, String priority) throws SQLException {
        String sql = "INSERT INTO wishlist_items (user_id, item_id, target_amount, current_paid_amount, notes, priority, is_completed) VALUES (?, ?, ?, 0.00, ?, ?, FALSE)";
        String prio = (priority != null && !priority.isBlank()) ? priority.toUpperCase() : "MEDIUM";
        int id = 0;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, itemId);
            stmt.setBigDecimal(3, targetAmount);
            stmt.setString(4, notes);
            stmt.setString(5, prio);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    id = keys.getInt(1);
                }
            }
        }

        return getWishlistItemById(id).orElseThrow(() -> new SQLException("Failed to retrieve added wishlist item"));
    }

    @Override
    public boolean updateItem(int wishlistItemId, BigDecimal targetAmount, String notes, String priority) throws SQLException {
        String sql = "UPDATE wishlist_items SET target_amount = ?, notes = ?, priority = ? WHERE id = ?";
        String prio = (priority != null && !priority.isBlank()) ? priority.toUpperCase() : "MEDIUM";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, targetAmount);
            stmt.setString(2, notes);
            stmt.setString(3, prio);
            stmt.setInt(4, wishlistItemId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean removeItem(int wishlistItemId) throws SQLException {
        String sql = "DELETE FROM wishlist_items WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wishlistItemId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updatePaidAmount(int wishlistItemId, BigDecimal additionalAmount, Connection conn) throws SQLException {
        String sql = "UPDATE wishlist_items SET current_paid_amount = current_paid_amount + ?, " +
                     "is_completed = CASE WHEN (current_paid_amount + ?) >= target_amount THEN TRUE ELSE FALSE END WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, additionalAmount);
            stmt.setBigDecimal(2, additionalAmount);
            stmt.setInt(3, wishlistItemId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public int getOwnerUserId(int wishlistItemId) throws SQLException {
        String sql = "SELECT user_id FROM wishlist_items WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wishlistItemId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        }
        throw new SQLException("Wishlist item " + wishlistItemId + " not found");
    }

    private WishlistItemDTO mapWishlistItem(ResultSet rs) throws SQLException {
        ItemDTO item = new ItemDTO(
                rs.getInt("item_id"),
                rs.getString("item_name"),
                rs.getString("item_desc"),
                rs.getBigDecimal("item_price"),
                rs.getString("item_img"),
                rs.getString("item_cat")
        );
        return new WishlistItemDTO(
                rs.getInt("id"),
                rs.getInt("user_id"),
                item,
                rs.getBigDecimal("target_amount"),
                rs.getBigDecimal("current_paid_amount"),
                rs.getString("notes"),
                rs.getString("priority"),
                rs.getBoolean("is_completed")
        );
    }
}
