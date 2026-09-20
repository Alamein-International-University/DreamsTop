package com.dreamstop.server.dao;

import com.dreamstop.common.dto.WishlistItemDTO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for managing user wishlist items.
 *
 * @author Omar ElSharkawy (@omarehab544)
 */
public interface WishlistDAO {

    /**
     * Retrieves all wishlist items for a given user.
     */
    List<WishlistItemDTO> getWishlistByUserId(int userId) throws SQLException;

    /**
     * Retrieves a single wishlist item by its ID (including populated ItemDTO details).
     */
    Optional<WishlistItemDTO> getWishlistItemById(int wishlistItemId) throws SQLException;

    /**
     * Adds an item to a user's wishlist.
     */
    WishlistItemDTO addItem(int userId, int itemId, BigDecimal targetAmount, String notes, String priority) throws SQLException;

    /**
     * Updates an existing wishlist item's target amount, notes, and priority.
     */
    boolean updateItem(int wishlistItemId, BigDecimal targetAmount, String notes, String priority) throws SQLException;

    /**
     * Removes an item from a user's wishlist.
     */
    boolean removeItem(int wishlistItemId) throws SQLException;

    /**
     * Updates the paid amount of a wishlist item and marks it completed if target is reached.
     * Run within an ongoing transaction.
     */
    boolean updatePaidAmount(int wishlistItemId, BigDecimal additionalAmount, Connection conn) throws SQLException;

    /**
     * Gets the owner user ID of a wishlist item.
     */
    int getOwnerUserId(int wishlistItemId) throws SQLException;
}
