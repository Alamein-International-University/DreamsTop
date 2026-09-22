package com.dreamstop.controller;

import com.dreamstop.App;
import com.dreamstop.model.User;
import com.dreamstop.service.FriendService;
import com.dreamstop.service.MockDataFactory;
import com.dreamstop.util.NotificationUtil;
import com.dreamstop.util.UiStyleUtil;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainDashboardController implements Initializable {

    private static MainDashboardController instance;

    @FXML
    private StackPane contentArea;
    @FXML
    private StackPane toastOverlay;

    @FXML
    private Button btnNavWishlist;
    @FXML
    private Button btnNavFriends;
    @FXML
    private Label lblRequestsBadge;

    @FXML
    private StackPane userAvatarPane;
    @FXML
    private Label lblUserInitials;
    @FXML
    private Label lblUserName;
    @FXML
    private Label lblUserHandle;

    public static MainDashboardController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;

        // Register toast notification host
        NotificationUtil.registerToastContainer(toastOverlay);

        // Populate User Info
        User me = MockDataFactory.getCurrentUser();
        lblUserName.setText(me.getFullName());
        lblUserHandle.setText("@" + me.getUsername());
        lblUserInitials.setText(me.getInitials());
        UiStyleUtil.applyAvatar(userAvatarPane, me.getAvatarColor(), "avatar-circle-profile");

        // Refresh server state for the current user
        FriendService friendService = FriendService.getInstance();
        friendService.refreshState();
        com.dreamstop.service.WishlistService.getInstance().refreshMyWishlist();

        // Bind pending requests badge
        updateBadge(friendService.getIncomingRequests().size());
        friendService.getIncomingRequests().addListener((ListChangeListener<Object>) c -> {
            Platform.runLater(() -> updateBadge(friendService.getIncomingRequests().size()));
        });

        // Load Wishlist as default view
        handleNavWishlist(null);
    }

    private void updateBadge(int count) {
        if (count > 0) {
            lblRequestsBadge.setVisible(true);
            lblRequestsBadge.setManaged(true);
            lblRequestsBadge.setText(String.valueOf(count));
        } else {
            lblRequestsBadge.setVisible(false);
            lblRequestsBadge.setManaged(false);
        }
    }

    @FXML
    public void handleNavWishlist(ActionEvent event) {
        setActiveNav(btnNavWishlist, btnNavFriends);
        com.dreamstop.service.WishlistService.getInstance().refreshMyWishlist();
        loadView("wishlist_view.fxml");
    }

    @FXML
    public void handleNavFriends(ActionEvent event) {
        setActiveNav(btnNavFriends, btnNavWishlist);
        FriendService.getInstance().refreshState();
        loadView("friends_view.fxml");
    }

    @FXML
    public void handleSignOut(ActionEvent event) throws IOException {
        instance = null;
        com.dreamstop.network.NetworkClient.getInstance().setSessionToken(null);
        com.dreamstop.network.NetworkClient.getInstance().setCurrentUser(null);
        App.setRoot("login_view");
    }

    public void openFriendWishlist(User friend) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dreamstop/view/friend_wishlist_view.fxml"));
            Parent view = loader.load();
            FriendWishlistController controller = loader.getController();
            controller.setFriend(friend);

            contentArea.getChildren().setAll(view);
            btnNavWishlist.getStyleClass().setAll("nav-button");
            btnNavFriends.getStyleClass().setAll("nav-button-active");
        } catch (IOException e) {
            e.printStackTrace();
            NotificationUtil.showError("Failed to load friend's wishlist view: " + e.getMessage());
        }
    }

    private void setActiveNav(Button active, Button inactive) {
        active.getStyleClass().setAll("nav-button-active");
        inactive.getStyleClass().setAll("nav-button");
    }

    public void loadView(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dreamstop/view/" + fxmlFileName));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            NotificationUtil.showError("Failed to load view " + fxmlFileName + ": " + e.getMessage());
        }
    }
}
