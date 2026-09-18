package com.dreamstop.server.handler;

import com.dreamstop.common.dto.UserDTO;
import com.dreamstop.common.protocol.Request;
import com.dreamstop.common.protocol.Response;
import com.dreamstop.server.network.ClientHandler;

import java.math.BigDecimal;

/****
 * Handles user profile and balance operations.
 *
 ****
 * Note *
 * I used STUB to return fixed dummy data to isolate and test this component
 ****
 *
 */

public class ProfileRequestHandler {

    public Response handleGetProfile(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) return Response.unauthorized("Unauthorized request");

        // stub profile data
        UserDTO profile = new UserDTO(userId, "user" + userId, "User " + userId, BigDecimal.ZERO);
        return Response.success(profile, "Profile retrieved");
    }

    public Response handleRechargeBalance(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) return Response.unauthorized("Unauthorized request");

        return Response.success("Balance recharged successfully (Stub)");
    }
}