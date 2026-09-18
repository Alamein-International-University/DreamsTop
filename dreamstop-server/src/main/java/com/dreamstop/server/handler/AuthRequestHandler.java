package com.dreamstop.server.handler;

import com.dreamstop.common.dto.AuthResultDTO;
import com.dreamstop.common.dto.LoginRequestDTO;
import com.dreamstop.common.dto.UserDTO;
import com.dreamstop.common.protocol.Request;
import com.dreamstop.common.protocol.Response;
import com.dreamstop.server.network.ClientHandler;

import java.math.BigDecimal;

/****
 * Handles authentication requests (login, register, logout).
 */
public class AuthRequestHandler {

    public Response handleLogin(Request request, ClientHandler client) {
        LoginRequestDTO login = request.getPayloadAs(LoginRequestDTO.class);
        if (login == null) {
            return Response.badRequest("Invalid login payload");
        }

        // stub: always succeed and create a session
        UserDTO user = new UserDTO(1, login.getUsernameOrEmail(), login.getUsernameOrEmail(), BigDecimal.ZERO);
        String token = client.bindUser(user.getId());

        return Response.success(new AuthResultDTO(token, user), "Login successful");
    }

    /****
      ** Note **
      I used STUB to return fixed dummy data to isolate and test this component
  */
    public Response handleRegister(Request request, ClientHandler client) {
        return Response.success("User registered successfully (Stub)");
    }

    public Response handleLogout(Request request, ClientHandler client) {
        if (request.getToken() != null) {
            client.getSessionManager().logout(request.getToken());
        }
        return Response.success("Logged out successfully");
    }
}