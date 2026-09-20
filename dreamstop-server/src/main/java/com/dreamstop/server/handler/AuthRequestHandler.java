package com.dreamstop.server.handler;

import com.dreamstop.common.dto.AuthResultDTO;
import com.dreamstop.common.dto.LoginRequestDTO;
import com.dreamstop.common.dto.RegisterRequestDTO;
import com.dreamstop.common.dto.UserDTO;
import com.dreamstop.common.protocol.Request;
import com.dreamstop.common.protocol.Response;
import com.dreamstop.server.dao.DAOFactory;
import com.dreamstop.server.dao.UserDAO;
import com.dreamstop.server.network.ClientHandler;

import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthRequestHandler {

    private static final Logger LOGGER = Logger.getLogger(AuthRequestHandler.class.getName());
    private final UserDAO userDAO;

    public AuthRequestHandler() {
        this(DAOFactory.getInstance().getUserDAO());
    }

    public AuthRequestHandler(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public Response handleLogin(Request request, ClientHandler client) {
        LoginRequestDTO login = request.getPayloadAs(LoginRequestDTO.class);
        if (login == null || isBlank(login.getUsernameOrEmail()) || isBlank(login.getPassword())) {
            return Response.badRequest("Username and password are required");
        }

        try {
            Optional<UserDTO> authenticatedUser = userDAO.authenticate(
                    login.getUsernameOrEmail().trim(),
                    login.getPassword()
            );

            if (authenticatedUser.isEmpty()) {
                return Response.unauthorized("Invalid username/email or password");
            }

            UserDTO user = authenticatedUser.get();
            String token = client.bindUser(user.getId());
            return Response.success(new AuthResultDTO(token, user), "Login successful");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during login for: " + login.getUsernameOrEmail(), e);
            return Response.error("Internal database error during authentication");
        }
    }

    public Response handleRegister(Request request, ClientHandler client) {
        RegisterRequestDTO registerReq = request.getPayloadAs(RegisterRequestDTO.class);
        if (registerReq == null || isBlank(registerReq.getUsername()) || isBlank(registerReq.getEmail()) || isBlank(registerReq.getPassword())) {
            return Response.badRequest("Username, email, and password are required");
        }

        try {
            String fullName = registerReq.getUsername();
            String avatarColor = "#6366F1";
            String bio = "";

            UserDTO createdUser = userDAO.register(registerReq, fullName, avatarColor, bio);
            String token = client.bindUser(createdUser.getId());
            return Response.success(new AuthResultDTO(token, createdUser), "User registered successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Registration failed for: " + registerReq.getUsername(), e);
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("unique") || msg.contains("duplicate") || e.getErrorCode() == 1062) {
                return Response.badRequest("Username or email already exists");
            }
            return Response.error("Failed to register user due to database error");
        }
    }

    public Response handleLogout(Request request, ClientHandler client) {
        if (request.getToken() != null) {
            client.getSessionManager().logout(request.getToken());
        }
        return Response.success("Logged out successfully");
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}