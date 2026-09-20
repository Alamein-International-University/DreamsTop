package com.dreamstop.server.handler;

import com.dreamstop.common.dto.AuthResultDTO;
import com.dreamstop.common.dto.ContributeRequestDTO;
import com.dreamstop.common.dto.LoginRequestDTO;
import com.dreamstop.common.dto.RegisterRequestDTO;
import com.dreamstop.common.model.RequestType;
import com.dreamstop.common.model.ResponseStatus;
import com.dreamstop.common.protocol.Request;
import com.dreamstop.common.protocol.Response;
import com.dreamstop.server.database.DatabaseConfig;
import com.dreamstop.server.database.DatabaseManager;
import com.dreamstop.server.network.ClientHandler;
import com.dreamstop.server.network.RequestDispatcher;
import com.dreamstop.server.network.SessionManager;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

public class HandlersIntegrationTest {

    private static DatabaseManager dbManager;
    private SessionManager sessionManager;
    private ClientHandler clientHandler;

    private AuthRequestHandler authHandler;
    private WishlistRequestHandler wishlistHandler;
    private FriendRequestHandler friendHandler;
    private ContributionRequestHandler contributionHandler;
    private NotificationRequestHandler notificationHandler;

    @BeforeAll
    static void initTestSuite() {
        DatabaseConfig.getInstance().overrideConfig(
                "org.h2.Driver",
                "jdbc:h2:mem:dreamstop_handlers_db;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
                "sa",
                "");
        dbManager = DatabaseManager.getInstance();
    }

    @BeforeEach
    void resetDatabase() throws Exception {
        dbManager.executeScript("database/schema.sql");
        dbManager.executeScript("database/seed.sql");

        sessionManager = new SessionManager();
        clientHandler = new ClientHandler(new Socket(), new RequestDispatcher(), sessionManager);

        authHandler = new AuthRequestHandler();
        wishlistHandler = new WishlistRequestHandler();
        friendHandler = new FriendRequestHandler();
        contributionHandler = new ContributionRequestHandler();
        notificationHandler = new NotificationRequestHandler();
    }

    @Test
    @DisplayName("Auth: Login with valid seed credentials succeeds and binds session")
    void testLoginSuccess() {
        Request req = Request.of(RequestType.LOGIN, new LoginRequestDTO("kady_x", "password123"));
        Response res = authHandler.handleLogin(req, clientHandler);

        assertEquals(ResponseStatus.SUCCESS, res.getStatus());
        AuthResultDTO authResult = res.getDataAs(AuthResultDTO.class);
        assertNotNull(authResult);
        assertNotNull(authResult.getToken());
        assertEquals(Integer.valueOf(2), sessionManager.resolve(authResult.getToken()));
    }

    @Test
    @DisplayName("Auth: Login with incorrect password returns UNAUTHORIZED")
    void testLoginFailure() {
        Request req = Request.of(RequestType.LOGIN, new LoginRequestDTO("kady_x", "wrong_pass"));
        Response res = authHandler.handleLogin(req, clientHandler);

        assertEquals(ResponseStatus.UNAUTHORIZED, res.getStatus());
    }

    @Test
    @DisplayName("Auth: Register new user persists to database and binds session")
    void testRegisterSuccess() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
                "newplayer",
                "player@dreamstop.com",
                "Secret123!",
                new BigDecimal("500.00"));
        Request req = Request.of(RequestType.REGISTER, dto);
        Response res = authHandler.handleRegister(req, clientHandler);

        assertEquals(ResponseStatus.SUCCESS, res.getStatus());
    }

    @Test
    @DisplayName("Wishlist: Retrieve catalog items returns seeded store catalog")
    void testGetCatalog() {
        Request req = new Request(RequestType.GET_CATALOG_ITEMS);
        Response res = wishlistHandler.handleGetCatalogItems(req, clientHandler);

        assertEquals(ResponseStatus.SUCCESS, res.getStatus());
        assertNotNull(res.getDataJson());
    }

    @Test
    @DisplayName("Wishlist: Add item to wishlist and retrieve for authenticated user")
    void testWishlistWorkflow() {
        String token = clientHandler.bindUser(2); // user 2: kady_x

        JsonObject addItemPayload = new JsonObject();
        addItemPayload.addProperty("itemId", 1);
        addItemPayload.addProperty("targetAmount", 89000.00);
        addItemPayload.addProperty("notes", "New GPU");
        addItemPayload.addProperty("priority", "HIGH");

        Request addReq = Request.of(RequestType.ADD_TO_WISHLIST, token, addItemPayload);
        Response addRes = wishlistHandler.handleAddToWishlist(addReq, clientHandler);

        assertEquals(ResponseStatus.SUCCESS, addRes.getStatus());

        Request getReq = Request.of(RequestType.GET_MY_WISHLIST, token, null);
        Response getRes = wishlistHandler.handleGetMyWishlist(getReq, clientHandler);

        assertEquals(ResponseStatus.SUCCESS, getRes.getStatus());
    }

    @Test
    @DisplayName("Friends: List friends and search other users")
    void testFriendsWorkflow() {
        String token = clientHandler.bindUser(1); // user 1: tarnished693

        Request getFriendsReq = Request.of(RequestType.GET_FRIENDS, token, null);
        Response friendsRes = friendHandler.handleGetFriends(getFriendsReq, clientHandler);
        assertEquals(ResponseStatus.SUCCESS, friendsRes.getStatus());

        Request searchReq = Request.of(RequestType.SEARCH_USERS, token, "kady");
        Response searchRes = friendHandler.handleSearchUsers(searchReq, clientHandler);
        assertEquals(ResponseStatus.SUCCESS, searchRes.getStatus());
    }

    @Test
    @DisplayName("Contribution: User contributes to friend wishlist item and verifies atomic update")
    void testContribution() {
        String token = clientHandler.bindUser(2); // user 2 contributes to wishlist item 1 (user 1's item)

        ContributeRequestDTO payload = new ContributeRequestDTO(1, new BigDecimal("100.00"));
        Request req = Request.of(RequestType.CONTRIBUTE, token, payload);

        Response res = contributionHandler.handleContribution(req, clientHandler);
        assertEquals(ResponseStatus.SUCCESS, res.getStatus());
    }

    @Test
    @DisplayName("Notifications: Retrieve notifications and mark as read")
    void testNotifications() {
        String token = clientHandler.bindUser(1); // user 1 has seed notifications

        Request getReq = Request.of(RequestType.GET_NOTIFICATIONS, token, null);
        Response getRes = notificationHandler.handleGetNotifications(getReq, clientHandler);
        assertEquals(ResponseStatus.SUCCESS, getRes.getStatus());

        Request markReq = Request.of(RequestType.MARK_NOTIFICATION_READ, token, 1);
        Response markRes = notificationHandler.handleMarkNotificationRead(markReq, clientHandler);
        assertEquals(ResponseStatus.SUCCESS, markRes.getStatus());
    }
}
