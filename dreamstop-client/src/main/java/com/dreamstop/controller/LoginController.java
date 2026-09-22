package com.dreamstop.controller;

import com.dreamstop.App;
import com.dreamstop.common.dto.AuthResultDTO;
import com.dreamstop.common.dto.LoginRequestDTO;
import com.dreamstop.common.dto.RegisterRequestDTO;
import com.dreamstop.common.dto.UserDTO;
import com.dreamstop.common.model.RequestType;
import com.dreamstop.common.protocol.Request;
import com.dreamstop.model.User;
import com.dreamstop.network.NetworkClient;
import com.dreamstop.service.MockDataFactory;
import com.dreamstop.util.NotificationUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private StackPane toastOverlay;

    @FXML
    private Button btnTabSignIn;
    @FXML
    private Button btnTabRegister;

    @FXML
    private TextField txtUsername;
    @FXML
    private VBox boxEmail;
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblMessage;
    @FXML
    private Button btnPrimaryAction;

    @FXML
    private StackPane loginLogoBox;
    @FXML
    private Label lblStatusDot;
    @FXML
    private Label lblServerStatus;

    private boolean isRegisterMode = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (loginLogoBox != null) {
            loginLogoBox.getChildren().setAll(com.dreamstop.util.AppConfig.createBrandLogo(54));
        }

        NotificationUtil.registerToastContainer(toastOverlay);

        String disconnectReason = NetworkClient.getAndClearLastDisconnectReason();
        if (disconnectReason != null) {
            showError(disconnectReason);
        }

        // Attempt background socket connection to server
        Thread initThread = new Thread(this::checkAndConnectServer, "network-client-init");
        initThread.setDaemon(true);
        initThread.start();
    }

    private boolean ensureConnected() {
        NetworkClient network = NetworkClient.getInstance();
        if (!network.isConnected()) {
            try {
                network.connect();
            } catch (Exception ignored) {
            }
        }
        boolean connected = network.isConnected();
        Platform.runLater(() -> updateServerStatusUi(connected));
        return connected;
    }

    private void checkAndConnectServer() {
        ensureConnected();
    }

    private void updateServerStatusUi(boolean connected) {
        if (connected) {
            lblStatusDot.setStyle("-fx-text-fill: #10B981; -fx-font-size: 10px;");
            lblServerStatus.setText("Connected to Server (127.0.0.1:5005)");
            lblServerStatus.setStyle("-fx-text-fill: #10B981; -fx-font-size: 11px;");
        } else {
            lblStatusDot.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 10px;");
            lblServerStatus.setText("Server Offline (Cannot connect to 127.0.0.1:5005)");
            lblServerStatus.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 11px;");
        }
    }

    @FXML
    public void handleSwitchToSignIn(ActionEvent event) {
        isRegisterMode = false;
        btnTabSignIn.setStyle(
                "-fx-background-color: #6366F1; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-padding: 8px; -fx-cursor: hand;");
        btnTabRegister.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #94A3B8; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-padding: 8px; -fx-cursor: hand;");

        boxEmail.setVisible(false);
        boxEmail.setManaged(false);
        btnPrimaryAction.setText("Sign In to DreamsTop");
        hideMessage();
    }

    @FXML
    public void handleSwitchToRegister(ActionEvent event) {
        isRegisterMode = true;
        btnTabRegister.setStyle(
                "-fx-background-color: #6366F1; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-padding: 8px; -fx-cursor: hand;");
        btnTabSignIn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #94A3B8; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-padding: 8px; -fx-cursor: hand;");

        boxEmail.setVisible(true);
        boxEmail.setManaged(true);
        btnPrimaryAction.setText("Create DreamsTop Account");
        hideMessage();
    }

    @FXML
    public void handlePrimaryAction() {
        String username = txtUsername.getText() != null ? txtUsername.getText().trim() : "";
        String password = txtPassword.getText() != null ? txtPassword.getText() : "";

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password cannot be empty");
            return;
        }

        if (isRegisterMode) {
            String email = txtEmail.getText() != null ? txtEmail.getText().trim() : "";
            if (email.isEmpty()) {
                showError("Email address is required for registration");
                return;
            }
            performRegister(username, email, password);
        } else {
            performLogin(username, password);
        }
    }

    private void performLogin(String username, String password) {
        btnPrimaryAction.setDisable(true);
        btnPrimaryAction.setText("Signing in...");

        if (!ensureConnected()) {
            btnPrimaryAction.setDisable(false);
            btnPrimaryAction.setText("Sign In to DreamsTop");
            showError("Server is offline. Please start the server and try again.");
            return;
        }

        NetworkClient network = NetworkClient.getInstance();
        Request loginReq = Request.of(RequestType.LOGIN, new LoginRequestDTO(username, password));
        network.sendRequestAsync(loginReq).thenAccept(response -> Platform.runLater(() -> {
            btnPrimaryAction.setDisable(false);
            btnPrimaryAction.setText("Sign In to DreamsTop");

            if (response.isSuccess()) {
                AuthResultDTO authResult = response.getDataAs(AuthResultDTO.class);
                if (authResult != null) {
                    network.setSessionToken(authResult.getToken());
                    network.setCurrentUser(authResult.getUser());
                    syncLocalUser(authResult.getUser());
                    navigateToDashboard();
                }
            } else {
                showError(response.getMessage() != null ? response.getMessage() : "Invalid credentials");
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                btnPrimaryAction.setDisable(false);
                btnPrimaryAction.setText("Sign In to DreamsTop");
                showError("Connection error: " + ex.getMessage());
            });
            return null;
        });
    }

    private void performRegister(String username, String email, String password) {
        btnPrimaryAction.setDisable(true);
        btnPrimaryAction.setText("Creating account...");

        if (!ensureConnected()) {
            btnPrimaryAction.setDisable(false);
            btnPrimaryAction.setText("Create DreamsTop Account");
            showError("Server is offline. Please start the server and try again.");
            return;
        }

        NetworkClient network = NetworkClient.getInstance();
        RegisterRequestDTO regDto = new RegisterRequestDTO(username, email, password, new BigDecimal("1000.00"));
        Request regReq = Request.of(RequestType.REGISTER, regDto);
        network.sendRequestAsync(regReq).thenAccept(response -> Platform.runLater(() -> {
            btnPrimaryAction.setDisable(false);
            btnPrimaryAction.setText("Create DreamsTop Account");

            if (response.isSuccess()) {
                AuthResultDTO authResult = response.getDataAs(AuthResultDTO.class);
                if (authResult != null) {
                    network.setSessionToken(authResult.getToken());
                    network.setCurrentUser(authResult.getUser());
                    syncLocalUser(authResult.getUser());
                    navigateToDashboard();
                }
            } else {
                showError(response.getMessage() != null ? response.getMessage() : "Registration failed");
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                btnPrimaryAction.setDisable(false);
                btnPrimaryAction.setText("Create DreamsTop Account");
                showError("Connection error: " + ex.getMessage());
            });
            return null;
        });
    }

    @FXML
    public void handleQuickLoginKady() {
        txtUsername.setText("kady_x");
        txtPassword.setText("password123");
        handlePrimaryAction();
    }

    @FXML
    public void handleQuickLoginYousef() {
        txtUsername.setText("tarnished693");
        txtPassword.setText("password123");
        handlePrimaryAction();
    }

    @FXML
    public void handleQuickLoginAdham() {
        txtUsername.setText("adham_hatem");
        txtPassword.setText("password123");
        handlePrimaryAction();
    }

    private void syncLocalUser(UserDTO dto) {
        if (dto == null)
            return;
        User user = new User(
                String.valueOf(dto.getId()),
                dto.getUsername(),
                dto.getFullName() != null && !dto.getFullName().isBlank() ? dto.getFullName() : dto.getUsername(),
                dto.getEmail(),
                dto.getAvatarColor() != null ? dto.getAvatarColor() : "#6366F1",
                dto.getBio() != null ? dto.getBio() : "");
        MockDataFactory.setCurrentUser(user);
    }

    private void navigateToDashboard() {
        try {
            App.setRoot("main_dashboard");
        } catch (IOException e) {
            showError("Failed to open Dashboard: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        lblMessage.setText(msg);
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
    }

    private void hideMessage() {
        lblMessage.setVisible(false);
        lblMessage.setManaged(false);
    }
}
