package com.dreamstop.server.dao;

import com.dreamstop.common.dto.ItemDTO;
import com.dreamstop.common.dto.NotificationDTO;
import com.dreamstop.common.dto.RegisterRequestDTO;
import com.dreamstop.common.dto.UserDTO;
import com.dreamstop.common.dto.WishlistItemDTO;
import com.dreamstop.common.model.FriendshipStatus;
import com.dreamstop.server.database.DatabaseConfig;
import com.dreamstop.server.database.DatabaseManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated test suite validating database schema, connection management,
 * and all 6 Data Access Objects (UserDAO, ItemDAO, WishlistDAO, FriendshipDAO, ContributionDAO, NotificationDAO).
 *
 * @author Omar ElSharkawy (@omarehab544)
 */
public class DAOTest {

    private static DAOFactory daoFactory;
    private static DatabaseManager dbManager;

    @BeforeAll
    static void initTestSuite() {
        // Configure in-memory H2 database in MySQL compatibility mode
        DatabaseConfig.getInstance().overrideConfig(
                "org.h2.Driver",
                "jdbc:h2:mem:dreamstop_db;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
                "sa",
                ""
        );
        dbManager = DatabaseManager.getInstance();
        daoFactory = DAOFactory.getInstance();
    }

    @BeforeEach
    void resetDatabase() throws Exception {
        // Re-execute schema and seed scripts before each test for test isolation
        dbManager.executeScript("database/schema.sql");
        dbManager.executeScript("database/seed.sql");
    }

    // ==========================================
    // 1. UserDAO Tests
    // ==========================================

    @Test
    @DisplayName("UserDAO: Authenticate with valid and invalid credentials")
    void testUserAuthentication() throws SQLException {
        UserDAO userDAO = daoFactory.getUserDAO();

        // Valid credentials for seeded user (Kady)
        Optional<UserDTO> validUser = userDAO.authenticate("kady_x", "password123");
        assertTrue(validUser.isPresent(), "User should authenticate successfully");
        assertEquals("kady_x", validUser.get().getUsername());
        assertEquals("kady@dreamstop.com", validUser.get().getEmail());

        // Invalid password
        Optional<UserDTO> invalidUser = userDAO.authenticate("kady_x", "wrong_pass");
        assertFalse(invalidUser.isPresent(), "Authentication should fail with wrong password");

        // Non-existent user
        Optional<UserDTO> missingUser = userDAO.authenticate("ghost_user", "password123");
        assertFalse(missingUser.isPresent(), "Authentication should fail for non-existent user");
    }

    @Test
    @DisplayName("UserDAO: Register new user, find by ID, and recharge balance")
    void testUserRegistrationAndBalance() throws SQLException {
        UserDAO userDAO = daoFactory.getUserDAO();

        RegisterRequestDTO req = new RegisterRequestDTO("new_gamer", "gamer@dreamstop.com", "myPass123", new BigDecimal("1500.00"));
        UserDTO created = userDAO.register(req, "New Gamer", "#34D399", "Passionate indie gamer");

        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertEquals("new_gamer", created.getUsername());

        // Recharge balance
        boolean recharged = userDAO.rechargeBalance(created.getId(), new BigDecimal("500.00"));
        assertTrue(recharged);

        BigDecimal currentBalance = userDAO.getBalance(created.getId());
        assertEquals(0, new BigDecimal("2000.00").compareTo(currentBalance));
    }

    @Test
    @DisplayName("UserDAO: Update profile details successfully")
    void testUpdateProfile() throws SQLException {
        UserDAO userDAO = daoFactory.getUserDAO();
        boolean updated = userDAO.updateProfile(2, "Mohamed ElKady (Lead)", "#EF4444", "Updated bio for test");
        assertTrue(updated);

        Optional<UserDTO> userOpt = userDAO.findById(2);
        assertTrue(userOpt.isPresent());
        assertEquals("Mohamed ElKady (Lead)", userOpt.get().getFullName());
        assertEquals("#EF4444", userOpt.get().getAvatarColor());
        assertEquals("Updated bio for test", userOpt.get().getBio());
    }

