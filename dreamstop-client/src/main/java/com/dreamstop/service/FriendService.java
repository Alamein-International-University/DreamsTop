package com.dreamstop.service;

import com.dreamstop.common.dto.FriendshipDTO;
import com.dreamstop.common.dto.UserDTO;
import com.dreamstop.common.model.FriendshipStatus;
import com.dreamstop.common.model.RequestType;
import com.dreamstop.common.protocol.JsonUtils;
import com.dreamstop.common.protocol.Request;
import com.dreamstop.model.FriendRequest;
import com.dreamstop.model.User;
import com.dreamstop.network.NetworkClient;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.lang.reflect.Type;
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
        NetworkClient network = NetworkClient.getInstance();
        if (network.isConnected() && network.getSessionToken() != null) {
            refreshStateFromNetwork(network);
        } else {
            refreshStateFromMock();
        }
    }

    private void refreshStateFromNetwork(NetworkClient network) {
        Request getFriendsReq = Request.of(RequestType.GET_FRIENDS, network.getSessionToken(), null);
        network.sendRequestAsync(getFriendsReq).thenAccept(response -> {
            if (response.isSuccess() && response.getDataJson() != null) {
                Type listType = new TypeToken<List<UserDTO>>() {
                }.getType();
                List<UserDTO> dtos = JsonUtils.fromJson(response.getDataJson(), listType);
                if (dtos != null) {
                    List<User> userModels = dtos.stream().map(this::toModel).collect(Collectors.toList());
                    Platform.runLater(() -> friends.setAll(userModels));
                }
            }
        });

        Request getReqsReq = Request.of(RequestType.GET_FRIEND_REQUESTS, network.getSessionToken(), null);
        network.sendRequestAsync(getReqsReq).thenAccept(response -> {
            if (response.isSuccess() && response.getDataJson() != null) {
                Type listType = new TypeToken<List<FriendshipDTO>>() {
                }.getType();
                List<FriendshipDTO> dtos = JsonUtils.fromJson(response.getDataJson(), listType);
                if (dtos != null) {
                    List<FriendRequest> reqModels = dtos.stream().map(this::toModel).collect(Collectors.toList());
                    Platform.runLater(() -> incomingRequests.setAll(reqModels));
                }
            }
        });
    }

    private void refreshStateFromMock() {
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
        NetworkClient network = NetworkClient.getInstance();
        if (network.isConnected() && network.getSessionToken() != null) {
            try {
                Request req = Request.of(RequestType.SEARCH_USERS, network.getSessionToken(),
                        query != null ? query.trim() : "");
                var response = network.sendRequest(req, 3);
                if (response != null && response.isSuccess() && response.getDataJson() != null) {
                    Type listType = new TypeToken<List<UserDTO>>() {
                    }.getType();
                    List<UserDTO> dtos = JsonUtils.fromJson(response.getDataJson(), listType);
                    if (dtos != null) {
                        return dtos.stream().map(this::toModel).collect(Collectors.toList());
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // Fallback to local search
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
        return incomingRequests.stream().anyMatch(r -> r.getSender().equals(user)) ||
                sentRequests.stream().anyMatch(r -> r.getReceiver().equals(user));
    }

    public boolean sendFriendRequest(User targetUser) {
        User me = MockDataFactory.getCurrentUser();
        if (targetUser.equals(me) || hasPendingRequestWith(targetUser)) {
            return false;
        }

        NetworkClient network = NetworkClient.getInstance();
        if (network.isConnected() && network.getSessionToken() != null) {
            try {
                int targetId = parseNumericId(targetUser.getId());
                JsonObject payload = new JsonObject();
                payload.addProperty("targetUserId", targetId);
                Request req = Request.of(RequestType.SEND_FRIEND_REQUEST, network.getSessionToken(), payload);
                network.sendRequestAsync(req);
            } catch (Exception ignored) {
            }
        }

        FriendRequest newReq = new FriendRequest(
                "req-" + UUID.randomUUID().toString().substring(0, 8),
                me,
                targetUser,
                FriendRequest.Status.PENDING,
                LocalDateTime.now());
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

        NetworkClient network = NetworkClient.getInstance();
        if (network.isConnected() && network.getSessionToken() != null) {
            try {
                int reqId = parseNumericId(request.getId());
                JsonObject payload = new JsonObject();
                payload.addProperty("requestId", reqId);
                Request req = Request.of(RequestType.ACCEPT_FRIEND_REQUEST, network.getSessionToken(), payload);
                network.sendRequestAsync(req);
            } catch (Exception ignored) {
            }
        }

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

        NetworkClient network = NetworkClient.getInstance();
        if (network.isConnected() && network.getSessionToken() != null) {
            try {
                int reqId = parseNumericId(request.getId());
                JsonObject payload = new JsonObject();
                payload.addProperty("requestId", reqId);
                Request req = Request.of(RequestType.DECLINE_FRIEND_REQUEST, network.getSessionToken(), payload);
                network.sendRequestAsync(req);
            } catch (Exception ignored) {
            }
        }

        return true;
    }

    public boolean removeFriend(User friend) {
        User me = MockDataFactory.getCurrentUser();
        Set<String> myFriends = MockDataFactory.getFriendships().get(me.getId());
        Set<String> theirFriends = MockDataFactory.getFriendships().get(friend.getId());

        if (myFriends != null)
            myFriends.remove(friend.getId());
        if (theirFriends != null)
            theirFriends.remove(me.getId());

        friends.remove(friend);

        NetworkClient network = NetworkClient.getInstance();
        if (network.isConnected() && network.getSessionToken() != null) {
            try {
                int friendId = parseNumericId(friend.getId());
                JsonObject payload = new JsonObject();
                payload.addProperty("friendId", friendId);
                Request req = Request.of(RequestType.REMOVE_FRIEND, network.getSessionToken(), payload);
                network.sendRequestAsync(req);
            } catch (Exception ignored) {
            }
        }

        return true;
    }

    private User toModel(UserDTO dto) {
        if (dto == null)
            return null;
        return new User(
                String.valueOf(dto.getId()),
                dto.getUsername(),
                dto.getUsername(),
                dto.getEmail(),
                "#6366F1",
                "Player");
    }

    private FriendRequest toModel(FriendshipDTO dto) {
        if (dto == null)
            return null;
        User sender = toModel(dto.getRequester());
        User receiver = toModel(dto.getAddressee());
        FriendRequest.Status status = FriendRequest.Status.PENDING;
        if (dto.getStatus() == FriendshipStatus.ACCEPTED)
            status = FriendRequest.Status.ACCEPTED;
        if (dto.getStatus() == FriendshipStatus.DECLINED)
            status = FriendRequest.Status.DECLINED;

        return new FriendRequest(
                String.valueOf(dto.getId()),
                sender,
                receiver,
                status,
                LocalDateTime.now());
    }

    private int parseNumericId(String idStr) {
        if (idStr == null)
            return 0;
        if (idStr.startsWith("usr-") || idStr.startsWith("req-")) {
            idStr = idStr.substring(4);
        }
        try {
            return Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
