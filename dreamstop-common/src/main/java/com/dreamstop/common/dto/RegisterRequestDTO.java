package com.dreamstop.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Data Transfer Object containing registration details submitted by a new user.
 */
public class RegisterRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String email;
    private String password;
    private BigDecimal initialBalance;

    public RegisterRequestDTO() {
        this.initialBalance = BigDecimal.ZERO;
    }

    public RegisterRequestDTO(String username, String email, String password, BigDecimal initialBalance) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.initialBalance = initialBalance != null ? initialBalance : BigDecimal.ZERO;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    @Override
    public String toString() {
        return "RegisterRequestDTO{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password=***" +
                ", initialBalance=" + initialBalance +
                '}';
    }
}
