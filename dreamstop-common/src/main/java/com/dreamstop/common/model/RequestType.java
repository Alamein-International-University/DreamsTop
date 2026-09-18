package com.dreamstop.common.model;

public enum RequestType {
    // Authentication
    LOGIN,
    REGISTER,
    LOGOUT,

    // Profile & Balance
    GET_PROFILE,
    RECHARGE_BALANCE,

    // Friends
    GET_FRIENDS,
    SEARCH_USERS,
    SEND_FRIEND_REQUEST,
    ACCEPT_FRIEND_REQUEST,
    DECLINE_FRIEND_REQUEST,
    REMOVE_FRIEND,
    GET_FRIEND_REQUESTS,

    // Catalog & Wishlist
    GET_CATALOG_ITEMS,
    GET_MY_WISHLIST,
    ADD_TO_WISHLIST,
    REMOVE_FROM_WISHLIST,
    GET_FRIEND_WISHLIST,

    // Contribution
    CONTRIBUTE,

    // Notifications
    GET_NOTIFICATIONS,
    MARK_NOTIFICATION_READ,

    // Utility
    PING
}
