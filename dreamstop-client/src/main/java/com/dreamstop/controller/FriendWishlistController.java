package com.dreamstop.controller;

import com.dreamstop.model.User;
import com.dreamstop.model.WishlistItem;
import com.dreamstop.service.ContributionResult;
import com.dreamstop.service.WishlistService;
import com.dreamstop.util.NotificationUtil;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class FriendWishlistController implements Initializable {

    @FXML
    private Button btnBackToFriends;
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
        friendAvatarPane.setStyle("-fx-background-color: " + currentFriend.getAvatarColor()
                + "; -fx-background-radius: 50%; -fx-pref-width: 54px; -fx-pref-height: 54px; -fx-alignment: CENTER;");

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
            emoji.setStyle("-fx-font-size: 44px;");

            Label title = new Label(currentFriend.getFullName() + " hasn't added any items to their wishlist yet!");
            title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #475569;");

            Label sub = new Label("Check back later or invite them to share their wishes.");
            sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8;");

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

        Label iconLabel = new Label(item.getItem().getIconEmoji());
        iconLabel.setStyle("-fx-font-size: 26px; -fx-padding: 4px;");

        VBox titleBox = new VBox(3);
        HBox nameAndBadges = new HBox(8);
        nameAndBadges.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(item.getItem().getName());
        nameLabel.getStyleClass().add("card-title");

        Label catBadge = new Label(item.getItem().getCategory());
        catBadge.getStyleClass().add("badge");

        nameAndBadges.getChildren().addAll(nameLabel, catBadge);

        Label descLabel = new Label(item.getItem().getDescription());
        descLabel.getStyleClass().add("card-desc");

        titleBox.getChildren().addAll(nameAndBadges, descLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnContribute = new Button(item.isCompleted() ? "Completed 🎉" : "🎁 Contribute");
        btnContribute.getStyleClass().add(item.isCompleted() ? "btn-secondary" : "btn-primary");
        btnContribute.setDisable(item.isCompleted());
        btnContribute.setOnAction(e -> handleContribute(item));

        topRow.getChildren().addAll(iconLabel, titleBox, spacer, btnContribute);

        // Notes box if available
        if (item.getNotes() != null && !item.getNotes().trim().isEmpty()) {
            Label notesLabel = new Label("💡 " + currentFriend.getFullName() + "'s Note: " + item.getNotes());
            notesLabel.setStyle(
                    "-fx-font-size: 12px; -fx-text-fill: #475569; -fx-font-style: italic; -fx-background-color: #F8FAFC; -fx-padding: 6px 12px; -fx-background-radius: 6px;");
            card.getChildren().addAll(topRow, notesLabel);
        } else {
            card.getChildren().add(topRow);
        }

        // Progress Section
        VBox progressSection = new VBox(6);
        HBox progressLabels = new HBox();
        progressLabels.setAlignment(Pos.CENTER_LEFT);

        double pct = item.getProgressPercentage();
        Label fundedText = new Label("Funded: " + currencyFormat.format(item.getCurrentAmount()) + " / "
                + currencyFormat.format(item.getTargetAmount()) + " EGP");
        fundedText.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0F172A;");

        Region progSpacer = new Region();
        HBox.setHgrow(progSpacer, Priority.ALWAYS);

        Label pctText = new Label(String.format("%.0f%%", pct));
        pctText.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: "
                + (item.isCompleted() ? "#10B981;" : "#6366F1;"));

        if (item.isCompleted()) {
            Label compBadge = new Label("FULLY FUNDED 🎉");
            compBadge.getStyleClass().add("badge-completed");
            progressLabels.getChildren().addAll(fundedText, progSpacer, compBadge, new Label(" "), pctText);
        } else {
            Label remText = new Label("(" + currencyFormat.format(item.getRemainingAmount()) + " EGP remaining)");
            remText.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
            progressLabels.getChildren().addAll(fundedText, new Label("  "), remText, progSpacer, pctText);
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
        pane.getStylesheets().add(getClass().getResource("/com/dreamstop/css/styles.css").toExternalForm());

        VBox form = new VBox(12);
        form.setPrefWidth(440);
        form.setPadding(new Insets(16));

        double remaining = item.getRemainingAmount();

        // Remaining info row
        HBox remainingRow = new HBox(8);
        remainingRow.setAlignment(Pos.CENTER_LEFT);
        Label remainingLbl = new Label("Remaining to complete: " + currencyFormat.format(remaining) + " EGP");
        remainingLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #6366F1; -fx-font-size: 14px;");
        remainingRow.getChildren().add(remainingLbl);

        // Cap-warning label (hidden until the user types more than remaining)
        Label capWarningLbl = new Label();
        capWarningLbl.setStyle("-fx-text-fill: #D97706; -fx-font-size: 12px; -fx-font-weight: bold;");
        capWarningLbl.setVisible(false);
        capWarningLbl.setManaged(false);

        Label promptLbl = new Label("Enter contribution amount (EGP):");
        promptLbl.setStyle("-fx-font-weight: bold;");

        TextField txtAmount = new TextField(String.valueOf((int) Math.min(200, remaining)));
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
        HBox presetBtns = new HBox(8);
        presetBtns.setAlignment(Pos.CENTER_LEFT);
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
        pane.setContent(form);

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
