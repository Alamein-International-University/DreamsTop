package com.dreamstop.controller;

import com.dreamstop.model.FriendRequest;
import com.dreamstop.model.User;
import com.dreamstop.model.WishlistItem;
import com.dreamstop.service.FriendService;
import com.dreamstop.service.MockDataFactory;
import com.dreamstop.util.NotificationUtil;
import com.dreamstop.util.UiStyleUtil;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class FriendsController implements Initializable {

    @FXML
    private TabPane friendsTabPane;
    @FXML
    private Tab tabFriends;
    @FXML
    private Tab tabRequests;
    @FXML
    private Tab tabFindFriends;

    @FXML
    private TextField txtSearchFriends;
    @FXML
    private Label lblFriendsCount;
    @FXML
    private VBox friendsContainer;

    @FXML
    private Label lblIncomingCount;
    @FXML
    private VBox incomingContainer;
    @FXML
    private VBox sentContainer;

    @FXML
    private TextField txtSearchNewUsers;
    @FXML
    private VBox discoverContainer;

    private final FriendService friendService = FriendService.getInstance();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        renderFriends("");
        renderRequests();
        renderDiscoverUsers("");

        // Search listeners
        txtSearchFriends.textProperty().addListener((obs, oldVal, newVal) -> renderFriends(newVal));
        txtSearchNewUsers.textProperty().addListener((obs, oldVal, newVal) -> renderDiscoverUsers(newVal));

        // State change listeners
        friendService.getFriends().addListener((ListChangeListener<User>) c -> {
            renderFriends(txtSearchFriends.getText());
            renderDiscoverUsers(txtSearchNewUsers.getText());
        });

        friendService.getIncomingRequests().addListener((ListChangeListener<FriendRequest>) c -> {
            renderRequests();
            renderDiscoverUsers(txtSearchNewUsers.getText());
        });

        friendService.getSentRequests().addListener((ListChangeListener<FriendRequest>) c -> {
            renderRequests();
            renderDiscoverUsers(txtSearchNewUsers.getText());
        });
    }

    // ==========================================
    // TASK 5: View Friends List & TASK 2: Remove
    // ==========================================
    private void renderFriends(String filter) {
        friendsContainer.getChildren().clear();

        String q = filter != null ? filter.trim().toLowerCase() : "";
        List<User> list = friendService.getFriends().stream()
                .filter(f -> q.isEmpty() ||
                        f.getFullName().toLowerCase().contains(q) ||
                        f.getUsername().toLowerCase().contains(q) ||
                        f.getEmail().toLowerCase().contains(q))
                .collect(Collectors.toList());

        lblFriendsCount.setText(list.size() + " " + (list.size() == 1 ? "friend" : "friends"));

        if (list.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50, 20, 50, 20));

            Label emoji = new Label("👥");
            emoji.getStyleClass().add("empty-emoji-large");

            Label title = new Label(
                    q.isEmpty() ? "You don't have any friends added yet!" : "No friends matched \"" + q + "\"");
            title.getStyleClass().add("empty-title");

            Label sub = new Label(
                    q.isEmpty() ? "Check the \"Find New Friends\" tab or respond to incoming friend requests."
                            : "Try searching by full name or username.");
            sub.getStyleClass().add("empty-subtitle");

            emptyBox.getChildren().addAll(emoji, title, sub);
            friendsContainer.getChildren().add(emptyBox);
            return;
        }

        for (User friend : list) {
            friendsContainer.getChildren().add(createFriendCard(friend));
        }
    }

    private HBox createFriendCard(User friend) {
        HBox card = new HBox(16);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);

        // Avatar circle
        StackPane avatarPane = new StackPane();
        UiStyleUtil.applyAvatar(avatarPane, friend.getAvatarColor(), "avatar-circle-medium");
        Label avatarText = new Label(friend.getInitials());
        avatarText.getStyleClass().add("avatar-text");
        avatarPane.getChildren().add(avatarText);

        // Friend info
        VBox infoBox = new VBox(3);
        HBox nameLine = new HBox(8);
        nameLine.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(friend.getFullName());
        nameLabel.getStyleClass().add("card-title");

        Label handleLabel = new Label("@" + friend.getUsername());
        handleLabel.getStyleClass().add("handle-label");

        // Wishlist items count
        List<WishlistItem> friendItems = MockDataFactory.getWishlistsByUser().getOrDefault(friend.getId(), List.of());
        Label wishlistBadge = new Label(friendItems.size() + " items");
        wishlistBadge.setGraphic(com.dreamstop.util.IconUtil.getIcon(com.dreamstop.util.IconUtil.IconType.GIFT, 12, "#818CF8"));
        wishlistBadge.setGraphicTextGap(5);
        wishlistBadge.getStyleClass().add("badge");

        nameLine.getChildren().addAll(nameLabel, handleLabel, wishlistBadge);

        Label bioLabel = new Label(friend.getBio() != null ? friend.getBio() : friend.getEmail());
        bioLabel.getStyleClass().add("card-desc");

        infoBox.getChildren().addAll(nameLine, bioLabel);
        infoBox.setMinWidth(0);
        infoBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Action Buttons: View Wishlist (Task 6) & Remove Friend (Task 2)
        Button btnViewWishlist = new Button("View Wishlist");
        btnViewWishlist.getStyleClass().add("btn-primary");
        com.dreamstop.util.IconUtil.styleButton(btnViewWishlist, com.dreamstop.util.IconUtil.IconType.GIFT, "View Wishlist", 12, "#FFFFFF");
        btnViewWishlist.setOnAction(e -> handleViewFriendWishlist(friend));

        Button btnRemove = new Button("Remove");
        btnRemove.getStyleClass().add("btn-card-delete");
        com.dreamstop.util.IconUtil.styleButton(btnRemove, com.dreamstop.util.IconUtil.IconType.CLOSE, "Remove", 11, "#F87171");
        btnRemove.setOnAction(e -> handleRemoveFriend(friend));

        card.getChildren().addAll(avatarPane, infoBox, spacer, btnViewWishlist, btnRemove);
        return card;
    }

    private void handleViewFriendWishlist(User friend) {
        if (MainDashboardController.getInstance() != null) {
            MainDashboardController.getInstance().openFriendWishlist(friend);
        }
    }

    private void handleRemoveFriend(User friend) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        com.dreamstop.util.ThemeManager.applyTheme(alert.getDialogPane());
        alert.setTitle("Remove Friend");
        alert.setHeaderText("Remove " + friend.getFullName() + " from your friends?");
        alert.setContentText(
                "You won't be able to view their private wishlists or contribute to their gifts until you add them again.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            friendService.removeFriend(friend);
            NotificationUtil.showInfo("Removed " + friend.getFullName() + " from friends.");
        }
    }

    // ==========================================
    // TASK 3: Accept / Decline Friend Requests
    // ==========================================
    private void renderRequests() {
        incomingContainer.getChildren().clear();
        sentContainer.getChildren().clear();

        var incoming = friendService.getIncomingRequests();
        lblIncomingCount.setText(incoming.size() + " pending");

        if (incoming.isEmpty()) {
            Label noInc = new Label("No pending incoming requests.");
            noInc.getStyleClass().add("empty-request");
            incomingContainer.getChildren().add(noInc);
        } else {
            for (FriendRequest req : incoming) {
                incomingContainer.getChildren().add(createIncomingRequestCard(req));
            }
        }

        var sent = friendService.getSentRequests();
        if (sent.isEmpty()) {
            Label noSent = new Label("No outgoing sent requests.");
            noSent.getStyleClass().add("empty-request");
            sentContainer.getChildren().add(noSent);
        } else {
            for (FriendRequest req : sent) {
                sentContainer.getChildren().add(createSentRequestCard(req));
            }
        }
    }

    private HBox createIncomingRequestCard(FriendRequest req) {
        HBox card = new HBox(14);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);

        User sender = req.getSender();

        StackPane avatar = new StackPane();
        UiStyleUtil.applyAvatar(avatar, sender.getAvatarColor(), "avatar-circle-small");
        Label avatarText = new Label(sender.getInitials());
        avatarText.getStyleClass().add("avatar-text");
        avatar.getChildren().add(avatarText);

        VBox info = new VBox(2);
        Label name = new Label(sender.getFullName() + " (@" + sender.getUsername() + ")");
        name.getStyleClass().add("card-title");
        Label time = new Label("Requested on " + req.getCreatedAt().format(dateFormatter));
        time.getStyleClass().add("request-time");
        info.getChildren().addAll(name, time);
        info.setMinWidth(0);
        info.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(info, Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAccept = new Button("Accept");
        btnAccept.getStyleClass().add("btn-success");
        com.dreamstop.util.IconUtil.styleButton(btnAccept, com.dreamstop.util.IconUtil.IconType.CHECK, "Accept", 12, "#FFFFFF");
        btnAccept.setOnAction(e -> {
            friendService.acceptFriendRequest(req);
            NotificationUtil.showSuccess("You and " + sender.getFullName() + " are now friends!");
        });

        Button btnDecline = new Button("Decline");
        btnDecline.getStyleClass().add("btn-card-delete");
        com.dreamstop.util.IconUtil.styleButton(btnDecline, com.dreamstop.util.IconUtil.IconType.CLOSE, "Decline", 11, "#F87171");
        btnDecline.setOnAction(e -> {
            friendService.declineFriendRequest(req);
            NotificationUtil.showInfo("Declined friend request from @" + sender.getUsername());
        });

        card.getChildren().addAll(avatar, info, spacer, btnAccept, btnDecline);
        return card;
    }

    private HBox createSentRequestCard(FriendRequest req) {
        HBox card = new HBox(14);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);

        User receiver = req.getReceiver();

        StackPane avatar = new StackPane();
        UiStyleUtil.applyAvatar(avatar, receiver.getAvatarColor(), "avatar-circle-request");
        Label avatarText = new Label(receiver.getInitials());
        avatarText.getStyleClass().add("avatar-text");
        avatar.getChildren().add(avatarText);

        VBox info = new VBox(2);
        Label name = new Label("Sent to " + receiver.getFullName() + " (@" + receiver.getUsername() + ")");
        name.getStyleClass().add("card-title");
        Label status = new Label("Status: Pending approval • " + req.getCreatedAt().format(dateFormatter));
        status.getStyleClass().add("request-status");
        info.getChildren().addAll(name, status);
        info.setMinWidth(0);
        info.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(info, Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnCancel = new Button("Cancel Request");
        btnCancel.getStyleClass().add("btn-secondary");
        btnCancel.setOnAction(e -> {
            friendService.cancelSentRequest(req);
            NotificationUtil.showInfo("Cancelled request to @" + receiver.getUsername());
        });

        card.getChildren().addAll(avatar, info, spacer, btnCancel);
        return card;
    }

    // ==========================================
    // TASK 2: Find & Add Friends
    // ==========================================
    private void renderDiscoverUsers(String query) {
        discoverContainer.getChildren().clear();

        List<User> discoverable = friendService.searchDiscoverableUsers(query);

        if (discoverable.isEmpty()) {
            VBox empty = new VBox(10);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(40, 20, 40, 20));

            Label emoji = new Label("🔍");
            emoji.getStyleClass().add("empty-emoji-medium");

            Label title = new Label("No new users found.");
            title.getStyleClass().add("empty-title-small");

            empty.getChildren().addAll(emoji, title);
            discoverContainer.getChildren().add(empty);
            return;
        }

        for (User user : discoverable) {
            discoverContainer.getChildren().add(createDiscoverUserCard(user));
        }
    }

    private HBox createDiscoverUserCard(User user) {
        HBox card = new HBox(14);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);

        StackPane avatar = new StackPane();
        UiStyleUtil.applyAvatar(avatar, user.getAvatarColor(), "avatar-circle-discover");
        Label avatarText = new Label(user.getInitials());
        avatarText.getStyleClass().add("avatar-text");
        avatar.getChildren().add(avatarText);

        VBox info = new VBox(2);
        HBox nameLine = new HBox(6);
        nameLine.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(user.getFullName());
        name.getStyleClass().add("card-title");
        Label handle = new Label("@" + user.getUsername());
        handle.getStyleClass().add("handle-label");
        nameLine.getChildren().addAll(name, handle);

        Label bio = new Label(user.getBio() != null ? user.getBio() : user.getEmail());
        bio.getStyleClass().add("card-desc");

        info.getChildren().addAll(nameLine, bio);
        info.setMinWidth(0);
        info.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(info, Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        boolean alreadyPending = friendService.hasPendingRequestWith(user);

        Button btnAdd = new Button();
        if (alreadyPending) {
            btnAdd.setText("Pending");
            btnAdd.getStyleClass().add("btn-secondary");
        } else {
            com.dreamstop.util.IconUtil.styleButton(btnAdd, com.dreamstop.util.IconUtil.IconType.PLUS, "Add Friend", 12, "#FFFFFF");
            btnAdd.getStyleClass().add("btn-primary");
        }
        btnAdd.setDisable(alreadyPending);

        btnAdd.setOnAction(e -> {
            boolean sent = friendService.sendFriendRequest(user);
            if (sent) {
                btnAdd.setText("Pending");
                btnAdd.setGraphic(null);
                btnAdd.getStyleClass().setAll("btn-secondary");
                btnAdd.setDisable(true);
                NotificationUtil.showSuccess("Friend request sent to " + user.getFullName() + "!");
            }
        });

        card.getChildren().addAll(avatar, info, spacer, btnAdd);
        return card;
    }
}
