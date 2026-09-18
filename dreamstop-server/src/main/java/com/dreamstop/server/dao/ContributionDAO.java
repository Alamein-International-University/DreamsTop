package com.dreamstop.server.dao;

import com.dreamstop.common.dto.ContributionDTO;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object interface for financial contributions toward friend wishlist items.
 *
 * @author Omar ElSharkawy (@omarehab544)
 */
public interface ContributionDAO {

    /**
     * Executes an atomic, transactional contribution toward a friend's wishlist item.
     * Automatically handles:
     * - Balance verification and deduction
     * - Capping contribution to remaining target and refunding excess
     * - Incrementing wishlist paid amount
     * - Marking item completed if target is met
     * - Creating in-app notifications for receiver and buyers
     */
    ContributionResult contribute(int contributorId, int wishlistItemId, BigDecimal amount) throws SQLException;

    /**
     * Retrieves all contributions recorded for a specific wishlist item.
     */
    List<ContributionDTO> getContributionsByWishlistItem(int wishlistItemId) throws SQLException;

    /**
     * Retrieves all contributions made by a specific user.
     */
    List<ContributionDTO> getContributionsByContributor(int contributorId) throws SQLException;

    /**
     * Retrieves unique user IDs of all friends who contributed to a specific wishlist item.
     */
    List<Integer> getUniqueContributorIds(int wishlistItemId) throws SQLException;
}
