package com.dreamstop.controller;

import com.dreamstop.model.Item;
import com.dreamstop.model.WishlistItem;
import com.dreamstop.service.WishlistService;
import com.dreamstop.util.NotificationUtil;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class WishlistController implements Initializable {

    @FXML
    private Button btnAddItem;
    @FXML
    private Label lblStatTotalItems;
    @FXML
    private Label lblStatTotalValue;
    @FXML
    private Label lblStatTotalFunded;
    @FXML
    private Label lblStatCompleted;
    @FXML
    private HBox statsCardsRow;
    @FXML
    private TextField txtSearchWishlist;
    @FXML
    private Label lblItemsCountSubtitle;
    @FXML
    private VBox itemsContainer;

    private final WishlistService wishlistService = WishlistService.getInstance();
    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.US);
    private final DecimalFormat compactNumberFormat = new DecimalFormat("0.#");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currencyFormat.setMaximumFractionDigits(0);

        if (!btnAddItem.getStyleClass().contains("btn-primary")) {
            btnAddItem.getStyleClass().add("btn-primary");
        }
        if (!btnAddItem.getStyleClass().contains("page-header-action")) {
            btnAddItem.getStyleClass().add("page-header-action");
        }

        statsCardsRow.widthProperty().addListener((obs, oldWidth, newWidth) -> updateStatistics());

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

        boolean compact = statsCardsRow.getWidth() > 0 && statsCardsRow.getWidth() < 760;
        setCompactStats(compact);

        lblStatTotalItems.setText(formatStatNumber(totalItems, compact));
        lblStatTotalValue.setText(formatStatNumber(totalVal, compact) + " EGP");
        lblStatTotalFunded.setText(formatStatNumber(fundedVal, compact) + " EGP (" + String.format("%.0f", pct) + "%)");
        lblStatCompleted.setText(formatStatNumber(completed, compact) + " 🎉");
    }

    private String formatStatNumber(double value, boolean compact) {
        if (!compact) {
            return currencyFormat.format(value);
        }
        double absoluteValue = Math.abs(value);
        if (absoluteValue >= 1_000_000) {
            return compactNumberFormat.format(value / 1_000_000) + "M";
        }
        if (absoluteValue >= 1_000) {
            return compactNumberFormat.format(value / 1_000) + "k";
        }
        return currencyFormat.format(value);
    }

    private void setCompactStats(boolean compact) {
        for (Label valueLabel : new Label[] {
                lblStatTotalItems, lblStatTotalValue, lblStatTotalFunded, lblStatCompleted
        }) {
            if (compact) {
                if (!valueLabel.getStyleClass().contains("stat-value-compact")) {
                    valueLabel.getStyleClass().add("stat-value-compact");
                }
            } else {
                valueLabel.getStyleClass().remove("stat-value-compact");
            }
        }
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

        lblItemsCountSubtitle
                .setText("Showing " + filteredList.size() + " of " + wishlistService.getMyWishlist().size() + " items");

        if (filteredList.isEmpty()) {
            VBox emptyBox = new VBox(12);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(60, 20, 60, 20));

            Label emoji = new Label("🎁");
            emoji.getStyleClass().add("empty-emoji-xlarge");

            Label title = new Label(q.isEmpty() ? "Your Wishlist is Empty!" : "No items match your search.");
            title.getStyleClass().add("empty-title-large");

            Label sub = new Label(q.isEmpty() ? "Add items you'd love your friends to contribute to."
                    : "Try a different search term or add a new item.");
            sub.getStyleClass().add("empty-subtitle");

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
        iconLabel.getStyleClass().add("card-icon");

        VBox titleBox = new VBox(3);
        FlowPane nameAndBadges = new FlowPane();
        nameAndBadges.setHgap(8);
        nameAndBadges.setVgap(4);
        nameAndBadges.setMaxWidth(Double.MAX_VALUE);

        Label nameLabel = new Label(item.getItem().getName());
        nameLabel.getStyleClass().add("card-title");

        Label catBadge = new Label(item.getItem().getCategory());
        catBadge.getStyleClass().add("badge");

        Label priorityBadge = new Label(item.getPriority() + " PRIORITY");
        priorityBadge.getStyleClass().add(getPriorityBadgeClass(item.getPriority()));

        nameAndBadges.getChildren().addAll(nameLabel, catBadge, priorityBadge);

        Label descLabel = new Label(item.getItem().getDescription());
        descLabel.getStyleClass().add("card-desc");

        titleBox.getChildren().addAll(nameAndBadges, descLabel);
        titleBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button btnEdit = new Button("✏️ Edit");
        btnEdit.getStyleClass().add("btn-secondary");
        btnEdit.setOnAction(e -> handleEditItem(item));

        Button btnDelete = new Button("🗑️ Delete");
        btnDelete.getStyleClass().add("btn-danger");
        btnDelete.setOnAction(e -> handleDeleteItem(item));

        topRow.getChildren().addAll(iconLabel, titleBox, btnEdit, btnDelete);

        // Notes box if available
        if (item.getNotes() != null && !item.getNotes().trim().isEmpty()) {
            Label notesLabel = new Label("💡 Note: " + item.getNotes());
            notesLabel.getStyleClass().add("notes-label");
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
            Label compBadge = new Label("COMPLETED 🎉");
            compBadge.getStyleClass().add("badge-completed");
            progressLabels.getChildren().addAll(fundedText, progressSpacer, compBadge, pctText);
        } else {
            Label remText = new Label("(" + currencyFormat.format(item.getRemainingAmount()) + " EGP left)");
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

    private String getPriorityBadgeClass(String priority) {
        if ("HIGH".equalsIgnoreCase(priority)) {
            return "priority-high";
        } else if ("LOW".equalsIgnoreCase(priority)) {
            return "priority-low";
        }
        return "priority-medium";
    }

    @FXML
    public void handleOpenAddDialog(ActionEvent event) {
        Dialog<WishlistItem> dialog = new Dialog<>();
        dialog.setTitle("Add Item to Wishlist");
        dialog.setHeaderText("Write your own dream wish or pick from the catalog!");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        pane.getStylesheets().add(getClass().getResource("/com/dreamstop/css/styles.css").toExternalForm());
        pane.getStyleClass().add("wishlist-dialog-pane");
        dialog.setResizable(true);
        pane.setMinWidth(500);
        pane.setMinHeight(500);
        pane.setPrefWidth(620);
        dialog.setOnShown(shownEvent -> {
            Stage window = (Stage) dialog.getDialogPane().getScene().getWindow();
            window.setMinWidth(540);
            window.setMinHeight(520);
        });

        VBox form = new VBox(14);
        form.getStyleClass().add("wishlist-dialog-form");
        form.setPrefWidth(560);
        form.setMaxWidth(Double.MAX_VALUE);
        form.setPadding(new Insets(16));

        // Mode Switcher: Custom vs Catalog
        Label lblMode = new Label("Choose Item Source:");
        lblMode.getStyleClass().add("dialog-field-label");

        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton rbCustom = new RadioButton("✍️ Write Custom Item");
        RadioButton rbCatalog = new RadioButton("📦 Pick from Catalog");
        rbCustom.setToggleGroup(modeGroup);
        rbCatalog.setToggleGroup(modeGroup);
        rbCustom.setSelected(true); // Default to custom as requested

        FlowPane modeBox = new FlowPane();
        modeBox.setHgap(20);
        modeBox.setVgap(8);
        modeBox.getChildren().addAll(rbCustom, rbCatalog);
        modeBox.getStyleClass().add("wishlist-mode-box");

        // --- Container 1: Custom Item Inputs ---
        VBox customBox = new VBox(10);
        Label lblCustomName = new Label("Item Name *:");
        lblCustomName.getStyleClass().add("dialog-field-label");
        TextField txtCustomName = new TextField();
        txtCustomName.setPromptText("e.g. Mechanical Gaming Keyboard, RTX 5090, PS5 Controller...");
        txtCustomName.setMaxWidth(Double.MAX_VALUE);
        txtCustomName.getStyleClass().add("text-field-modern");

        FlowPane catAndEmoji = new FlowPane();
        catAndEmoji.setHgap(12);
        catAndEmoji.setVgap(8);
        catAndEmoji.setMaxWidth(Double.MAX_VALUE);
        VBox catBox = new VBox(4);
        Label lblCat = new Label("Category:");
        lblCat.getStyleClass().add("dialog-field-label");
        ComboBox<String> cbCategory = new ComboBox<>();
        cbCategory.getStyleClass().add("combo-box-modern");
        cbCategory.getItems().addAll("Gaming", "Hardware", "Peripherals", "Console", "Steam", "Tech", "Other");
        cbCategory.getSelectionModel().select("Gaming");
        cbCategory.setMaxWidth(Double.MAX_VALUE);
        catBox.getChildren().addAll(lblCat, cbCategory);

        VBox emojiBox = new VBox(4);
        Label lblEmoji = new Label("Icon:");
        lblEmoji.getStyleClass().add("dialog-field-label");
        ComboBox<String> cbEmoji = new ComboBox<>();
        cbEmoji.getStyleClass().add("combo-box-modern");
        cbEmoji.getItems().addAll("🎁", "🎮", "💻", "🎧", "🖥️", "📱", "⚔️", "⚡", "🔥");
        cbEmoji.getSelectionModel().select("🎁");
        cbEmoji.setMaxWidth(Double.MAX_VALUE);
        emojiBox.getChildren().addAll(lblEmoji, cbEmoji);
        catAndEmoji.getChildren().addAll(catBox, emojiBox);

        customBox.getChildren().addAll(lblCustomName, txtCustomName, catAndEmoji);

        // --- Container 2: Catalog Inputs ---
        VBox catalogBox = new VBox(10);
        Label lblCatalog = new Label("Select Catalog Item:");
        lblCatalog.getStyleClass().add("dialog-field-label");
        ComboBox<Item> cbCatalog = new ComboBox<>(wishlistService.getCatalog());
        cbCatalog.getStyleClass().add("combo-box-modern");
        cbCatalog.setMaxWidth(Double.MAX_VALUE);
        if (!wishlistService.getCatalog().isEmpty()) {
            cbCatalog.getSelectionModel().selectFirst();
        }
        catalogBox.getChildren().addAll(lblCatalog, cbCatalog);
        catalogBox.setVisible(false);
        catalogBox.setManaged(false);

        // --- Common Inputs: Target Amount, Priority, Notes ---
        Label lblPrice = new Label("Target Amount / Price (EGP) *:");
        lblPrice.getStyleClass().add("dialog-field-label");
        TextField txtPrice = new TextField();
        txtPrice.setPromptText("e.g. 3500");
        txtPrice.setMaxWidth(Double.MAX_VALUE);
        txtPrice.getStyleClass().add("text-field-modern");

        // Priority
        Label lblPriority = new Label("Priority:");
        lblPriority.getStyleClass().add("dialog-field-label");
        ComboBox<String> cbPriority = new ComboBox<>();
        cbPriority.getStyleClass().add("combo-box-modern");
        cbPriority.getItems().addAll("HIGH", "MEDIUM", "LOW");
        cbPriority.getSelectionModel().select("HIGH");
        cbPriority.setMaxWidth(Double.MAX_VALUE);

        // Notes
        Label lblNotes = new Label("Notes / Specific Preferences:");
        lblNotes.getStyleClass().add("dialog-field-label");
        TextField txtNotes = new TextField();
        txtNotes.setPromptText("e.g. Preferred color, size, model variation...");
        txtNotes.setMaxWidth(Double.MAX_VALUE);
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

        form.getChildren().addAll(lblMode, modeBox, customBox, catalogBox, lblPrice, txtPrice, lblPriority, cbPriority,
                lblNotes, txtNotes);
        ScrollPane formScroll = new ScrollPane(form);
        formScroll.setFitToWidth(true);
        formScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        formScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        formScroll.setPannable(true);
        formScroll.getStyleClass().add("dialog-form-scroll");
        form.prefWidthProperty().bind(Bindings.createDoubleBinding(
                () -> formScroll.getViewportBounds().getWidth(), formScroll.viewportBoundsProperty()));
        catAndEmoji.prefWrapLengthProperty().bind(Bindings.createDoubleBinding(
                () -> formScroll.getViewportBounds().getWidth(), formScroll.viewportBoundsProperty()));
        formScroll.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            double availableWidth = newBounds.getWidth();
            double columnWidth = Math.max(140, (availableWidth - catAndEmoji.getHgap()) / 2);
            catBox.setPrefWidth(columnWidth);
            emojiBox.setPrefWidth(columnWidth);
        });
        pane.setContent(formScroll);

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
                            txtNotes.getText().trim());
                } else {
                    Item selectedItem = cbCatalog.getValue();
                    return wishlistService.addItemToMyWishlist(
                            selectedItem,
                            txtNotes.getText().trim(),
                            targetPrice,
                            cbPriority.getValue());
                }
            }
            return null;
        });

        Optional<WishlistItem> result = dialog.showAndWait();
        result.ifPresent(item -> {
            NotificationUtil.showSuccess("Added \"" + item.getItem().getName() + "\" ("
                    + currencyFormat.format(item.getTargetAmount()) + " EGP) to your wishlist!");
        });
    }

    private void handleEditItem(WishlistItem item) {
        Dialog<Boolean> dialog = new Dialog<>();

        dialog.setTitle("Edit Wishlist Item");
        dialog.setHeaderText("Edit details for \"" + item.getItem().getName() + "\"");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Use the main stylesheet
        pane.getStylesheets().add(
                getClass().getResource("/com/dreamstop/css/styles.css").toExternalForm());

        // Dialog styling
        pane.getStyleClass().add("wishlist-dialog-pane");

        dialog.setResizable(true);

        pane.setMinWidth(440);
        pane.setPrefWidth(520);
        pane.setMinHeight(350);

        dialog.setOnShown(shownEvent -> {
            Stage window = (Stage) dialog.getDialogPane().getScene().getWindow();
            window.setMinWidth(480);
            window.setMinHeight(400);
        });

        // ============================
        // Form
        // ============================

        VBox form = new VBox(14);
        form.getStyleClass().add("wishlist-dialog-form");
        form.setPadding(new Insets(16));
        form.setMaxWidth(Double.MAX_VALUE);

        // Target Price
        Label lblPrice = new Label("Target Price (EGP):");
        lblPrice.getStyleClass().add("dialog-field-label");

        TextField txtPrice = new TextField(
                String.valueOf((int) item.getTargetAmount()));
        txtPrice.setPromptText("e.g. 3500");
        txtPrice.setMaxWidth(Double.MAX_VALUE);
        txtPrice.getStyleClass().add("text-field-modern");

        // Priority
        Label lblPriority = new Label("Priority:");
        lblPriority.getStyleClass().add("dialog-field-label");

        ComboBox<String> cbPriority = new ComboBox<>();
        cbPriority.getStyleClass().add("combo-box-modern");
        cbPriority.getItems().addAll("HIGH", "MEDIUM", "LOW");
        cbPriority.setValue(item.getPriority());
        cbPriority.setMaxWidth(Double.MAX_VALUE);

        // Notes
        Label lblNotes = new Label("Notes / Preferences:");
        lblNotes.getStyleClass().add("dialog-field-label");

        TextField txtNotes = new TextField(
                item.getNotes() != null ? item.getNotes() : "");
        txtNotes.setPromptText("e.g. Preferred color, size, model variation...");
        txtNotes.setMaxWidth(Double.MAX_VALUE);
        txtNotes.getStyleClass().add("text-field-modern");

        form.getChildren().addAll(
                lblPrice,
                txtPrice,
                lblPriority,
                cbPriority,
                lblNotes,
                txtNotes);

        // ============================
        // Scroll container
        // ============================

        ScrollPane formScroll = new ScrollPane(form);
        formScroll.setFitToWidth(true);
        formScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        formScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        formScroll.setPannable(true);
        formScroll.getStyleClass().add("dialog-form-scroll");

        pane.setContent(formScroll);

        // ============================
        // Validation
        // ============================

        Button okButton = (Button) pane.lookupButton(ButtonType.OK);

        okButton.addEventFilter(ActionEvent.ACTION, ae -> {
            String priceStr = txtPrice.getText() != null
                    ? txtPrice.getText().trim()
                    : "";

            if (priceStr.isEmpty()) {
                ae.consume();
                NotificationUtil.showWarning("Please enter a target amount!");
                txtPrice.requestFocus();
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);

                if (price <= 0) {
                    ae.consume();
                    NotificationUtil.showWarning(
                            "Amount must be greater than 0 EGP!");
                    txtPrice.requestFocus();
                }

            } catch (NumberFormatException ex) {
                ae.consume();
                NotificationUtil.showWarning(
                        "Please enter a valid numeric amount!");
                txtPrice.requestFocus();
            }
        });

        // ============================
        // Result
        // ============================

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                double targetPrice;

                try {
                    targetPrice = Double.parseDouble(
                            txtPrice.getText().trim());
                } catch (NumberFormatException e) {
                    targetPrice = item.getTargetAmount();
                }

                return wishlistService.updateWishlistItem(
                        item,
                        txtNotes.getText().trim(),
                        targetPrice,
                        cbPriority.getValue());
            }

            return false;
        });

        Optional<Boolean> result = dialog.showAndWait();

        if (result.isPresent() && result.get()) {
            NotificationUtil.showSuccess(
                    "Wishlist item updated successfully!");
        }
    }

    private void handleDeleteItem(WishlistItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Wishlist Item");
        alert.setHeaderText("Remove \"" + item.getItem().getName() + "\"?");
        alert.setContentText(
                "Are you sure you want to remove this item from your wishlist? This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            wishlistService.deleteWishlistItem(item);
            NotificationUtil.showInfo("Removed \"" + item.getItem().getName() + "\" from your wishlist.");
        }
    }
}
