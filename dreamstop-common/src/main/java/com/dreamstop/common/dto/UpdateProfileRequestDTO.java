package com.dreamstop.common.dto;

import java.io.Serializable;

public class UpdateProfileRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fullName;
    private String avatarColor;
    private String bio;

    public UpdateProfileRequestDTO() {
    }

    public UpdateProfileRequestDTO(String fullName, String avatarColor, String bio) {
        this.fullName = fullName;
        this.avatarColor = avatarColor;
        this.bio = bio;
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
}
