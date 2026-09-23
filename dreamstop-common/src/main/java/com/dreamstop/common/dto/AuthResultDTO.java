package com.dreamstop.common.dto;

import java.io.Serializable;

/**
 * DTO returned after a successful login.
 * Contains the session token and the authenticated user data.
 */
public class AuthResultDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String token;
    private UserDTO user;

    public AuthResultDTO() {
    }

    public AuthResultDTO(String token, UserDTO user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "AuthResultDTO{" +
                "hasToken=" + (token != null && !token.isBlank()) +
                ", user=" + (user != null ? user.getUsername() : "null") +
                '}';
    }
}