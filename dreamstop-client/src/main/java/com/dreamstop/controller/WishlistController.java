package com.dreamstop.controller;

import com.dreamstop.model.Item;
import com.dreamstop.model.WishlistItem;
import com.dreamstop.service.WishlistService;
import com.dreamstop.util.NotificationUtil;
import javafx.collections.ListChangeListener;
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
import java.util.stream.Collectors;

public class WishlistController implements Initializable {

    @FXML private Button btnAddItem;
    @FXML private Label lblStatTotalItems;
    @FXML private Label lblStatTotalValue;
    @FXML private Label lblStatTotalFunded;
    @FXML private Label lblStatCompleted;
    @FXML private TextField txtSearchWishlist;
    @FXML private Label lblItemsCountSubtitle;
    @FXML private VBox itemsContainer;

    private final WishlistService wishlistService = WishlistService.getInstance();
    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.US);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currencyFormat.setMaximumFractionDigits(0);

        renderWishlistItems("");
        updateStatistics();

        txtSearchWishlist.textProperty().addListener((obs, oldVal, newVal) -> {
            renderWishlistItems(newVal);
        });

        wishlistService.getMyWishlist().addListener((ListChangeListener<WishlistItem>) c -> {
            renderWishlistItems(txtSearchWishlist.getText());
            updateStatistics();
        });
    }

    private void updateStatistics() {
        int totalItems = wishlistService.getMyWishlist().size();
        double totalVal = wishlistService.getTotalWishlistValue();
        double fundedVal = wishlistService.getTotalFundedValue();
        long completed = wishlistService.getCompletedItemsCount();

        double pct = totalVal > 0 ? (fundedVal / totalVal) * 100.0 : 0;

        lblStatTotalItems.setText(String.valueOf(totalItems));
        lblStatTotalValue.setText(currencyFormat.format(totalVal) + " EGP");
        lblStatTotalFunded.setText(currencyFormat.format(fundedVal) + " EGP (" + String.format("%.0f", pct) + "%)");
        lblStatCompleted.setText(completed + " 🎉");
    }

    private void renderWishlistItems(String filter) {
        itemsContainer.getChildren().clear();

        String q = filter != null ? filter.trim().toLowerCase() : "";
        var filteredList = wishlistService.getMyWishlist().stream()
                .filter(w -> q.isEmpty() ||
                        w.getItem().getName().toLowerCase().contains(q) ||
                        w.getItem().getCategory().toLowerCase().contains(q) ||
                        (w.getNotes() != null && w.getNotes().toLowerCase().contains(q)))
                .collect(Collectors.toList());

        lblItemsCountSubtitle.setText("Showing " + filteredList.size() + " of " + wishlistService.getMyWishlist().size() + " items");

        if (filteredList.isEmpty()) {
            VBox emptyBox = new VBox(12);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(60, 20, 60, 20));

            Label emoji = new Label("🎁");
            emoji.setStyle("-fx-font-size: 48px;");

            Label title = new Label(q.isEmpty() ? "Your Wishlist is Empty!" : "No items match your search.");
            title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #475569;");

            Label sub = new Label(q.isEmpty() ? "Add items you'd love your friends to contribute to." : "Try a different search term or add a new item.");
            sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8;");

            emptyBox.getChildren().addAll(emoji, title, sub);
            if (q.isEmpty()) {
                Button btnEmptyAdd = new Button("✚  Add Your First Item");
                btnEmptyAdd.getStyleClass().add("btn-primary");
                btnEmptyAdd.setOnAction(this::handleOpenAddDialog);
                emptyBox.getChildren().add(btnEmptyAdd);
            }
            itemsContainer.getChildren().add(emptyBox);
            return;
        }

        for (WishlistItem item : filteredList) {
            itemsContainer.getChildren().add(createWishlistItemCard(item));
        }
    }

    private VBox createWishlistItemCard(WishlistItem item) {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");

        // Top Row: Emoji, Name, Category badge, Priority badge, Actions
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

        Label priorityBadge = new Label(item.getPriority() + " PRIORITY");
        priorityBadge.setStyle(getPriorityBadgeStyle(item.getPriority()));

        nameAndBadges.getChildren().addAll(nameLabel, catBadge, priorityBadge);

        Label descLabel = new Label(item.getItem().getDescription());
        descLabel.getStyleClass().add("card-desc");

        titleBox.getChildren().addAll(nameAndBadges, descLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnEdit = new Button("✏️ Edit");
        btnEdit.getStyleClass().add("btn-secondary");
        btnEdit.setOnAction(e -> handleEditItem(item));

        Button btnDelete = new Button("🗑️ Delete");
        btnDelete.getStyleClass().add("btn-danger");
        btnDelete.setOnAction(e -> handleDeleteItem(item));

        topRow.getChildren().addAll(iconLabel, titleBox, spacer, btnEdit, btnDelete);

        // Notes box if available
        if (item.getNotes() != null && !item.getNotes().trim().isEmpty()) {
            Label notesLabel = new Label("💡 Note: " + item.getNotes());
            notesLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569; -fx-font-style: italic; -fx-background-color: #F8FAFC; -fx-padding: 6px 12px; -fx-background-radius: 6px;");
            card.getChildren().addAll(topRow, notesLabel);
        } else {
            card.getChildren().add(topRow);
        }

        // Progress Section
        VBox progressSection = new VBox(6);
        HBox progressLabels = new HBox();
        progressLabels.setAlignment(Pos.CENTER_LEFT);

        double pct = item.getProgressPercentage();
        Label fundedText = new Label("Funded: " + currencyFormat.format(item.getCurrentAmount()) + " / " + currencyFormat.format(item.getTargetAmount()) + " EGP");
        fundedText.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0F172A;");

        Region progSpacer = new Region();
        HBox.setHgrow(progSpacer, Priority.ALWAYS);

        Label pctText = new Label(String.format("%.0f%%", pct));
        pctText.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: " + (item.isCompleted() ? "#10B981;" : "#6366F1;"));

        if (item.isCompleted()) {
            Label compBadge = new Label("COMPLETED 🎉");
            compBadge.getStyleClass().add("badge-completed");
            progressLabels.getChildren().addAll(fundedText, progSpacer, compBadge, new Label(" "), pctText);
        } else {
            Label remText = new Label("(" + currencyFormat.format(item.getRemainingAmount()) + " EGP left)");
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

    private String getPriorityBadgeStyle(String priority) {
        if ("HIGH".equalsIgnoreCase(priority)) {
            return "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2px 8px; -fx-background-radius: 10px;";
        } else if ("LOW".equalsIgnoreCase(priority)) {
            return "-fx-background-color: #F1F5F9; -fx-text-fill: #64748B; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2px 8px; -fx-background-radius: 10px;";
        }
        return "-fx-background-color: #FEF3C7; -fx-text-fill: #D97706; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2px 8px; -fx-background-radius: 10px;";
    }

    @FXML
    public void handleOpenAddDialog(ActionEvent event) {
        Dialog<WishlistItem> dialog = new Dialog<>();
        dialog.setTitle("Add Item to Wishlist");
        dialog.setHeaderText("Write your own dream wish or pick from the catalog!");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        pane.getStylesheets().add(getClass().getResource("/com/dreamstop/css/styles.css").toExternalForm());

        VBox form = new VBox(14);
        form.setPrefWidth(480);
        form.setPadding(new Insets(16));

        // Mode Switcher: Custom vs Catalog
        Label lblMode = new Label("Choose Item Source:");
        lblMode.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton rbCustom = new RadioButton("✍️ Write Custom Item");
        RadioButton rbCatalog = new RadioButton("📦 Pick from Catalog");
        rbCustom.setToggleGroup(modeGroup);
        rbCatalog.setToggleGroup(modeGroup);
        rbCustom.setSelected(true); // Default to custom as requested

        HBox modeBox = new HBox(20, rbCustom, rbCatalog);
        modeBox.setStyle("-fx-background-color: #F1F5F9; -fx-padding: 10px 14px; -fx-background-radius: 8px;");

        // --- Container 1: Custom Item Inputs ---
        VBox customBox = new VBox(10);
        Label lblCustomName = new Label("Item Name *:");
        lblCustomName.setStyle("-fx-font-weight: bold;");
        TextField txtCustomName = new TextField();
        txtCustomName.setPromptText("e.g. Mechanical Gaming Keyboard, RTX 5090, PS5 Controller...");
        txtCustomName.getStyleClass().add("text-field-modern");

        HBox catAndEmoji = new HBox(12);
        VBox catBox = new VBox(4);
        Label lblCat = new Label("Category:");
        lblCat.setStyle("-fx-font-weight: bold;");
        ComboBox<String> cbCategory = new ComboBox<>();
        cbCategory.getItems().addAll("Gaming", "Hardware", "Peripherals", "Console", "Steam", "Tech", "Other");
        cbCategory.getSelectionModel().select("Gaming");
        cbCategory.setMaxWidth(Double.MAX_VALUE);
        catBox.getChildren().addAll(lblCat, cbCategory);
        HBox.setHgrow(catBox, Priority.ALWAYS);

        VBox emojiBox = new VBox(4);
        Label lblEmoji = new Label("Icon:");
        lblEmoji.setStyle("-fx-font-weight: bold;");
        ComboBox<String> cbEmoji = new ComboBox<>();
        cbEmoji.getItems().addAll("🎁", "🎮", "💻", "🎧", "🖥️", "📱", "⚔️", "⚡", "🔥");
        cbEmoji.getSelectionModel().select("🎁");
        cbEmoji.setMaxWidth(Double.MAX_VALUE);
        emojiBox.getChildren().addAll(lblEmoji, cbEmoji);
        catAndEmoji.getChildren().addAll(catBox, emojiBox);

        customBox.getChildren().addAll(lblCustomName, txtCustomName, catAndEmoji);

        // --- Container 2: Catalog Inputs ---
        VBox catalogBox = new VBox(10);
        Label lblCatalog = new Label("Select Catalog Item:");
        lblCatalog.setStyle("-fx-font-weight: bold;");
        ComboBox<Item> cbCatalog = new ComboBox<>(wishlistService.getCatalog());
        cbCatalog.setMaxWidth(Double.MAX_VALUE);
        if (!wishlistService.getCatalog().isEmpty()) {
            cbCatalog.getSelectionModel().selectFirst();
        }
        catalogBox.getChildren().addAll(lblCatalog, cbCatalog);
        catalogBox.setVisible(false);
        catalogBox.setManaged(false);

        // --- Common Inputs: Target Amount, Priority, Notes ---
        Label lblPrice = new Label("Target Amount / Price (EGP) *:");
        lblPrice.setStyle("-fx-font-weight: bold;");
        TextField txtPrice = new TextField();
        txtPrice.setPromptText("e.g. 3500");
        txtPrice.getStyleClass().add("text-field-modern");

        // Priority
        Label lblPriority = new Label("Priority:");
        lblPriority.setStyle("-fx-font-weight: bold;");
        ComboBox<String> cbPriority = new ComboBox<>();
        cbPriority.getItems().addAll("HIGH", "MEDIUM", "LOW");
        cbPriority.getSelectionModel().select("HIGH");
        cbPriority.setMaxWidth(Double.MAX_VALUE);

        // Notes
        Label lblNotes = new Label("Notes / Specific Preferences:");
        lblNotes.setStyle("-fx-font-weight: bold;");
        TextField txtNotes = new TextField();
        txtNotes.setPromptText("e.g. Preferred color, size, model variation...");
        txtNotes.getStyleClass().add("text-field-modern");

        // Toggle behavior
        modeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            boolean isCustom = rbCustom.isSelected();
            customBox.setVisible(isCustom);
            customBox.setManaged(isCustom);
            catalogBox.setVisible(!isCustom);
            catalogBox.setManaged(!isCustom);

            if (!isCustom && cbCatalog.getValue() != null) {
                txtPrice.setText(String.valueOf((int) cbCatalog.getValue().getPrice()));
            }
        });

        cbCatalog.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && rbCatalog.isSelected()) {
                txtPrice.setText(String.valueOf((int) newVal.getPrice()));
            }
        });

        form.getChildren().addAll(lblMode, modeBox, customBox, catalogBox, lblPrice, txtPrice, lblPriority, cbPriority, lblNotes, txtNotes);
        pane.setContent(form);

        // Validate on OK button click before closing
        Button okButton = (Button) pane.lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, ae -> {
            if (rbCustom.isSelected()) {
                if (txtCustomName.getText() == null || txtCustomName.getText().trim().isEmpty()) {
                    ae.consume();
                    NotificationUtil.showWarning("Please enter an item name!");
                    txtCustomName.requestFocus();
                    return;
                }
            }

            String priceStr = txtPrice.getText() != null ? txtPrice.getText().trim() : "";
            if (priceStr.isEmpty()) {
                ae.consume();
                NotificationUtil.showWarning("Please enter a target amount!");
                txtPrice.requestFocus();
                return;
            }

            try {
                double p = Double.parseDouble(priceStr);
                if (p <= 0) {
                    ae.consume();
                    NotificationUtil.showWarning("Amount must be greater than 0 EGP!");
                    txtPrice.requestFocus();
                }
            } catch (NumberFormatException ex) {
                ae.consume();
                NotificationUtil.showWarning("Please enter a valid numeric amount!");
                txtPrice.requestFocus();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                double targetPrice;
                try {
                    targetPrice = Double.parseDouble(txtPrice.getText().trim());
                } catch (NumberFormatException e) {
                    targetPrice = 100.0;
                }

                if (rbCustom.isSelected()) {
                    String customName = txtCustomName.getText().trim();
                    String category = cbCategory.getValue();
                    String emoji = cbEmoji.getValue();
                    return wishlistService.addCustomItemToMyWishlist(
                            customName,
                            customName,
                            category,
                            targetPrice,
                            emoji,
                            cbPriority.getValue(),
                            txtNotes.getText().trim()
                    );
                } else {
                    Item selectedItem = cbCatalog.getValue();
                    return wishlistService.addItemToMyWishlist(
                            selectedItem,
                            txtNotes.getText().trim(),
                            targetPrice,
                            cbPriority.getValue()
                    );
                }
            }
            return null;
        });

        Optional<WishlistItem> result = dialog.showAndWait();
        result.ifPresent(item -> {
            NotificationUtil.showSuccess("Added \"" + item.getItem().getName() + "\" (" + currencyFormat.format(item.getTargetAmount()) + " EGP) to your wishlist!");
        });
    }

    private void handleEditItem(WishlistItem item) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit Wishlist Item");
        dialog.setHeaderText("Edit details for \"" + item.getItem().getName() + "\"");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        pane.getStylesheets().add(getClass().getResource("/com/dreamstop/css/styles.css").toExternalForm());

        VBox form = new VBox(14);
        form.setPrefWidth(440);
        form.setPadding(new Insets(16));

        Label lblPrice = new Label("Target Price (EGP):");
        lblPrice.setStyle("-fx-font-weight: bold;");
        TextField txtPrice = new TextField(String.valueOf((int) item.getTargetAmount()));
        txtPrice.getStyleClass().add("text-field-modern");

        Label lblPriority = new Label("Priority:");
        lblPriority.setStyle("-fx-font-weight: bold;");
        ComboBox<String> cbPriority = new ComboBox<>();
        cbPriority.getItems().addAll("HIGH", "MEDIUM", "LOW");
        cbPriority.setValue(item.getPriority());
        cbPriority.setMaxWidth(Double.MAX_VALUE);

        Label lblNotes = new Label("Notes / Preferences:");
        lblNotes.setStyle("-fx-font-weight: bold;");
        TextField txtNotes = new TextField(item.getNotes() != null ? item.getNotes() : "");
        txtNotes.getStyleClass().add("text-field-modern");

        form.getChildren().addAll(lblPrice, txtPrice, lblPriority, cbPriority, lblNotes, txtNotes);
        pane.setContent(form);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                double targetPrice;
                try {
                    targetPrice = Double.parseDouble(txtPrice.getText().trim());
                } catch (NumberFormatException e) {
                    targetPrice = item.getTargetAmount();
                }
                return wishlistService.updateWishlistItem(item, txtNotes.getText().trim(), targetPrice, cbPriority.getValue());
            }
            return false;
        });

        Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent() && result.get()) {
            NotificationUtil.showSuccess("Wishlist item updated successfully!");
        }
    }

    private void handleDeleteItem(WishlistItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Wishlist Item");
        alert.setHeaderText("Remove \"" + item.getItem().getName() + "\"?");
        alert.setContentText("Are you sure you want to remove this item from your wishlist? This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            wishlistService.deleteWishlistItem(item);
            NotificationUtil.showInfo("Removed \"" + item.getItem().getName() + "\" from your wishlist.");
        }
    }
}
