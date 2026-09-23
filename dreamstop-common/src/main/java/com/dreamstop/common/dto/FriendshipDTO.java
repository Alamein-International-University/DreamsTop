package com.dreamstop.common.dto;

import com.dreamstop.common.model.FriendshipStatus;
import java.io.Serializable;

/**
 * Data Transfer Object representing a friendship relation between two users.
 */
public class FriendshipDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private UserDTO requester;
    private UserDTO addressee;
    private FriendshipStatus status;

    public FriendshipDTO() {
    }

    public FriendshipDTO(int id, UserDTO requester, UserDTO addressee, FriendshipStatus status) {
        this.id = id;
        this.requester = requester;
        this.addressee = addressee;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UserDTO getRequester() {
        return requester;
    }

    public void setRequester(UserDTO requester) {
        this.requester = requester;
    }

    public UserDTO getAddressee() {
        return addressee;
    }

    public void setAddressee(UserDTO addressee) {
        this.addressee = addressee;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public void setStatus(FriendshipStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FriendshipDTO that = (FriendshipDTO) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "FriendshipDTO{" +
                "id=" + id +
                ", requester=" + (requester != null ? requester.getUsername() : "null") +
                ", addressee=" + (addressee != null ? addressee.getUsername() : "null") +
                ", status=" + status +
                '}';
    }
}
