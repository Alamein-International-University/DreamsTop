package com.dreamstop.network;

import com.dreamstop.common.dto.UserDTO;
import com.dreamstop.common.model.NotificationType;
import com.dreamstop.common.protocol.JsonUtils;
import com.dreamstop.common.protocol.Request;
import com.dreamstop.common.protocol.Response;
import com.dreamstop.common.protocol.ServerNotification;
import com.dreamstop.util.NotificationUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class NetworkClient {

    private static final Logger LOGGER = Logger.getLogger(NetworkClient.class.getName());
    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 5005;

    private static final NetworkClient INSTANCE = new NetworkClient();

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread listenerThread;
    private volatile boolean running;

    private String sessionToken;
    private UserDTO currentUser;

    private final BlockingQueue<Response> responseQueue = new LinkedBlockingQueue<>();
    private final List<Consumer<ServerNotification>> notificationListeners = new CopyOnWriteArrayList<>();
    private final List<Runnable> disconnectListeners = new CopyOnWriteArrayList<>();
    private static volatile String lastDisconnectReason = null;

    public static String getAndClearLastDisconnectReason() {
        String reason = lastDisconnectReason;
        lastDisconnectReason = null;
        return reason;
    }

    public static void setLastDisconnectReason(String reason) {
        lastDisconnectReason = reason;
    }

    public void addDisconnectListener(Runnable listener) {
        if (listener != null) {
            disconnectListeners.add(listener);
        }
    }

    public void removeDisconnectListener(Runnable listener) {
        disconnectListeners.remove(listener);
    }

    private NetworkClient() {
    }

    public static NetworkClient getInstance() {
        return INSTANCE;
    }

    public synchronized void connect() throws IOException {
        connect(DEFAULT_HOST, DEFAULT_PORT);
    }

    public synchronized void connect(String host, int port) throws IOException {
        if (isConnected()) {
            return;
        }

        this.socket = new Socket(host, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        this.running = true;

        this.listenerThread = new Thread(this::listenIncomingMessages, "network-client-listener");
        this.listenerThread.setDaemon(true);
        this.listenerThread.start();

        LOGGER.info("Connected to DreamsTop server at " + host + ":" + port);
    }

    public synchronized void disconnect() {
        this.running = false;
        this.sessionToken = null;
        this.currentUser = null;

        closeResources();

        if (listenerThread != null && listenerThread != Thread.currentThread()) {
            listenerThread.interrupt();
        }

        LOGGER.info("Disconnected from DreamsTop server.");
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public synchronized Response sendRequest(Request request, long timeoutSeconds) throws IOException, InterruptedException {
        if (!isConnected()) {
            throw new IOException("Not connected to server.");
        }

        if (request.getToken() == null && sessionToken != null) {
            request.setToken(sessionToken);
        }

        responseQueue.clear();
        String jsonPayload = JsonUtils.toJson(request);
        out.println(jsonPayload);
        out.flush();

        return responseQueue.poll(timeoutSeconds, TimeUnit.SECONDS);
    }

    public CompletableFuture<Response> sendRequestAsync(Request request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return sendRequest(request, 5);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Async request failed: " + request.getType(), e);
                return Response.error("Connection error: " + e.getMessage());
            }
        });
    }

    public void addNotificationListener(Consumer<ServerNotification> listener) {
        if (listener != null) {
            notificationListeners.add(listener);
        }
    }

    public void removeNotificationListener(Consumer<ServerNotification> listener) {
        notificationListeners.remove(listener);
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public UserDTO getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(UserDTO currentUser) {
        this.currentUser = currentUser;
    }

    private void listenIncomingMessages() {
        try {
            while (running && isConnected()) {
                String line = in.readLine();
                if (line == null) {
                    LOGGER.info("Server closed connection (EOF).");
                    break;
                }
                processIncomingLine(line.trim());
            }
        } catch (IOException e) {
            if (running) {
                LOGGER.info("Connection closed by server.");
            }
        } finally {
            boolean wasLoggedIn = (sessionToken != null || currentUser != null);
            this.running = false;
            this.sessionToken = null;
            this.currentUser = null;
            closeResources();
            if (wasLoggedIn) {
                lastDisconnectReason = "Server connection lost. You have been signed out.";
            }
            notifyServerDisconnected(wasLoggedIn);
        }
    }

    private void notifyServerDisconnected(boolean wasLoggedIn) {
        try {
            Platform.runLater(() -> {
                if (wasLoggedIn) {
                    try {
                        com.dreamstop.App.setRoot("login_view");
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Failed to navigate to login_view on disconnect", e);
                    }
                }
                for (Runnable listener : disconnectListeners) {
                    try {
                        listener.run();
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Error in disconnect listener", e);
                    }
                }
            });
        } catch (IllegalStateException e) {
            // Headless / non-JavaFX environment
            for (Runnable listener : disconnectListeners) {
                try {
                    listener.run();
                } catch (Exception ignored) {}
            }
        }
    }

    private void processIncomingLine(String rawJson) {
        if (rawJson == null || rawJson.isEmpty()) {
            return;
        }

        try {
            JsonObject jsonObject = JsonParser.parseString(rawJson).getAsJsonObject();

            if (jsonObject.has("status")) {
                Response response = JsonUtils.fromJson(rawJson, Response.class);
                responseQueue.offer(response);
            } else if (jsonObject.has("title") && jsonObject.has("type")) {
                ServerNotification notification = JsonUtils.fromJson(rawJson, ServerNotification.class);
                dispatchNotification(notification);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse incoming message: " + rawJson, e);
        }
    }

    private void dispatchNotification(ServerNotification notification) {
        try {
            Platform.runLater(() -> {
                showToastNotification(notification);
                for (Consumer<ServerNotification> listener : notificationListeners) {
                    listener.accept(notification);
                }
            });
        } catch (IllegalStateException e) {
            // JavaFX runtime not initialized (e.g. inside headless unit test)
            for (Consumer<ServerNotification> listener : notificationListeners) {
                listener.accept(notification);
            }
        }
    }

    private void showToastNotification(ServerNotification notification) {
        if (notification == null) {
            return;
        }

        NotificationType type = notification.getType();
        String text = notification.getTitle() + "\n" + notification.getMessage();

        if (type == NotificationType.ITEM_COMPLETED_BUYER || type == NotificationType.ITEM_COMPLETED_RECEIVER) {
            NotificationUtil.showSuccess("🎁 " + text);
        } else if (type == NotificationType.CONTRIBUTION_RECEIVED) {
            NotificationUtil.showInfo("💰 " + text);
        } else if (type == NotificationType.FRIEND_REQUEST || type == NotificationType.FRIEND_REQUEST_ACCEPTED) {
            NotificationUtil.showInfo("👥 " + text);
        } else if (type == NotificationType.FRIEND_REMOVED) {
            NotificationUtil.showWarning("👥 " + text);
        } else {
            NotificationUtil.showInfo(text);
        }
    }

    private void closeResources() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException ignored) {}
        try {
            if (out != null) {
                out.close();
            }
        } catch (Exception ignored) {}
    }
}
