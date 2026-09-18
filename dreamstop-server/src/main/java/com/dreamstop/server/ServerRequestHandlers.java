package com.dreamstop.server;

import com.dreamstop.common.model.RequestType;
import com.dreamstop.common.protocol.Response;
import com.dreamstop.server.handler.*;
import com.dreamstop.server.network.RequestDispatcher;

/****
 * Registers all request handlers into the dispatcher.
 */
public final class ServerRequestHandlers {

    private ServerRequestHandlers() {
    }

    public static RequestDispatcher buildDefault() {
        RequestDispatcher dispatcher = new RequestDispatcher();

        AuthRequestHandler authHandler = new AuthRequestHandler();
        ProfileRequestHandler profileHandler = new ProfileRequestHandler();
        FriendRequestHandler friendHandler = new FriendRequestHandler();
        WishlistRequestHandler wishlistHandler = new WishlistRequestHandler();
        ContributionRequestHandler contributionHandler = new ContributionRequestHandler();
        NotificationRequestHandler notificationHandler = new NotificationRequestHandler();

        // Authentication
        dispatcher.register(RequestType.LOGIN, authHandler::handleLogin);
        dispatcher.register(RequestType.REGISTER, authHandler::handleRegister);
        dispatcher.register(RequestType.LOGOUT, authHandler::handleLogout);

        // Profile
        dispatcher.register(RequestType.GET_PROFILE, profileHandler::handleGetProfile);
        dispatcher.register(RequestType.RECHARGE_BALANCE, profileHandler::handleRechargeBalance);

        // Friends
        dispatcher.register(RequestType.GET_FRIENDS, friendHandler::handleGetFriends);
        dispatcher.register(RequestType.SEARCH_USERS, friendHandler::handleSearchUsers);
        dispatcher.register(RequestType.SEND_FRIEND_REQUEST, friendHandler::handleSendFriendRequest);
        dispatcher.register(RequestType.ACCEPT_FRIEND_REQUEST, friendHandler::handleAcceptFriendRequest);
        dispatcher.register(RequestType.DECLINE_FRIEND_REQUEST, friendHandler::handleDeclineFriendRequest);
        dispatcher.register(RequestType.REMOVE_FRIEND, friendHandler::handleRemoveFriend);
        dispatcher.register(RequestType.GET_FRIEND_REQUESTS, friendHandler::handleGetFriendRequests);

        // Wishlist
        dispatcher.register(RequestType.GET_CATALOG_ITEMS, wishlistHandler::handleGetCatalogItems);
        dispatcher.register(RequestType.GET_MY_WISHLIST, wishlistHandler::handleGetMyWishlist);
        dispatcher.register(RequestType.ADD_TO_WISHLIST, wishlistHandler::handleAddToWishlist);
        dispatcher.register(RequestType.REMOVE_FROM_WISHLIST, wishlistHandler::handleRemoveFromWishlist);
        dispatcher.register(RequestType.GET_FRIEND_WISHLIST, wishlistHandler::handleGetFriendWishlist);

        // Contribution
        dispatcher.register(RequestType.CONTRIBUTE, contributionHandler::handleContribution);

        // Notifications
        dispatcher.register(RequestType.GET_NOTIFICATIONS, notificationHandler::handleGetNotifications);
        dispatcher.register(RequestType.MARK_NOTIFICATION_READ, notificationHandler::handleMarkNotificationRead);

        // Utility
        dispatcher.register(RequestType.PING, (req, client) -> Response.success("PONG"));

        return dispatcher;
    }
}