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
import java.util.List;
import java.util.Optional;

/**
 * Concise, clean JDBC implementation of {@link WishlistDAO}.
 *
 * @author Omar ElSharkawy (@omarehab544)
 */
public class WishlistDAOImpl implements WishlistDAO {

    private final DatabaseManager db;

    public WishlistDAOImpl() {
        this(DatabaseManager.getInstance());
    }

    public WishlistDAOImpl(DatabaseManager db) {
        this.db = db;
    }

    private static final String SELECT_JOIN =
            "SELECT w.id, w.user_id, w.item_id, w.notes, w.priority, w.target_amount, w.current_paid_amount, w.is_completed, " +
            "       i.name AS item_name, i.description AS item_desc, i.category AS item_cat, i.price AS item_price, i.image_url AS item_img " +
            "FROM wishlist_items w JOIN items i ON w.item_id = i.id ";

    @Override
    public List<WishlistItemDTO> getWishlistByUserId(int userId) throws SQLException {
        return db.queryList(SELECT_JOIN + "WHERE w.user_id = ? ORDER BY w.created_at DESC", this::mapWishlistItem, userId);
    }

    @Override
    public Optional<WishlistItemDTO> getWishlistItemById(int wishlistItemId) throws SQLException {
        return db.queryOne(SELECT_JOIN + "WHERE w.id = ?", this::mapWishlistItem, wishlistItemId);
    }

    @Override
    public WishlistItemDTO addItem(int userId, int itemId, BigDecimal targetAmount, String notes, String priority) throws SQLException {
        String sql = "INSERT INTO wishlist_items (user_id, item_id, target_amount, current_paid_amount, notes, priority, is_completed) VALUES (?, ?, ?, 0.00, ?, ?, FALSE)";
        String prio = (priority != null && !priority.isBlank()) ? priority.toUpperCase() : "MEDIUM";
        int id = db.insertAndGetId(sql, userId, itemId, targetAmount, notes, prio);
        return getWishlistItemById(id).orElseThrow(() -> new SQLException("Failed to retrieve added wishlist item"));
    }

    @Override
    public boolean updateItem(int wishlistItemId, BigDecimal targetAmount, String notes, String priority) throws SQLException {
        String sql = "UPDATE wishlist_items SET target_amount = ?, notes = ?, priority = ? WHERE id = ?";
        String prio = (priority != null && !priority.isBlank()) ? priority.toUpperCase() : "MEDIUM";
        return db.update(sql, targetAmount, notes, prio, wishlistItemId) > 0;
    }

    @Override
    public boolean removeItem(int wishlistItemId) throws SQLException {
        return db.update("DELETE FROM wishlist_items WHERE id = ?", wishlistItemId) > 0;
    }

    @Override
    public boolean updatePaidAmount(int wishlistItemId, BigDecimal additionalAmount, Connection conn) throws SQLException {
        String sql = "UPDATE wishlist_items SET current_paid_amount = current_paid_amount + ?, " +
                     "is_completed = CASE WHEN (current_paid_amount + ?) >= target_amount THEN TRUE ELSE FALSE END WHERE id = ?";
        try (PreparedStatement stmt = DatabaseManager.prepare(conn, sql, additionalAmount, additionalAmount, wishlistItemId)) {
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public int getOwnerUserId(int wishlistItemId) throws SQLException {
        return db.queryOne("SELECT user_id FROM wishlist_items WHERE id = ?", rs -> rs.getInt("user_id"), wishlistItemId)
                .orElseThrow(() -> new SQLException("Wishlist item " + wishlistItemId + " not found"));
    }

    private WishlistItemDTO mapWishlistItem(ResultSet rs) throws SQLException {
        ItemDTO item = new ItemDTO(rs.getInt("item_id"), rs.getString("item_name"), rs.getString("item_desc"),
                rs.getBigDecimal("item_price"), rs.getString("item_img"), rs.getString("item_cat"));
        return new WishlistItemDTO(rs.getInt("id"), rs.getInt("user_id"), item, rs.getBigDecimal("current_paid_amount"), rs.getBoolean("is_completed"));
    }
}
