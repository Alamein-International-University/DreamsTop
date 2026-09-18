package com.dreamstop.server.handler;

import com.dreamstop.common.protocol.Request;
import com.dreamstop.common.protocol.Response;
import com.dreamstop.server.network.ClientHandler;

import java.util.Collections;

/****
 * Handles notification retrieval and marking as read.
 *
 ****
 * Note *
 * I used STUB to return fixed dummy data to isolate and test this component
 ****
 */

public class NotificationRequestHandler {

    public Response handleGetNotifications(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) return Response.unauthorized("Unauthorized request");

        return Response.success(Collections.emptyList(), "Notifications list retrieved");
    }

    public Response handleMarkNotificationRead(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) return Response.unauthorized("Unauthorized request");

        return Response.success("Notification marked as read");
    }
}