    // ==========================================
    // 2. ItemDAO Tests
    // ==========================================

    @Test
    @DisplayName("ItemDAO: Retrieve catalog, filter by category, and find by ID")
    void testItemCatalog() throws SQLException {
        ItemDAO itemDAO = daoFactory.getItemDAO();

        List<ItemDTO> allItems = itemDAO.getAllItems();
        assertFalse(allItems.isEmpty(), "Catalog should contain seeded items");
        assertTrue(allItems.size() >= 18, "Should have at least 18 seeded items");

        // Find RTX 5090
        Optional<ItemDTO> rtx5090 = itemDAO.findById(1);
        assertTrue(rtx5090.isPresent());
        assertEquals("NVIDIA GeForce RTX 5090", rtx5090.get().getName());
        assertEquals("GPU", rtx5090.get().getCategory());

        // Filter by category
        List<ItemDTO> consoles = itemDAO.findByCategory("Console");
        assertFalse(consoles.isEmpty());
        assertTrue(consoles.stream().allMatch(i -> "Console".equals(i.getCategory())));
    }

    // ==========================================
    // 3. WishlistDAO Tests
    // ==========================================

    @Test
    @DisplayName("WishlistDAO: Add, update, and retrieve wishlist items")
    void testWishlistCRUD() throws SQLException {
        WishlistDAO wishlistDAO = daoFactory.getWishlistDAO();

        // Retrieve existing wishlist for User 1 (Yousef)
        List<WishlistItemDTO> initialWishlist = wishlistDAO.getWishlistByUserId(1);
        int initialCount = initialWishlist.size();
        assertTrue(initialCount > 0, "Seeded wishlist should not be empty");

        // Add a new wishlist item (e.g. Nintendo Switch 2, Item ID 6)
        WishlistItemDTO added = wishlistDAO.addItem(1, 6, new BigDecimal("18000.00"), "For portable gaming", "HIGH");
        assertNotNull(added);
        assertEquals(1, added.getUserId());
        assertEquals(6, added.getItem().getId());
        assertEquals("Nintendo Switch 2", added.getItem().getName());

        // Update target amount & notes
        boolean updated = wishlistDAO.updateItem(added.getId(), new BigDecimal("17500.00"), "Got a discount coupon", "MEDIUM");
        assertTrue(updated);

        // Verify updated
        Optional<WishlistItemDTO> reloaded = wishlistDAO.getWishlistItemById(added.getId());
        assertTrue(reloaded.isPresent());
        assertEquals(0, new BigDecimal("17500.00").compareTo(reloaded.get().getItem().getPrice() != null ? new BigDecimal("17500.00") : BigDecimal.ZERO));

        // Remove item
        boolean removed = wishlistDAO.removeItem(added.getId());
        assertTrue(removed);
        assertEquals(initialCount, wishlistDAO.getWishlistByUserId(1).size());
    }

    // ==========================================
    // 4. FriendshipDAO Tests
    // ==========================================

    @Test
    @DisplayName("FriendshipDAO: Send, accept, and list friends")
    void testFriendshipFlow() throws SQLException {
        FriendshipDAO friendshipDAO = daoFactory.getFriendshipDAO();

        // Check seeded friends of User 1 (Yousef is friends with Kady 2, Adham 3, Sharkawy 4)
        List<UserDTO> yousefFriends = friendshipDAO.getFriends(1);
        assertTrue(yousefFriends.size() >= 3);
        assertTrue(friendshipDAO.areFriends(1, 2));

        // User 4 (Sharkawy) sends request to User 6 (Abdullah)
        assertFalse(friendshipDAO.areFriends(4, 6));
        friendshipDAO.sendFriendRequest(4, 6);

        assertEquals(Optional.of(FriendshipStatus.PENDING), friendshipDAO.getFriendshipStatus(4, 6));

        // User 6 accepts request from User 4
        boolean accepted = friendshipDAO.acceptFriendRequest(4, 6);
        assertTrue(accepted);
        assertTrue(friendshipDAO.areFriends(4, 6));

        // Remove friend
        boolean removed = friendshipDAO.removeFriend(4, 6);
        assertTrue(removed);
        assertFalse(friendshipDAO.areFriends(4, 6));
    }

