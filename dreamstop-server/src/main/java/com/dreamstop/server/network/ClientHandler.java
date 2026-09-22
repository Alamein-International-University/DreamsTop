package com.dreamstop.server.network;

import com.dreamstop.common.model.RequestType;
import com.dreamstop.common.protocol.JsonUtils;
import com.dreamstop.common.protocol.Request;
import com.dreamstop.common.protocol.Response;
import com.dreamstop.common.protocol.ServerNotification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles communication with a single connected client over a TCP socket.
 * Reads line-delimited JSON requests, dispatches them, and returns JSON responses.
 */
public final class ClientHandler implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());

    private final Socket socket;
    private final RequestDispatcher dispatcher;
    private final SessionManager sessionManager;
    private final Consumer<ClientHandler> onDisconnect;

    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean running = true;
    private Integer userId;

    public ClientHandler(Socket socket, RequestDispatcher dispatcher, SessionManager sessionManager) {
        this(socket, dispatcher, sessionManager, null);
    }

    public ClientHandler(Socket socket, RequestDispatcher dispatcher, SessionManager sessionManager, Consumer<ClientHandler> onDisconnect) {
        this.socket = socket;
        this.dispatcher = dispatcher;
        this.sessionManager = sessionManager;
        this.onDisconnect = onDisconnect;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            String line;
            while (running && (line = in.readLine()) != null) {
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty()) {
                    continue;
                }

                Request request = JsonUtils.fromJson(trimmedLine, Request.class);
                if (request == null) {
                    sendResponse(Response.badRequest("Malformed JSON request"));
                    continue;
                }

                Response response = dispatcher.dispatch(request, this);
                sendResponse(response);

                // If user logged out, cleanly finish this connection loop
                if (request.getType() == RequestType.LOGOUT) {
                    break;
                }
            }
        } catch (SocketException e) {
            LOGGER.info(() -> "Client disconnected: " + describeClient());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error handling client " + describeClient(), e);
        } finally {
            close();
        }
    }

    public synchronized void sendResponse(Response response) {
        write(response);
    }

    public synchronized void sendNotification(ServerNotification notification) {
        write(notification);
    }

    private void write(Object payload) {
        if (out == null || socket.isClosed()) {
            return;
        }
        try {
            String json = JsonUtils.toJson(payload);
            out.println(json);
            out.flush();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to send data to " + describeClient(), e);
        }
    }

    public Integer resolveUserId(Request request) {
        if (request == null) return null;
        return sessionManager.resolve(request.getToken());
    }

    public String bindUser(int userId) {
        this.userId = userId;
        return sessionManager.login(userId, this);
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void disconnect() {
        running = false;
        close();
    }

    private void close() {
        running = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
        try {
            if (in != null) in.close();
        } catch (IOException ignored) {}
        try {
            if (out != null) out.close();
        } catch (Exception ignored) {}

        if (sessionManager != null) {
            sessionManager.unregisterHandler(this);
        }

        if (onDisconnect != null) {
            onDisconnect.accept(this);
        }
    }

    public String getRemoteAddress() {
        if (socket != null && socket.getRemoteSocketAddress() != null) {
            return socket.getRemoteSocketAddress().toString();
        }
        return describeClient();
    }

    private String describeClient() {
        return userId != null ? "user#" + userId : (socket != null ? String.valueOf(socket.getRemoteSocketAddress()) : "unknown");
    }
}