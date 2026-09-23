package com.dreamstop.common.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class DTOTest {

    @Test
    @DisplayName("DTO: WishlistItemDTO getters, setters, and state")
    void testWishlistItemDTO() {
        ItemDTO catalogItem = new ItemDTO(1, "PS5 Pro", "Gaming Console", new BigDecimal("30000.00"), null, "Gaming");

        WishlistItemDTO item = new WishlistItemDTO(
                101,
                1,
                catalogItem,
                new BigDecimal("30000.00"),
                new BigDecimal("15000.00"),
                "White color",
                "HIGH",
                false
        );

        assertEquals(101, item.getId());
        assertEquals(1, item.getUserId());
        assertNotNull(item.getItem());
        assertEquals("PS5 Pro", item.getItem().getName());
        assertEquals(new BigDecimal("30000.00"), item.getTargetAmount());
        assertEquals(new BigDecimal("15000.00"), item.getCurrentPaidAmount());
        assertEquals("White color", item.getNotes());
        assertEquals("HIGH", item.getPriority());
        assertFalse(item.isCompleted());

        // Update state via setters
        item.setCompleted(true);
        item.setCurrentPaidAmount(new BigDecimal("30000.00"));
        assertTrue(item.isCompleted());
        assertEquals(new BigDecimal("30000.00"), item.getCurrentPaidAmount());
    }

    @Test
    @DisplayName("DTO: UserDTO getters, setters, and properties")
    void testUserDTO() {
        UserDTO user = new UserDTO(1, "kady_x", "kady@dreamstop.com", new BigDecimal("500.00"), "Mohamed ElKady", "#6366F1", "Software Engineer");

        assertEquals(1, user.getId());
        assertEquals("kady_x", user.getUsername());
        assertEquals("kady@dreamstop.com", user.getEmail());
        assertEquals(new BigDecimal("500.00"), user.getBalance());
        assertEquals("Mohamed ElKady", user.getFullName());
        assertEquals("#6366F1", user.getAvatarColor());
        assertEquals("Software Engineer", user.getBio());

        user.setBalance(new BigDecimal("1500.00"));
        assertEquals(new BigDecimal("1500.00"), user.getBalance());
    }

    @Test
    @DisplayName("DTO: Entity equality based on ID")
    void testEntityEquality() {
        UserDTO user1 = new UserDTO(1, "kady", "kady@mail.com", BigDecimal.ZERO);
        UserDTO user2 = new UserDTO(1, "kady_renamed", "kady2@mail.com", BigDecimal.TEN);
        UserDTO user3 = new UserDTO(2, "adham", "adham@mail.com", BigDecimal.ZERO);

        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());

        ItemDTO item1 = new ItemDTO(5, "Item A", "Desc", BigDecimal.TEN, null, "Cat");
        ItemDTO item2 = new ItemDTO(5, "Item A renamed", "Desc", BigDecimal.ONE, null, "Cat");
        assertEquals(item1, item2);
    }
}