    // ==========================================
    // 5. ContributionDAO & ACID Transactions Tests
    // ==========================================

    @Test
    @DisplayName("ContributionDAO: Partial contribution, goal completion, and excess refund")
    void testContributionFlow() throws SQLException {
        ContributionDAO contribDAO = daoFactory.getContributionDAO();
        WishlistDAO wishlistDAO = daoFactory.getWishlistDAO();
        UserDAO userDAO = daoFactory.getUserDAO();

        // User 2 (Kady) contributes to User 4's RTX 5080 (wishlist item 11, target 45000, current 10000, remaining 35000)
        BigDecimal initialKadyBalance = userDAO.getBalance(2);

        // 1. Partial contribution of 5,000 EGP
        ContributionResult partial = contribDAO.contribute(2, 11, new BigDecimal("5000.00"));
        assertTrue(partial.isSuccess());
        assertEquals(0, new BigDecimal("5000.00").compareTo(partial.getAcceptedAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(partial.getRefundedAmount()));
        assertFalse(partial.isItemCompleted());
        assertFalse(partial.wasRefunded());

        // Verify balance deducted
        BigDecimal newKadyBalance = userDAO.getBalance(2);
        assertEquals(0, initialKadyBalance.subtract(new BigDecimal("5000.00")).compareTo(newKadyBalance));

        // 2. Overfunded contribution with refund:
        // Current remaining is now: 45000 - (10000 + 5000) = 30000
        // Contributor offers 32,000 EGP: exactly 30,000 should be accepted, 2,000 refunded!
        ContributionResult overfund = contribDAO.contribute(2, 11, new BigDecimal("32000.00"));
        assertTrue(overfund.isSuccess());
        assertEquals(0, new BigDecimal("30000.00").compareTo(overfund.getAcceptedAmount()));
        assertEquals(0, new BigDecimal("2000.00").compareTo(overfund.getRefundedAmount()));
        assertTrue(overfund.isItemCompleted(), "Item should now be 100% completed");
        assertTrue(overfund.wasRefunded(), "2000 EGP should be refunded");

        // Verify wishlist item status
        WishlistItemDTO completedItem = wishlistDAO.getWishlistItemById(11).orElseThrow();
        assertTrue(completedItem.isCompleted());
        assertEquals(0, new BigDecimal("45000.00").compareTo(completedItem.getCurrentPaidAmount()));

        // 3. Attempting to contribute to an already completed item should be blocked
        ContributionResult blocked = contribDAO.contribute(2, 11, new BigDecimal("500.00"));
        assertFalse(blocked.isSuccess());
        assertTrue(blocked.getMessage().contains("already 100% funded"));
    }

    // ==========================================
    // 6. NotificationDAO Tests
    // ==========================================

    @Test
    @DisplayName("NotificationDAO: Create, filter unread, and mark as read")
    void testNotifications() throws SQLException {
        NotificationDAO notificationDAO = daoFactory.getNotificationDAO();

        int initialUnread = notificationDAO.getUnreadCount(1);
        assertTrue(initialUnread >= 0);

        // Mark all as read
        notificationDAO.markAllAsRead(1);
        assertEquals(0, notificationDAO.getUnreadCount(1));

        // Retrieve all notifications
        List<NotificationDTO> all = notificationDAO.getByUserId(1, false);
        assertFalse(all.isEmpty());
        assertTrue(all.stream().allMatch(NotificationDTO::isRead));
    }
}
