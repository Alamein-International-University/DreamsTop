package com.dreamstop.server.gui;

import com.dreamstop.server.ServerDaemon;
import com.dreamstop.server.ServerListener;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ServerGuiController implements ServerListener {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    private Label lblBadge;

    @FXML
    private TextField txtPort;

    @FXML
    private Button btnToggle;

    @FXML
    private Label lblCardStatus;

    @FXML
    private Label lblCardStatusSub;

    @FXML
    private Label lblCardPort;

    @FXML
    private Label lblCardClients;

    @FXML
    private TextArea txtLogs;

    private ServerDaemon daemon;

    public void setDaemon(ServerDaemon daemon) {
        this.daemon = daemon;
        if (daemon != null) {
            daemon.addListener(this);
            syncInitialState();
        }
    }

    public ServerDaemon getDaemon() {
        return daemon;
    }

    @FXML
    public void initialize() {
        log("Server GUI initialized. Ready to start.");
    }

    private void syncInitialState() {
        if (daemon == null) return;

        Platform.runLater(() -> {
            boolean running = daemon.isRunning();
            int port = daemon.getPort();
            txtPort.setText(String.valueOf(port));
            lblCardPort.setText(String.valueOf(port));

            if (running) {
                onServerStarted(port);
                lblCardClients.setText(String.valueOf(daemon.getActiveClientCount()));
            } else {
                onServerStopped();
            }
        });
    }

    @FXML
    private void handleToggleServer(ActionEvent event) {
        if (daemon == null) return;

        if (daemon.isRunning()) {
            // Stop Server
            btnToggle.setDisable(true);
            new Thread(() -> {
                try {
                    daemon.stop();
                } finally {
                    Platform.runLater(() -> btnToggle.setDisable(false));
                }
            }, "server-stop-thread").start();

        } else {
            // Start Server
            int port;
            try {
                port = Integer.parseInt(txtPort.getText().trim());
                if (port < 1 || port > 65535) {
                    showError("Invalid Port", "Port number must be between 1 and 65535.");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Invalid Port", "Please enter a valid integer port number.");
                return;
            }

            btnToggle.setDisable(true);
            final int selectedPort = port;
            new Thread(() -> {
                try {
                    daemon.start(selectedPort);
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        showError("Server Start Failed", "Could not start server on port " + selectedPort + ":\n" + e.getMessage());
                        onServerStopped();
                    });
                } finally {
                    Platform.runLater(() -> btnToggle.setDisable(false));
                }
            }, "server-start-thread").start();
        }
    }

    @FXML
    private void handleClearLogs(ActionEvent event) {
        txtLogs.clear();
    }

    @Override
    public void onServerStarted(int port) {
        Platform.runLater(() -> {
            lblBadge.setText("RUNNING");
            lblBadge.getStyleClass().setAll("badge-running");

            btnToggle.setText("⏹ Stop Server");
            btnToggle.getStyleClass().setAll("btn-stop");

            lblCardStatus.setText("ONLINE");
            lblCardStatusSub.setText("Listening on port " + port);
            lblCardPort.setText(String.valueOf(port));

            txtPort.setDisable(true);
            log("Server started successfully on port " + port);
        });
    }

    @Override
    public void onServerStopped() {
        Platform.runLater(() -> {
            lblBadge.setText("STOPPED");
            lblBadge.getStyleClass().setAll("badge-stopped");

            btnToggle.setText("▶ Start Server");
            btnToggle.getStyleClass().setAll("btn-start");

            lblCardStatus.setText("OFFLINE");
            lblCardStatusSub.setText("Socket is closed");
            lblCardClients.setText("0");

            txtPort.setDisable(false);
            log("Server stopped. All client connections terminated.");
        });
    }

    @Override
    public void onClientConnected(int activeClientsCount, String remoteAddress) {
        Platform.runLater(() -> {
            lblCardClients.setText(String.valueOf(activeClientsCount));
            log("Client connected from " + remoteAddress + " (Total active: " + activeClientsCount + ")");
        });
    }

    @Override
    public void onClientDisconnected(int activeClientsCount, String remoteAddress) {
        Platform.runLater(() -> {
            lblCardClients.setText(String.valueOf(activeClientsCount));
            log("Client disconnected: " + remoteAddress + " (Total active: " + activeClientsCount + ")");
        });
    }

    @Override
    public void onServerError(String message, Throwable throwable) {
        Platform.runLater(() -> log("[ERROR] " + message + (throwable != null ? ": " + throwable.getMessage() : "")));
    }

    @Override
    public void onServerLog(String message) {
        Platform.runLater(() -> log(message));
    }

    private void log(String message) {
        String timestamp = LocalTime.now().format(TIME_FORMATTER);
        txtLogs.appendText("[" + timestamp + "] " + message + "\n");
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
