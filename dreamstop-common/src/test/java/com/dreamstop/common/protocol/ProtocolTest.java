package com.dreamstop.common.protocol;

import com.dreamstop.common.dto.LoginRequestDTO;
import com.dreamstop.common.dto.UserDTO;
import com.dreamstop.common.model.NotificationType;
import com.dreamstop.common.model.RequestType;
import com.dreamstop.common.model.ResponseStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ProtocolTest {

    @Test
    @DisplayName("Protocol: Request packaging and payload extraction")
    void testRequestPackaging() {
        LoginRequestDTO credentials = new LoginRequestDTO("kady_x", "password123");
        Request req = Request.of(RequestType.LOGIN, "test-token-123", credentials);

        assertEquals(RequestType.LOGIN, req.getType());
        assertEquals("test-token-123", req.getToken());
        assertNotNull(req.getPayloadJson());

        LoginRequestDTO extracted = req.getPayloadAs(LoginRequestDTO.class);
        assertNotNull(extracted);
        assertEquals("kady_x", extracted.getUsernameOrEmail());
        assertEquals("password123", extracted.getPassword());
        assertTrue(req.toString().contains("LOGIN"));
    }

    @Test
    @DisplayName("Protocol: Response factory methods and status checks")
    void testResponseFactories() {
        UserDTO user = new UserDTO(1, "kady_x", "kady@dreamstop.com", new BigDecimal("75000.00"));

        Response successRes = Response.success(user, "User logged in successfully");
        assertTrue(successRes.isSuccess());
        assertEquals(ResponseStatus.SUCCESS, successRes.getStatus());
        assertEquals("User logged in successfully", successRes.getMessage());

        UserDTO extractedUser = successRes.getDataAs(UserDTO.class);
        assertNotNull(extractedUser);
        assertEquals("kady_x", extractedUser.getUsername());

        Response errRes = Response.error("Internal server error");
        assertFalse(errRes.isSuccess());
        assertEquals(ResponseStatus.ERROR, errRes.getStatus());

        Response badReq = Response.badRequest("Invalid input");
        assertEquals(ResponseStatus.BAD_REQUEST, badReq.getStatus());

        Response unauth = Response.unauthorized("Access denied");
        assertEquals(ResponseStatus.UNAUTHORIZED, unauth.getStatus());

        Response notFound = Response.notFound("Item not found");
        assertEquals(ResponseStatus.NOT_FOUND, notFound.getStatus());

        Response conflict = Response.conflict("Already exists");
        assertEquals(ResponseStatus.CONFLICT, conflict.getStatus());
    }

    @Test
    @DisplayName("Protocol: ServerNotification factory methods")
    void testServerNotificationFactories() {
        ServerNotification notif = ServerNotification.contributionReceived("Adham", "PS5 Pro", 10);
        assertEquals(NotificationType.CONTRIBUTION_RECEIVED, notif.getType());
        assertEquals(Integer.valueOf(10), notif.getRelatedWishlistItemId());
        assertTrue(notif.getMessage().contains("Adham"));
        assertNotNull(notif.getTimestamp());

        ServerNotification wishNotif = ServerNotification.wishlistUpdated("Updated", "Item modified", 5);
        assertEquals(NotificationType.WISHLIST_UPDATED, wishNotif.getType());
        assertEquals(Integer.valueOf(5), wishNotif.getRelatedWishlistItemId());

        ServerNotification profNotif = ServerNotification.profileUpdated("Profile Updated", "Name changed");
        assertEquals(NotificationType.PROFILE_UPDATED, profNotif.getType());
    }
}
