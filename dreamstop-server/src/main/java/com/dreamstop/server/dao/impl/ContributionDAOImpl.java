package com.dreamstop.server.dao.impl;

import com.dreamstop.common.dto.ContributionDTO;
import com.dreamstop.common.model.NotificationType;
import com.dreamstop.server.dao.ContributionDAO;
import com.dreamstop.server.dao.ContributionResult;
import com.dreamstop.server.dao.NotificationDAO;
import com.dreamstop.server.database.DatabaseManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Concise, clean JDBC implementation of {@link ContributionDAO} managing ACID transactions,
 * excess refund logic, completion status, and buyer/receiver notifications.
 *
 * @author Omar ElSharkawy (@omarehab544)
 */
public class ContributionDAOImpl implements ContributionDAO {

    private final DatabaseManager db;
    private final NotificationDAO notificationDAO;

    public ContributionDAOImpl() {
        this(DatabaseManager.getInstance(), new NotificationDAOImpl());
    }

    public ContributionDAOImpl(DatabaseManager db, NotificationDAO notificationDAO) {
        this.db = db;
        this.notificationDAO = notificationDAO;
    }

    @Override
    public ContributionResult contribute(int contributorId, int wishlistItemId, BigDecimal amount) throws SQLException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ContributionResult.failure("Contribution amount must be positive.");
        }

        try {
            return db.runInTransaction(conn -> {
                // 1. Check contributor balance
                String userSql = "SELECT username, balance FROM users WHERE id = ? FOR UPDATE";
                String username;
                BigDecimal balance;
                try (PreparedStatement s = DatabaseManager.prepare(conn, userSql, contributorId);
                     ResultSet rs = s.executeQuery()) {
                    if (!rs.next()) return ContributionResult.failure("Contributor not found.");
                    username = rs.getString("username");
                    balance = rs.getBigDecimal("balance");
                }

                if (balance.compareTo(amount) < 0) {
                    return ContributionResult.failure("Insufficient wallet balance (" + balance + " EGP).");
                }

                // 2. Check wishlist item
                String itemSql = "SELECT w.user_id, w.target_amount, w.current_paid_amount, w.is_completed, i.name " +
                                 "FROM wishlist_items w JOIN items i ON w.item_id = i.id WHERE w.id = ? FOR UPDATE";
                int ownerId;
                BigDecimal target, current;
                boolean completed;
                String itemName;
                try (PreparedStatement s = DatabaseManager.prepare(conn, itemSql, wishlistItemId);
                     ResultSet rs = s.executeQuery()) {
                    if (!rs.next()) return ContributionResult.failure("Wishlist item not found.");
                    ownerId = rs.getInt("user_id");
                    target = rs.getBigDecimal("target_amount");
                    current = rs.getBigDecimal("current_paid_amount");
                    completed = rs.getBoolean("is_completed");
                    itemName = rs.getString("name");
                }

                if (completed || current.compareTo(target) >= 0) {
                    return ContributionResult.failure("Item is already 100% funded!");
                }

                // 3. Excess refund calculation
                BigDecimal needed = target.subtract(current);
                BigDecimal accepted = amount.compareTo(needed) > 0 ? needed : amount;
                BigDecimal refund = amount.subtract(accepted);

                // 4. Deduct balance & update wishlist item
                try (PreparedStatement s = DatabaseManager.prepare(conn, "UPDATE users SET balance = balance - ? WHERE id = ?", accepted, contributorId)) {
                    s.executeUpdate();
                }

                boolean itemDone = current.add(accepted).compareTo(target) >= 0;
                try (PreparedStatement s = DatabaseManager.prepare(conn,
                        "UPDATE wishlist_items SET current_paid_amount = current_paid_amount + ?, is_completed = ? WHERE id = ?",
                        accepted, itemDone, wishlistItemId)) {
                    s.executeUpdate();
                }

                // 5. Insert contribution
                int contribId = 0;
                try (PreparedStatement s = conn.prepareStatement("INSERT INTO contributions (contributor_id, wishlist_item_id, amount) VALUES (?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    DatabaseManager.setParams(s, contributorId, wishlistItemId, accepted);
                    s.executeUpdate();
                    try (ResultSet k = s.getGeneratedKeys()) { if (k.next()) contribId = k.getInt(1); }
                }

                // 6. Send notifications
                notificationDAO.create(ownerId, NotificationType.CONTRIBUTION_RECEIVED, "Contribution Received",
                        username + " contributed " + accepted + " EGP to \"" + itemName + "\"!", wishlistItemId, conn);

                if (itemDone) {
                    notificationDAO.create(ownerId, NotificationType.ITEM_COMPLETED_RECEIVER, "Gift Goal Reached!",
                            "Your wishlist item \"" + itemName + "\" has been 100% funded!", wishlistItemId, conn);

                    for (int buyerId : getUniqueContributorIdsTransactional(wishlistItemId, conn)) {
                        notificationDAO.create(buyerId, NotificationType.ITEM_COMPLETED_BUYER, "Gift Completed",
                                "The wishlist item \"" + itemName + "\" you contributed to is fully funded!", wishlistItemId, conn);
                    }
                }

                return ContributionResult.success(contribId, accepted, refund, itemDone);
            });
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("Transaction failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ContributionDTO> getContributionsByWishlistItem(int wishlistItemId) throws SQLException {
        String sql = "SELECT c.id, c.contributor_id, c.wishlist_item_id, c.amount, c.created_at, u.username " +
                     "FROM contributions c JOIN users u ON c.contributor_id = u.id WHERE c.wishlist_item_id = ? ORDER BY c.created_at DESC";
        return db.queryList(sql, this::mapContribution, wishlistItemId);
    }

    @Override
    public List<ContributionDTO> getContributionsByContributor(int contributorId) throws SQLException {
        String sql = "SELECT c.id, c.contributor_id, c.wishlist_item_id, c.amount, c.created_at, u.username " +
                     "FROM contributions c JOIN users u ON c.contributor_id = u.id WHERE c.contributor_id = ? ORDER BY c.created_at DESC";
        return db.queryList(sql, this::mapContribution, contributorId);
    }

    @Override
    public List<Integer> getUniqueContributorIds(int wishlistItemId) throws SQLException {
        try (Connection conn = db.getConnection()) {
            return getUniqueContributorIdsTransactional(wishlistItemId, conn);
        }
    }

    private List<Integer> getUniqueContributorIdsTransactional(int wishlistItemId, Connection conn) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT DISTINCT contributor_id FROM contributions WHERE wishlist_item_id = ?";
        try (PreparedStatement s = DatabaseManager.prepare(conn, sql, wishlistItemId);
             ResultSet rs = s.executeQuery()) {
            while (rs.next()) ids.add(rs.getInt("contributor_id"));
        }
        return ids;
    }

    private ContributionDTO mapContribution(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        return new ContributionDTO(
                rs.getInt("id"), rs.getInt("contributor_id"), rs.getString("username"),
                rs.getInt("wishlist_item_id"), rs.getBigDecimal("amount"),
                ts != null ? ts.toLocalDateTime() : LocalDateTime.now()
        );
    }
}
