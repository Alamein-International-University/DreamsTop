package com.dreamstop.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class UserDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String username;
    private String email;
    private BigDecimal balance;
    private String fullName;
    private String avatarColor;
    private String bio;

    public UserDTO() {
    }

    public UserDTO(int id, String username, String email, BigDecimal balance) {
        this(id, username, email, balance, username, "#6366F1", "");
    }

    public UserDTO(int id, String username, String email, BigDecimal balance, String fullName, String avatarColor, String bio) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.balance = balance;
        this.fullName = fullName != null ? fullName : username;
        this.avatarColor = avatarColor != null ? avatarColor : "#6366F1";
        this.bio = bio != null ? bio : "";
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatarColor() {
        return avatarColor;
    }

    public void setAvatarColor(String avatarColor) {
        this.avatarColor = avatarColor;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public boolean hasSufficientBalance(BigDecimal amount) {
        if (amount == null || this.balance == null) {
            return false;
        }
        return this.balance.compareTo(amount) >= 0;
    }
}
