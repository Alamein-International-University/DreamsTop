package com.dreamstop;

import com.dreamstop.model.FriendRequest;
import com.dreamstop.model.Item;
import com.dreamstop.model.User;
import com.dreamstop.model.WishlistItem;
import com.dreamstop.service.FriendService;
import com.dreamstop.service.WishlistService;
import com.dreamstop.service.ContributionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TasksTest {

    private FriendService friendService;
    private WishlistService wishlistService;

    @BeforeEach
    void setUp() {
        friendService = FriendService.getInstance();
        friendService.refreshState();
        wishlistService = WishlistService.getInstance();
        wishlistService.refreshMyWishlist();
    }

    @Test
    @DisplayName("Task 5: View my Friends list")
    void testViewFriendsList() {
        var friends = friendService.getFriends();
        assertNotNull(friends);
        assertTrue(friends.size() >= 2, "Current user should have initial friends");

        boolean hasKady = friends.stream().anyMatch(f -> f.getUsername().equals("kady_x"));
        assertTrue(hasKady, "Mohamed ElKady should be in friends list");
    }

    @Test
    @DisplayName("Task 2: Add & Remove Friend")
    void testAddAndRemoveFriend() {
        // 1. Search for a non-friend user
        List<User> discoverable = friendService.searchDiscoverableUsers("omar");
        assertFalse(discoverable.isEmpty(), "Should find user matching 'omar'");

        User target = discoverable.get(0);

        // 2. Send friend request (Task 2: Add)
        boolean sent = friendService.sendFriendRequest(target);
        assertTrue(sent, "Friend request should be sent successfully");
        assertTrue(friendService.hasPendingRequestWith(target));

        // 3. Test removing friend (Task 2: Remove)
        User friendToRemove = friendService.getFriends().get(0);
        int initialCount = friendService.getFriends().size();
        boolean removed = friendService.removeFriend(friendToRemove);
        assertTrue(removed, "Friend should be removed");
        assertEquals(initialCount - 1, friendService.getFriends().size());
        assertFalse(friendService.getFriends().contains(friendToRemove));
    }

    @Test
    @DisplayName("Task 3: Accept & Decline Friend Request")
    void testAcceptAndDeclineFriendRequest() {
        var incoming = friendService.getIncomingRequests();
        assertFalse(incoming.isEmpty(), "Should have incoming pending friend requests");

        FriendRequest reqToAccept = incoming.get(0);
        User sender = reqToAccept.getSender();
        int initialFriendsCount = friendService.getFriends().size();

        // 1. Accept Request
        boolean accepted = friendService.acceptFriendRequest(reqToAccept);
        assertTrue(accepted, "Request should be accepted");
        assertEquals(FriendRequest.Status.ACCEPTED, reqToAccept.getStatus());
        assertTrue(friendService.getFriends().contains(sender), "Sender should now be in friends list");
        assertEquals(initialFriendsCount + 1, friendService.getFriends().size());

        // 2. Decline Request
        if (!incoming.isEmpty()) {
            FriendRequest reqToDecline = incoming.get(0);
            int remainingIncoming = incoming.size();
            boolean declined = friendService.declineFriendRequest(reqToDecline);
            assertTrue(declined, "Request should be declined");
            assertEquals(FriendRequest.Status.DECLINED, reqToDecline.getStatus());
            assertEquals(remainingIncoming - 1, incoming.size());
        }
    }

    @Test
    @DisplayName("Task 4: Create, Update, Delete my Wish List")
    void testWishlistCRUD() {
        var myWishlist = wishlistService.getMyWishlist();
        int initialCount = myWishlist.size();

        // 1. Create (Add) Item
        Item catalogItem = wishlistService.getCatalog().get(3); // Sony Headphones
        WishlistItem created = wishlistService.addItemToMyWishlist(catalogItem, "Looking forward to this!", 12000.0, "HIGH");
        assertNotNull(created);
        assertEquals(initialCount + 1, myWishlist.size());
        assertEquals("Looking forward to this!", created.getNotes());
        assertEquals(12000.0, created.getTargetAmount());
        assertEquals(0.0, created.getCurrentAmount());
        assertFalse(created.isCompleted());

        // 2. Update Item
        boolean updated = wishlistService.updateWishlistItem(created, "Updated note: Black color please", 11500.0, "MEDIUM");
        assertTrue(updated, "Item should update successfully");
        assertEquals("Updated note: Black color please", created.getNotes());
        assertEquals(11500.0, created.getTargetAmount());
        assertEquals("MEDIUM", created.getPriority());

        // 3. Delete Item
        boolean deleted = wishlistService.deleteWishlistItem(created);
        assertTrue(deleted, "Item should be deleted");
        assertEquals(initialCount, myWishlist.size());
        assertFalse(myWishlist.contains(created));

        // 4. Create Custom Written Item with Custom Amount
        WishlistItem customItem = wishlistService.addCustomItemToMyWishlist(
                "Keychron Q1 Pro Mechanical Keyboard",
                "Custom mechanical keyboard with tactile switches",
                "Peripherals",
                7500.0,
                "⌨️",
                "HIGH",
                "Fully assembled carbon black version"
        );
        assertNotNull(customItem);
        assertEquals("Keychron Q1 Pro Mechanical Keyboard", customItem.getItem().getName());
        assertEquals(7500.0, customItem.getTargetAmount());
        assertEquals("⌨️", customItem.getItem().getIconEmoji());
        assertEquals("Peripherals", customItem.getItem().getCategory());
        assertTrue(myWishlist.contains(customItem));

        // Cleanup custom item
        wishlistService.deleteWishlistItem(customItem);
    }

    @Test
    @DisplayName("Task 6: View my Friends Wish List + contribution cap & refund")
    void testViewFriendWishlist() {
        User kady = friendService.getFriends().stream()
                .filter(f -> f.getUsername().equals("kady_x"))
                .findFirst()
                .orElse(null);
        assertNotNull(kady, "Friend Kady should exist");

        var friendWishlist = wishlistService.getFriendWishlist(kady);
        assertFalse(friendWishlist.isEmpty(), "Friend should have wishlist items");

        WishlistItem item = friendWishlist.get(0);
        assertTrue(item.getTargetAmount() > 0);
        assertTrue(item.getProgressPercentage() >= 0 && item.getProgressPercentage() <= 100);

        // 1. Partial contribution — should be accepted in full, no refund
        ContributionResult partial = wishlistService.contributeToFriendItem(item, 100.0);
        assertNotNull(partial, "Partial contribution should succeed");
        assertEquals(100.0, partial.acceptedAmount(), 0.01);
        assertEquals(0.0,   partial.refundedAmount(), 0.01);
        assertFalse(partial.wasRefunded());

        // 2. Contribution that exactly reaches the goal — no refund
        double nowRemaining = item.getRemainingAmount();
        ContributionResult exact = wishlistService.contributeToFriendItem(item, nowRemaining);
        assertNotNull(exact, "Exact-goal contribution should succeed");
        assertEquals(nowRemaining, exact.acceptedAmount(), 0.01);
        assertEquals(0.0,          exact.refundedAmount(), 0.01);
        assertTrue(item.isCompleted(), "Item should be completed after exact contribution");

        // 3. Contribution after goal is already met — should return null (blocked)
        ContributionResult blocked = wishlistService.contributeToFriendItem(item, 500.0);
        assertNull(blocked, "Contribution to a completed item must return null");

        // 4. Over-goal contribution on a fresh item — excess should be refunded
        WishlistItem item2 = friendWishlist.stream()
                .filter(i -> !i.isCompleted())
                .findFirst()
                .orElse(null);
        if (item2 != null) {
            double over = item2.getRemainingAmount() + 999.0;
            ContributionResult refunded = wishlistService.contributeToFriendItem(item2, over);
            assertNotNull(refunded, "Over-goal contribution should return a result");
            assertEquals(item2.getTargetAmount(), item2.getCurrentAmount(), 0.01);
            assertTrue(refunded.wasRefunded(), "Excess should be flagged as refunded");
            assertEquals(999.0, refunded.refundedAmount(), 0.01);
        }
    }
}
