package com.dreamstop.service;

import com.dreamstop.model.FriendRequest;
import com.dreamstop.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class FriendService {

    private static FriendService instance;

    private final ObservableList<User> friends = FXCollections.observableArrayList();
    private final ObservableList<FriendRequest> incomingRequests = FXCollections.observableArrayList();
    private final ObservableList<FriendRequest> sentRequests = FXCollections.observableArrayList();

    private FriendService() {
        refreshState();
    }

    public static synchronized FriendService getInstance() {
        if (instance == null) {
            instance = new FriendService();
        }
        return instance;
    }

    public void refreshState() {
        User me = MockDataFactory.getCurrentUser();
        Set<String> friendIds = MockDataFactory.getFriendships().getOrDefault(me.getId(), Collections.emptySet());

        List<User> currentFriends = MockDataFactory.getAllUsers().stream()
                .filter(u -> friendIds.contains(u.getId()))
                .collect(Collectors.toList());
        friends.setAll(currentFriends);

        List<FriendRequest> incoming = MockDataFactory.getFriendRequests().stream()
                .filter(r -> r.getReceiver().equals(me) && r.getStatus() == FriendRequest.Status.PENDING)
                .collect(Collectors.toList());
        incomingRequests.setAll(incoming);

        List<FriendRequest> sent = MockDataFactory.getFriendRequests().stream()
                .filter(r -> r.getSender().equals(me) && r.getStatus() == FriendRequest.Status.PENDING)
                .collect(Collectors.toList());
        sentRequests.setAll(sent);
    }

    public ObservableList<User> getFriends() {
        return friends;
    }

    public ObservableList<FriendRequest> getIncomingRequests() {
        return incomingRequests;
    }

    public ObservableList<FriendRequest> getSentRequests() {
        return sentRequests;
    }

    public List<User> searchDiscoverableUsers(String query) {
        User me = MockDataFactory.getCurrentUser();
        Set<String> friendIds = MockDataFactory.getFriendships().getOrDefault(me.getId(), Collections.emptySet());
        String q = query != null ? query.trim().toLowerCase() : "";

        return MockDataFactory.getAllUsers().stream()
                .filter(u -> !u.equals(me))
                .filter(u -> !friendIds.contains(u.getId()))
                .filter(u -> q.isEmpty() ||
                        u.getFullName().toLowerCase().contains(q) ||
                        u.getUsername().toLowerCase().contains(q) ||
                        u.getEmail().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public boolean hasPendingRequestWith(User user) {
        User me = MockDataFactory.getCurrentUser();
        return MockDataFactory.getFriendRequests().stream()
                .anyMatch(r -> r.getStatus() == FriendRequest.Status.PENDING &&
                        ((r.getSender().equals(me) && r.getReceiver().equals(user)) ||
                         (r.getSender().equals(user) && r.getReceiver().equals(me))));
    }

    public boolean sendFriendRequest(User targetUser) {
        User me = MockDataFactory.getCurrentUser();
        if (targetUser.equals(me) || hasPendingRequestWith(targetUser)) {
            return false;
        }

        FriendRequest newReq = new FriendRequest(
                "req-" + UUID.randomUUID().toString().substring(0, 8),
                me,
                targetUser,
                FriendRequest.Status.PENDING,
                LocalDateTime.now()
        );
        MockDataFactory.getFriendRequests().add(newReq);
        sentRequests.add(newReq);
        return true;
    }

    public boolean cancelSentRequest(FriendRequest request) {
        if (MockDataFactory.getFriendRequests().remove(request)) {
            sentRequests.remove(request);
            return true;
        }
        return false;
    }

    public boolean acceptFriendRequest(FriendRequest request) {
        request.setStatus(FriendRequest.Status.ACCEPTED);
        User me = MockDataFactory.getCurrentUser();
        User newFriend = request.getSender().equals(me) ? request.getReceiver() : request.getSender();

        MockDataFactory.getFriendships().computeIfAbsent(me.getId(), k -> new HashSet<>()).add(newFriend.getId());
        MockDataFactory.getFriendships().computeIfAbsent(newFriend.getId(), k -> new HashSet<>()).add(me.getId());

        incomingRequests.remove(request);
        if (!friends.contains(newFriend)) {
            friends.add(newFriend);
        }
        return true;
    }

    public boolean declineFriendRequest(FriendRequest request) {
        request.setStatus(FriendRequest.Status.DECLINED);
        incomingRequests.remove(request);
        return true;
    }

    public boolean removeFriend(User friend) {
        User me = MockDataFactory.getCurrentUser();
        Set<String> myFriends = MockDataFactory.getFriendships().get(me.getId());
        Set<String> theirFriends = MockDataFactory.getFriendships().get(friend.getId());

        if (myFriends != null) myFriends.remove(friend.getId());
        if (theirFriends != null) theirFriends.remove(me.getId());

        friends.remove(friend);
        return true;
    }
}
