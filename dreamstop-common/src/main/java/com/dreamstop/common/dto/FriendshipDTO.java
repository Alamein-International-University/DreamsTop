package com.dreamstop.common.dto;

import com.dreamstop.common.model.FriendshipStatus;
import java.io.Serializable;

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
}
