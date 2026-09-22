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
 * Standard JDBC implementation of {@link ContributionDAO}.
 * Manages ACID transactions, balance checks, excess refunds, and notifications.
 */
public class ContributionDAOImpl implements ContributionDAO {

    private final NotificationDAO notificationDAO;

    public ContributionDAOImpl() {
        this(new NotificationDAOImpl());
    }

    public ContributionDAOImpl(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    public ContributionDAOImpl(DatabaseManager db, NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    @Override
    public ContributionResult contribute(int contributorId, int wishlistItemId, BigDecimal amount) throws SQLException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ContributionResult.failure("Contribution amount must be positive.");
        }

        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            // 1. Check contributor balance
            String userSql = "SELECT username, balance FROM users WHERE id = ? FOR UPDATE";
            String username;
            BigDecimal balance;
            try (PreparedStatement stmt = conn.prepareStatement(userSql)) {
                stmt.setInt(1, contributorId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return ContributionResult.failure("Contributor not found.");
                    }
                    username = rs.getString("username");
                    balance = rs.getBigDecimal("balance");
                }
            }

            if (balance.compareTo(amount) < 0) {
                conn.rollback();
                return ContributionResult.failure("Insufficient wallet balance (" + balance + " EGP).");
            }

            // 2. Check wishlist item
            String itemSql = "SELECT w.user_id, w.target_amount, w.current_paid_amount, w.is_completed, i.name " +
                             "FROM wishlist_items w JOIN items i ON w.item_id = i.id WHERE w.id = ? FOR UPDATE";
            int ownerId;
            BigDecimal targetAmount;
            BigDecimal currentPaid;
            boolean isCompleted;
            String itemName;
            try (PreparedStatement stmt = conn.prepareStatement(itemSql)) {
                stmt.setInt(1, wishlistItemId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return ContributionResult.failure("Wishlist item not found.");
                    }
                    ownerId = rs.getInt("user_id");
                    targetAmount = rs.getBigDecimal("target_amount");
                    currentPaid = rs.getBigDecimal("current_paid_amount");
                    isCompleted = rs.getBoolean("is_completed");
                    itemName = rs.getString("name");
                }
            }

            if (isCompleted || currentPaid.compareTo(targetAmount) >= 0) {
                conn.rollback();
                return ContributionResult.failure("Item is already 100% funded!");
            }

            // 3. Excess refund calculation
            BigDecimal remainingNeeded = targetAmount.subtract(currentPaid);
            BigDecimal acceptedAmount = amount.compareTo(remainingNeeded) > 0 ? remainingNeeded : amount;
            BigDecimal refundedAmount = amount.subtract(acceptedAmount);

            // 4. Deduct contributor balance
            String deductSql = "UPDATE users SET balance = balance - ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deductSql)) {
                stmt.setBigDecimal(1, acceptedAmount);
                stmt.setInt(2, contributorId);
                stmt.executeUpdate();
            }

            // 5. Update wishlist item paid amount & completion status
            boolean itemCompleted = currentPaid.add(acceptedAmount).compareTo(targetAmount) >= 0;
            String updateItemSql = "UPDATE wishlist_items SET current_paid_amount = current_paid_amount + ?, is_completed = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateItemSql)) {
                stmt.setBigDecimal(1, acceptedAmount);
                stmt.setBoolean(2, itemCompleted);
                stmt.setInt(3, wishlistItemId);
                stmt.executeUpdate();
            }

            // 6. Insert contribution record
            int contributionId = 0;
            String insertSql = "INSERT INTO contributions (contributor_id, wishlist_item_id, amount) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, contributorId);
                stmt.setInt(2, wishlistItemId);
                stmt.setBigDecimal(3, acceptedAmount);
                stmt.executeUpdate();
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        contributionId = keys.getInt(1);
                    }
                }
            }

            // 7. Create in-app notifications
            notificationDAO.create(ownerId, NotificationType.CONTRIBUTION_RECEIVED, "Contribution Received",
                    username + " contributed " + acceptedAmount + " EGP to \"" + itemName + "\"!", wishlistItemId, conn);

            if (itemCompleted) {
                notificationDAO.create(ownerId, NotificationType.ITEM_COMPLETED_RECEIVER, "Gift Goal Reached!",
                        "Your wishlist item \"" + itemName + "\" has been 100% funded!", wishlistItemId, conn);

                List<Integer> buyerIds = getUniqueContributorIds(wishlistItemId, conn);
                for (int buyerId : buyerIds) {
                    notificationDAO.create(buyerId, NotificationType.ITEM_COMPLETED_BUYER, "Gift Completed",
                            "The wishlist item \"" + itemName + "\" you contributed to is fully funded!", wishlistItemId, conn);
                }
            }

            // Commit transaction
            conn.commit();
            return ContributionResult.success(contributionId, acceptedAmount, refundedAmount, itemCompleted);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    // ignore rollback exception
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    // ignore close exception
                }
            }
        }
    }

    @Override
    public List<ContributionDTO> getContributionsByWishlistItem(int wishlistItemId) throws SQLException {
        List<ContributionDTO> list = new ArrayList<>();
        String sql = "SELECT c.id, c.contributor_id, c.wishlist_item_id, c.amount, c.created_at, u.username " +
                     "FROM contributions c JOIN users u ON c.contributor_id = u.id WHERE c.wishlist_item_id = ? ORDER BY c.created_at DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wishlistItemId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapContribution(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<ContributionDTO> getContributionsByContributor(int contributorId) throws SQLException {
        List<ContributionDTO> list = new ArrayList<>();
        String sql = "SELECT c.id, c.contributor_id, c.wishlist_item_id, c.amount, c.created_at, u.username " +
                     "FROM contributions c JOIN users u ON c.contributor_id = u.id WHERE c.contributor_id = ? ORDER BY c.created_at DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, contributorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapContribution(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Integer> getUniqueContributorIds(int wishlistItemId) throws SQLException {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            return getUniqueContributorIds(wishlistItemId, conn);
        }
    }

    private List<Integer> getUniqueContributorIds(int wishlistItemId, Connection conn) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT DISTINCT contributor_id FROM contributions WHERE wishlist_item_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wishlistItemId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("contributor_id"));
                }
            }
        }
        return ids;
    }

    private ContributionDTO mapContribution(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        return new ContributionDTO(
                rs.getInt("id"),
                rs.getInt("contributor_id"),
                rs.getString("username"),
                rs.getInt("wishlist_item_id"),
                rs.getBigDecimal("amount"),
                ts != null ? ts.toLocalDateTime() : LocalDateTime.now()
        );
    }
}
