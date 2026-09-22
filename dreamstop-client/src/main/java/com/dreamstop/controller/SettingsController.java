package com.dreamstop.controller;

import com.dreamstop.model.User;
import com.dreamstop.network.NetworkClient;
import com.dreamstop.service.MockDataFactory;
import com.dreamstop.util.NotificationUtil;
import com.dreamstop.util.ThemeManager;
import com.dreamstop.util.UiStyleUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;

public class SettingsController implements Initializable {

    @FXML
    private VBox boxThemeDark;
    @FXML
    private VBox boxThemeLight;

    @FXML
    private StackPane avatarPreviewPane;
    @FXML
    private Label lblAvatarPreviewText;
    @FXML
    private HBox colorChipsContainer;

    @FXML
    private TextField txtFullName;
    @FXML
    private TextField txtUsername;
    @FXML
    private TextField txtBio;
    @FXML
    private TextField txtEmail;
    @FXML
    private Button btnSaveProfile;

    @FXML
    private Label lblWalletBalance;
    @FXML
    private TextField txtCustomRecharge;

    private String selectedAvatarColor = "#6366F1";
    private static double localWalletBalance = 15000.0;
    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.US);

    private static final String[] PALETTE = {
            "#6366F1", // Indigo
            "#10B981", // Emerald
            "#F59E0B", // Amber
            "#EC4899", // Rose
            "#8B5CF6", // Purple
            "#3B82F6", // Blue
            "#06B6D4", // Cyan
            "#EF4444"  // Red
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currencyFormat.setMinimumFractionDigits(2);
        currencyFormat.setMaximumFractionDigits(2);



        // 1. Initialize Theme Selector UI
        updateThemeCards(ThemeManager.getCurrentTheme());

        // 2. Load Active User Data
        User me = MockDataFactory.getCurrentUser();
        if (me != null) {
            txtFullName.setText(me.getFullName());
            txtUsername.setText("@" + me.getUsername());
            txtBio.setText(me.getBio() != null ? me.getBio() : "");
            txtEmail.setText(me.getEmail() != null ? me.getEmail() : "");
            selectedAvatarColor = me.getAvatarColor();
            updateAvatarPreview(me.getFullName(), selectedAvatarColor);

            // Dynamic live initials on name typing
            txtFullName.textProperty().addListener((obs, oldVal, newVal) -> {
                updateAvatarPreview(newVal, selectedAvatarColor);
            });
        }

        // 3. Render Color Chips
        renderColorChips();

        // 4. Initialize Wallet Balance Display
        updateWalletDisplay();
    }

    private void updateThemeCards(ThemeManager.Theme theme) {
        boxThemeDark.getStyleClass().removeAll("theme-choice-box-active", "theme-choice-box-inactive");
        boxThemeLight.getStyleClass().removeAll("theme-choice-box-active", "theme-choice-box-inactive");

        if (theme == ThemeManager.Theme.DARK) {
            boxThemeDark.getStyleClass().add("theme-choice-box-active");
            boxThemeLight.getStyleClass().add("theme-choice-box-inactive");
        } else {
            boxThemeLight.getStyleClass().add("theme-choice-box-active");
            boxThemeDark.getStyleClass().add("theme-choice-box-inactive");
        }
    }

    @FXML
    public void handleSelectDarkTheme() {
        ThemeManager.setTheme(ThemeManager.Theme.DARK);
        updateThemeCards(ThemeManager.Theme.DARK);
        NotificationUtil.showInfo("Switched to Dark Glassmorphism mode");
    }

    @FXML
    public void handleSelectLightTheme() {
        ThemeManager.setTheme(ThemeManager.Theme.LIGHT);
        updateThemeCards(ThemeManager.Theme.LIGHT);
        NotificationUtil.showInfo("Switched to Clean Light mode");
    }

    private void renderColorChips() {
        colorChipsContainer.getChildren().clear();
        for (String hex : PALETTE) {
            StackPane chip = new StackPane();
            chip.getStyleClass().add("avatar-chip");
            boolean isSelected = hex.equalsIgnoreCase(selectedAvatarColor);

            String baseStyle = "-fx-background-color: " + hex + "; "
                    + "-fx-background-radius: 50%; "
                    + "-fx-min-width: 32px; -fx-min-height: 32px; "
                    + "-fx-pref-width: 32px; -fx-pref-height: 32px; "
                    + "-fx-max-width: 32px; -fx-max-height: 32px; "
                    + "-fx-cursor: hand; ";

            if (isSelected) {
                chip.setStyle(baseStyle 
                        + "-fx-border-color: #ffffff; -fx-border-width: 3px; -fx-border-radius: 50%; "
                        + "-fx-effect: dropshadow(three-pass-box, rgba(255, 255, 255, 0.75), 8, 0, 0, 0);");
                Label check = new Label("✓");
                check.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
                chip.getChildren().setAll(check);
            } else {
                chip.setStyle(baseStyle 
                        + "-fx-border-color: rgba(255,255,255,0.2); -fx-border-width: 1px; -fx-border-radius: 50%;");
            }

            chip.setOnMouseClicked(e -> {
                selectedAvatarColor = hex;
                updateAvatarPreview(txtFullName.getText(), selectedAvatarColor);
                renderColorChips();
            });
            colorChipsContainer.getChildren().add(chip);
        }
    }

    private void updateAvatarPreview(String name, String color) {
        String initials = computeInitials(name);
        lblAvatarPreviewText.setText(initials);
        UiStyleUtil.applyAvatar(avatarPreviewPane, color, "avatar-circle-large");
    }

    private String computeInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "?";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        }
        return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
    }

    @FXML
    public void handleSaveProfile(ActionEvent event) {
        String newName = txtFullName.getText() != null ? txtFullName.getText().trim() : "";
        if (newName.isEmpty()) {
            NotificationUtil.showError("Full Name cannot be empty.");
            return;
        }

        User me = MockDataFactory.getCurrentUser();
        if (me != null) {
            me.setFullName(newName);
            me.setBio(txtBio.getText() != null ? txtBio.getText().trim() : "");
            me.setAvatarColor(selectedAvatarColor);

            // Propagate changes to MainDashboardController's active sidebar widget
            MainDashboardController dashboard = MainDashboardController.getInstance();
            if (dashboard != null) {
                dashboard.refreshUserProfileDisplay();
            }

            NotificationUtil.showSuccess("Profile updated successfully!");
        }
    }

    private void updateWalletDisplay() {
        lblWalletBalance.setText(currencyFormat.format(localWalletBalance) + " EGP");
    }

    @FXML
    public void handleQuickRecharge500(ActionEvent event) {
        performRecharge(500.0);
    }

    @FXML
    public void handleQuickRecharge1000(ActionEvent event) {
        performRecharge(1000.0);
    }

    @FXML
    public void handleQuickRecharge2500(ActionEvent event) {
        performRecharge(2500.0);
    }

    @FXML
    public void handleQuickRecharge5000(ActionEvent event) {
        performRecharge(5000.0);
    }

    @FXML
    public void handleCustomRecharge(ActionEvent event) {
        String input = txtCustomRecharge.getText() != null ? txtCustomRecharge.getText().trim() : "";
        if (input.isEmpty()) {
            NotificationUtil.showError("Please enter an amount to recharge.");
            return;
        }
        try {
            double amount = Double.parseDouble(input);
            if (amount <= 0) {
                NotificationUtil.showError("Recharge amount must be greater than 0.");
                return;
            }
            performRecharge(amount);
            txtCustomRecharge.clear();
        } catch (NumberFormatException e) {
            NotificationUtil.showError("Invalid amount format. Please enter a valid number.");
        }
    }

    private void performRecharge(double amount) {
        NetworkClient network = NetworkClient.getInstance();
        if (network.isConnected() && network.getCurrentUser() != null) {
            com.dreamstop.common.protocol.Request req = com.dreamstop.common.protocol.Request.of(
                    com.dreamstop.common.model.RequestType.RECHARGE_BALANCE,
                    BigDecimal.valueOf(amount)
            );
            network.sendRequestAsync(req).thenAccept(resp -> Platform.runLater(() -> {
                if (resp.isSuccess()) {
                    try {
                        BigDecimal newBal = resp.getDataAs(BigDecimal.class);
                        if (newBal != null) {
                            localWalletBalance = newBal.doubleValue();
                        } else {
                            localWalletBalance += amount;
                        }
                    } catch (Exception e) {
                        localWalletBalance += amount;
                    }
                    updateWalletDisplay();
                    NotificationUtil.showSuccess("Recharged +" + currencyFormat.format(amount) + " EGP successfully!");
                } else {
                    NotificationUtil.showError(resp.getMessage() != null ? resp.getMessage() : "Recharge failed.");
                }
            }));
        } else {
            // Local fallback balance update
            localWalletBalance += amount;
            updateWalletDisplay();
            NotificationUtil.showSuccess("Recharged +" + currencyFormat.format(amount) + " EGP successfully!");
        }
    }
}
