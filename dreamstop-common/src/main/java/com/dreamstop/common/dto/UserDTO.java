package com.dreamstop.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class UserDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String username;
    private String email;
    private BigDecimal balance;

    public UserDTO() {
    }

    public UserDTO(int id, String username, String email, BigDecimal balance) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.balance = balance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public boolean hasSufficientBalance(BigDecimal amount) {
        if (amount == null || this.balance == null) {
            return false;
        }
        return this.balance.compareTo(amount) >= 0;
    }
}
