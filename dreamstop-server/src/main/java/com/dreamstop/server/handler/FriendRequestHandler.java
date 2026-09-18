package com.dreamstop.server.handler;

import com.dreamstop.common.protocol.Request;
import com.dreamstop.common.protocol.Response;
import com.dreamstop.server.network.ClientHandler;

import java.util.Collections;

/****
 * Handles all friend-related requests.
 *
 ****
 * Note *
 * I used STUB to return fixed dummy data to isolate and test this component
 ****
 *
 */

public class FriendRequestHandler {

    public Response handleGetFriends(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) return Response.unauthorized("Unauthorized request");

        return Response.success(Collections.emptyList(), "Friends list retrieved");
    }

    public Response handleSearchUsers(Request request, ClientHandler client) {
        return Response.success(Collections.emptyList(), "Search results retrieved");
    }

    public Response handleSendFriendRequest(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) return Response.unauthorized("Unauthorized request");

        return Response.success("Friend request sent");
    }

    public Response handleAcceptFriendRequest(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) return Response.unauthorized("Unauthorized request");

        return Response.success("Friend request accepted");
    }

    public Response handleDeclineFriendRequest(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) return Response.unauthorized("Unauthorized request");

        return Response.success("Friend request declined");
    }

    public Response handleRemoveFriend(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) return Response.unauthorized("Unauthorized request");

        return Response.success("Friend removed");
    }

    public Response handleGetFriendRequests(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) return Response.unauthorized("Unauthorized request");

        return Response.success(Collections.emptyList(), "Friend requests retrieved");
    }
}