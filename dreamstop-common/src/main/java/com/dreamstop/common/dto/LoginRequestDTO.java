package com.dreamstop.common.dto;

import java.io.Serializable;

/**
 * Data Transfer Object containing login credentials sent by the client.
 */
public class LoginRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String usernameOrEmail;
    private String password;

    public LoginRequestDTO() {
    }

    public LoginRequestDTO(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginRequestDTO{" +
                "usernameOrEmail='" + usernameOrEmail + '\'' +
                ", password=***" +
                '}';
    }
}
