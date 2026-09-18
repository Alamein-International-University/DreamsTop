package com.dreamstop.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class FriendRequest {
    public enum Status {
        PENDING,
        ACCEPTED,
        DECLINED
    }

    private String id;
    private User sender;
    private User receiver;
    private Status status;
    private LocalDateTime createdAt;

    public FriendRequest() {
        this.status = Status.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public FriendRequest(String id, User sender, User receiver, Status status, LocalDateTime createdAt) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.status = status != null ? status : Status.PENDING;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendRequest that = (FriendRequest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
