package com.dreamstop.server.network;

import com.dreamstop.common.protocol.ServerNotification;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/****
 * Manages user sessions using tokens.
 * Also supports pushing notifications to online users.
 */
public final class SessionManager {

    private final Map<String, Integer> tokenToUserId = new ConcurrentHashMap<>();
    private final Map<Integer, ClientHandler> userIdToHandler = new ConcurrentHashMap<>();

    // Creates a new session token for the user and registers the connection
    public String login(int userId, ClientHandler handler) {
        String token = UUID.randomUUID().toString();
        tokenToUserId.put(token, userId);
        userIdToHandler.put(userId, handler);
        return token;
    }

    public void logout(String token) {
        if (token == null) return;
        Integer userId = tokenToUserId.remove(token);
        if (userId != null) {
            userIdToHandler.remove(userId);
        }
    }

    // Returns the userId for a valid token, or null if invalid
    public Integer resolve(String token) {
        return token != null ? tokenToUserId.get(token) : null;
    }

    // Sends a notification to a user if they are currently online
    public boolean push(int userId, ServerNotification notification) {
        ClientHandler handler = userIdToHandler.get(userId);
        if (handler == null) return false;
        handler.sendNotification(notification);
        return true;
    }
}