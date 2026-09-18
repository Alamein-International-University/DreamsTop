package com.dreamstop.server.handler;

import com.dreamstop.common.protocol.Request;
import com.dreamstop.common.protocol.Response;
import com.dreamstop.server.network.ClientHandler;

import java.util.Collections;

/**
 * Handles catalog and wishlist operations.
 *
 ****
 * Note *
 * I used STUB to return fixed dummy data to isolate and test this component
 ****
 *
 */
public class WishlistRequestHandler {

    public Response handleGetCatalogItems(Request request, ClientHandler client) {
        return Response.success(Collections.emptyList(), "Catalog items retrieved");
    }

    public Response handleGetMyWishlist(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) return Response.unauthorized("Unauthorized request");

        return Response.success(Collections.emptyList(), "My wishlist retrieved");
    }

    public Response handleAddToWishlist(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) return Response.unauthorized("Unauthorized request");

        return Response.success("Item added to wishlist");
    }

    public Response handleRemoveFromWishlist(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) return Response.unauthorized("Unauthorized request");

        return Response.success("Item removed from wishlist");
    }

    public Response handleGetFriendWishlist(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) return Response.unauthorized("Unauthorized request");

        return Response.success(Collections.emptyList(), "Friend wishlist retrieved");
    }
}