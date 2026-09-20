package com.dreamstop.server.network;

import com.dreamstop.common.model.RequestType;
import com.dreamstop.common.protocol.Request;
import com.dreamstop.common.protocol.Response;
import com.dreamstop.common.protocol.ServerNotification;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/*****
 * Handles one client connection in its own thread.
 * Responsible for reading requests, dispatching them, and sending responses.
 */

public final class ClientHandler implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());

    private final Socket socket;
    private final RequestDispatcher dispatcher;
    private final SessionManager sessionManager;
    private final Consumer<ClientHandler> onDisconnect;

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private volatile boolean running = true; // flag
    private Integer userId; // only used for logging

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
            // Create output stream first to avoid deadlock
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush(); // use flush to forces header bytes through the socket immediately
            in = new ObjectInputStream(socket.getInputStream());

            while (running) {
                Request request = (Request) in.readObject();

                if (request.getType() == RequestType.LOGOUT) {
                    sessionManager.logout(request.getToken());
                    sendResponse(Response.success("Logged out"));
                    break;
                }

                sendResponse(dispatcher.dispatch(request, this));
            }
        } catch (EOFException | SocketException e) {
            LOGGER.info(() -> "Client disconnected: " + describeClient());
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "Error handling client " + describeClient(), e);
        } finally {
            close(); // socket closed
        }
    }

    // because there might be more than one thread trying to write on the same ObjectOutputStream
    public synchronized void sendResponse(Response response) {
        write(response);
    }

    public synchronized void sendNotification(ServerNotification notification) {
        write(notification);
    }

    private void write(Object payload) {
        if (out == null) return;
        try {
            out.writeObject(payload);
            out.flush();
            out.reset();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to send data to " + describeClient(), e);
        }
    }

    // Returns the userId associated with the request token, or null if invalid
    public Integer resolveUserId(Request request) {
        return sessionManager.resolve(request.getToken());
    }

    // Called after successful login. Returns a new session token
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