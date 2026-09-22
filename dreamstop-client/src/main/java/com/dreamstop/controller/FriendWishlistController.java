package com.dreamstop.controller;

import com.dreamstop.common.model.NotificationType;
import com.dreamstop.common.protocol.ServerNotification;
import com.dreamstop.model.User;
import com.dreamstop.model.WishlistItem;
import com.dreamstop.network.NetworkClient;
import com.dreamstop.service.ContributionResult;
import com.dreamstop.service.WishlistService;
import com.dreamstop.util.NotificationUtil;
import com.dreamstop.util.UiStyleUtil;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class FriendWishlistController implements Initializable {

    @FXML
    private StackPane friendAvatarPane;
    @FXML
    private Label lblFriendInitials;
    @FXML
    private Label lblFriendName;
    @FXML
    private Label lblFriendUsername;
    @FXML
    private Label lblFriendBio;
    @FXML
    private Label lblFriendItemsCount;
    @FXML
    private Label lblFriendFundingSummary;
    @FXML
    private VBox friendItemsContainer;

    private User currentFriend;
    private final WishlistService wishlistService = WishlistService.getInstance();
    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.US);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currencyFormat.setMaximumFractionDigits(0);

        NetworkClient.getInstance().addNotificationListener(notification -> {
            if (notification == null) return;
            NotificationType type = notification.getType();
            if (type == NotificationType.CONTRIBUTION_RECEIVED
                    || type == NotificationType.ITEM_COMPLETED_RECEIVER
                    || type == NotificationType.ITEM_COMPLETED_BUYER) {
                Platform.runLater(() -> {
                    if (currentFriend != null) {
                        updateFriendHeader();
                        renderFriendItems();
                    }
                });
            }
        });
    }

    public void setFriend(User friend) {
        this.currentFriend = friend;
        updateFriendHeader();
        renderFriendItems();
    }

    private void updateFriendHeader() {
        if (currentFriend == null)
            return;

        lblFriendName.setText(currentFriend.getFullName());
        lblFriendUsername.setText("@" + currentFriend.getUsername());
        lblFriendBio.setText(currentFriend.getBio() != null ? currentFriend.getBio() : currentFriend.getEmail());
        lblFriendInitials.setText(currentFriend.getInitials());
        UiStyleUtil.applyAvatar(friendAvatarPane, currentFriend.getAvatarColor(), "avatar-circle-large");

        ObservableList<WishlistItem> items = wishlistService.getFriendWishlist(currentFriend);
        double totalTarget = items.stream().mapToDouble(WishlistItem::getTargetAmount).sum();
        double totalFunded = items.stream().mapToDouble(WishlistItem::getCurrentAmount).sum();
        double pct = totalTarget > 0 ? (totalFunded / totalTarget) * 100.0 : 0;

        lblFriendItemsCount.setText(items.size() + (items.size() == 1 ? " Item" : " Items") + " on Wishlist");
        lblFriendFundingSummary.setText(currencyFormat.format(totalFunded) + " / " + currencyFormat.format(totalTarget)
                + " EGP Funded (" + String.format("%.0f", pct) + "%)");
    }

    private void renderFriendItems() {
        friendItemsContainer.getChildren().clear();

        if (currentFriend == null)
            return;

        ObservableList<WishlistItem> items = wishlistService.getFriendWishlist(currentFriend);

        if (items.isEmpty()) {
            VBox emptyBox = new VBox(12);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50, 20, 50, 20));

            Label emoji = new Label("🎁");
            emoji.getStyleClass().add("empty-emoji-large");

            Label title = new Label(currentFriend.getFullName() + " hasn't added any items to their wishlist yet!");
            title.getStyleClass().add("empty-title");

            Label sub = new Label("Check back later or invite them to share their wishes.");
            sub.getStyleClass().add("empty-subtitle");

            emptyBox.getChildren().addAll(emoji, title, sub);
            friendItemsContainer.getChildren().add(emptyBox);
            return;
        }

        for (WishlistItem item : items) {
            friendItemsContainer.getChildren().add(createFriendWishlistItemCard(item));
        }
    }

    private VBox createFriendWishlistItemCard(WishlistItem item) {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");

        // Top Row: Emoji, Name, Category badge, Priority badge, Contribute Action
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.setMaxWidth(Double.MAX_VALUE);

        StackPane iconTile = new StackPane();
        iconTile.getStyleClass().add("card-icon-tile");
        String emoji = item.getItem().getIconEmoji() != null ? item.getItem().getIconEmoji() : "🎁";
        Label iconLabel = new Label(emoji);
        iconLabel.setStyle("-fx-font-size: 20px;");
        iconTile.getChildren().add(iconLabel);

        VBox titleBox = new VBox(4);
        FlowPane nameAndBadges = new FlowPane();
        nameAndBadges.setHgap(8);
        nameAndBadges.setVgap(4);
        nameAndBadges.setMaxWidth(Double.MAX_VALUE);

        Label nameLabel = new Label(item.getItem().getName());
        nameLabel.getStyleClass().add("card-title");

        Label catBadge = new Label(item.getItem().getCategory());
        catBadge.getStyleClass().add("badge");

        nameAndBadges.getChildren().addAll(nameLabel, catBadge);

        Label descLabel = new Label(item.getItem().getDescription());
        descLabel.getStyleClass().add("card-desc");
        descLabel.setWrapText(true);
        descLabel.setMinWidth(0);
        descLabel.setMaxWidth(Double.MAX_VALUE);

        titleBox.getChildren().addAll(nameAndBadges, descLabel);
        titleBox.setMinWidth(0);
        titleBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button btnContribute = new Button();
        if (item.isCompleted()) {
            btnContribute.setText("Fully Funded");
            btnContribute.getStyleClass().add("btn-secondary");
        } else {
            btnContribute.setText("Contribute");
            btnContribute.getStyleClass().add("btn-primary");
        }
        btnContribute.getStyleClass().add("contribute-button");
        btnContribute.setMinWidth(160);
        btnContribute.setPrefWidth(160);
        btnContribute.setMaxWidth(160);
        btnContribute.setDisable(item.isCompleted());
        btnContribute.setOnAction(e -> handleContribute(item));

        topRow.getChildren().addAll(iconTile, titleBox, btnContribute);

        // Notes box if available
        if (item.getNotes() != null && !item.getNotes().trim().isEmpty()) {
            Label notesLabel = new Label(currentFriend.getFullName() + "'s Note: " + item.getNotes());
            notesLabel.getStyleClass().add("notes-callout");
            notesLabel.setWrapText(true);
            notesLabel.setMaxWidth(Double.MAX_VALUE);
            card.getChildren().addAll(topRow, notesLabel);
        } else {
            card.getChildren().add(topRow);
        }

        // Progress Section
        VBox progressSection = new VBox(6);
        HBox progressLabels = new HBox(8);
        progressLabels.setAlignment(Pos.CENTER_LEFT);
        progressLabels.setMaxWidth(Double.MAX_VALUE);
        progressLabels.getStyleClass().add("progress-labels");

        double pct = item.getProgressPercentage();
        Label fundedText = new Label("Funded: " + currencyFormat.format(item.getCurrentAmount()) + " / "
                + currencyFormat.format(item.getTargetAmount()) + " EGP");
        fundedText.getStyleClass().add("funded-value");
        fundedText.setWrapText(true);
        fundedText.setMinWidth(0);
        fundedText.setMaxWidth(Double.MAX_VALUE);

        Region progressSpacer = new Region();
        HBox.setHgrow(fundedText, Priority.ALWAYS);
        HBox.setHgrow(progressSpacer, Priority.ALWAYS);

        Label pctText = new Label(String.format("%.0f%%", pct));
        pctText.getStyleClass().add(item.isCompleted() ? "percent-complete" : "percent-active");
        pctText.setMinWidth(44);
        pctText.setPrefWidth(44);
        pctText.setMaxWidth(44);
        pctText.setAlignment(Pos.CENTER_RIGHT);

        if (item.isCompleted()) {
            Label compBadge = new Label("FULLY FUNDED");
            compBadge.getStyleClass().add("badge-completed");
            progressLabels.getChildren().addAll(fundedText, progressSpacer, compBadge, pctText);
        } else {
            Label remText = new Label("(" + currencyFormat.format(item.getRemainingAmount()) + " EGP remaining)");
            remText.getStyleClass().add("remaining-label");
            remText.setWrapText(true);
            remText.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(remText, Priority.ALWAYS);
            progressLabels.getChildren().addAll(fundedText, remText, progressSpacer, pctText);
        }

        ProgressBar pb = new ProgressBar(item.getProgressRatio());
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.getStyleClass().add("progress-bar-modern");
        if (item.isCompleted()) {
            pb.getStyleClass().add("progress-bar-completed");
        }

        progressSection.getChildren().addAll(progressLabels, pb);
        card.getChildren().add(progressSection);

        return card;
    }

    private void handleContribute(WishlistItem item) {
        // Guard: item is already fully funded
        if (item.isCompleted()) {
            NotificationUtil.showInfo(item.getItem().getName() + " is already fully funded. No contributions needed!");
            return;
        }

        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Contribute to Gift");
        dialog.setHeaderText(
                "Contribute towards " + currentFriend.getFullName() + "'s \"" + item.getItem().getName() + "\"");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        com.dreamstop.util.ThemeManager.styleDialog(dialog);
        dialog.setResizable(true);
        pane.setMinWidth(460);
        pane.setMinHeight(360);
        pane.setPrefWidth(540);
        dialog.setOnShown(event -> {
            Stage window = (Stage) dialog.getDialogPane().getScene().getWindow();
            window.setMinWidth(500);
            window.setMinHeight(380);
        });

        VBox form = new VBox(12);
        form.getStyleClass().add("wishlist-dialog-form");
        form.setPrefWidth(480);
        form.setMaxWidth(Double.MAX_VALUE);
        form.setPadding(new Insets(16));

        double remaining = item.getRemainingAmount();

        // Remaining info row
        HBox remainingRow = new HBox(8);
        remainingRow.setAlignment(Pos.CENTER_LEFT);
        Label remainingLbl = new Label("Remaining to complete: " + currencyFormat.format(remaining) + " EGP");
        remainingLbl.getStyleClass().add("contribution-remaining");
        remainingRow.getChildren().add(remainingLbl);

        // Cap-warning label (hidden until the user types more than remaining)
        Label capWarningLbl = new Label();
        capWarningLbl.getStyleClass().add("dialog-warning");
        capWarningLbl.setVisible(false);
        capWarningLbl.setManaged(false);

        Label promptLbl = new Label("Enter contribution amount (EGP):");
        promptLbl.getStyleClass().add("dialog-field-label");

        TextField txtAmount = new TextField(String.valueOf((int) Math.min(200, remaining)));
        txtAmount.setMaxWidth(Double.MAX_VALUE);
        txtAmount.getStyleClass().add("text-field-modern");

        // Live validation: warn & show refund preview when over the goal
        txtAmount.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                double entered = Double.parseDouble(newVal.trim());
                if (entered > remaining) {
                    double excess = entered - remaining;
                    capWarningLbl.setText("⚠️  Only " + currencyFormat.format(remaining) + " EGP needed — "
                            + currencyFormat.format(excess) + " EGP will be refunded.");
                    capWarningLbl.setVisible(true);
                    capWarningLbl.setManaged(true);
                } else {
                    capWarningLbl.setVisible(false);
                    capWarningLbl.setManaged(false);
                }
            } catch (NumberFormatException ignored) {
                capWarningLbl.setVisible(false);
                capWarningLbl.setManaged(false);
            }
        });

        // Preset buttons (capped to remaining so they never show more than needed)
        FlowPane presetBtns = new FlowPane();
        presetBtns.setHgap(8);
        presetBtns.setVgap(8);
        presetBtns.setMaxWidth(Double.MAX_VALUE);
        int[] presets = { 100, 250, 500, 1000 };
        for (int p : presets) {
            Button b = new Button("+" + p);
            b.getStyleClass().add("btn-secondary");
            b.setOnAction(e -> txtAmount.setText(String.valueOf(p)));
            presetBtns.getChildren().add(b);
        }
        // Quick-fill button for exact remaining amount
        Button btnExact = new Button("Exact: " + currencyFormat.format(remaining) + " EGP");
        btnExact.getStyleClass().add("btn-success");
        btnExact.setOnAction(e -> {
            txtAmount.setText(String.valueOf((int) remaining));
        });

        form.getChildren().addAll(remainingRow, capWarningLbl, promptLbl, txtAmount, presetBtns, btnExact);
        ScrollPane formScroll = new ScrollPane(form);
        formScroll.setFitToWidth(true);
        formScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        formScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        formScroll.setPannable(true);
        formScroll.getStyleClass().add("dialog-form-scroll");
        form.prefWidthProperty().bind(Bindings.createDoubleBinding(
                () -> formScroll.getViewportBounds().getWidth(), formScroll.viewportBoundsProperty()));
        pane.setContent(formScroll);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    return Double.parseDouble(txtAmount.getText().trim());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
            return null;
        });

        Optional<Double> result = dialog.showAndWait();
        result.ifPresent(amount -> {
            if (amount <= 0)
                return;

            ContributionResult contribution = wishlistService.contributeToFriendItem(item, amount);
            if (contribution == null) {
                NotificationUtil.showError("This item is already fully funded!");
                return;
            }

            updateFriendHeader();
            renderFriendItems();

            if (contribution.wasRefunded()) {
                // Goal reached but user sent too much — show refund notice
                NotificationUtil.showSuccess(
                        "🎉 Fully funded! " + currencyFormat.format(contribution.acceptedAmount())
                                + " EGP accepted — " + currencyFormat.format(contribution.refundedAmount())
                                + " EGP refunded to you.");
            } else if (item.isCompleted()) {
                // Contribution hit exactly 100%
                NotificationUtil.showSuccess(
                        "🎉 You just fully funded " + item.getItem().getName()
                                + " for " + currentFriend.getFullName() + "!");
            } else {
                // Partial contribution
                NotificationUtil.showSuccess(
                        "Contributed " + currencyFormat.format(contribution.acceptedAmount())
                                + " EGP towards " + currentFriend.getFullName() + "'s gift!");
            }
        });
    }

    @FXML
    public void handleBack(ActionEvent event) {
        if (MainDashboardController.getInstance() != null) {
            MainDashboardController.getInstance().handleNavFriends(event);
        }
    }
}
