package com.dreamstop.model;

import java.util.Objects;

public class User {
    private String id;
    private String username;
    private String fullName;
    private String email;
    private String avatarColor;
    private String bio;

    public User() {}

    public User(String id, String username, String fullName, String email, String avatarColor, String bio) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.avatarColor = avatarColor;
        this.bio = bio;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarColor() {
        return avatarColor != null ? avatarColor : "#6366F1";
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

    public String getInitials() {
        if (fullName == null || fullName.trim().isEmpty()) {
            return username != null && !username.isEmpty() ? username.substring(0, 1).toUpperCase() : "?";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        }
        return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return fullName + " (@" + username + ")";
    }
}
