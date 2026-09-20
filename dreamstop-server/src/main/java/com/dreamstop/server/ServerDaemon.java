package com.dreamstop.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dreamstop.server.network.ClientHandler;
import com.dreamstop.server.network.RequestDispatcher;
import com.dreamstop.server.network.SessionManager;

public final class ServerDaemon {

    private static final Logger LOGGER = Logger.getLogger(ServerDaemon.class.getName());
    public static final int DEFAULT_PORT = 5005;

    private final RequestDispatcher dispatcher;
    private final SessionManager sessionManager;
    private final Set<ClientHandler> activeClients = ConcurrentHashMap.newKeySet();
    private final List<ServerListener> listeners = new CopyOnWriteArrayList<>();

    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private Thread acceptThread;
    private volatile boolean running;
    private int port = DEFAULT_PORT;

    public ServerDaemon() {
        this.sessionManager = new SessionManager();
        this.dispatcher = ServerRequestHandlers.buildDefault();
    }

    public synchronized void start(int listenPort) throws IOException {
        if (running) {
            String msg = "Server is already running on port " + port;
            LOGGER.info(msg);
            notifyLog(msg);
            return;
        }

        this.port = listenPort;
        this.serverSocket = new ServerSocket();
        this.serverSocket.setReuseAddress(true);
        this.serverSocket.bind(new InetSocketAddress(this.port));

        this.threadPool = Executors.newCachedThreadPool();
        this.running = true;

        com.dreamstop.server.database.DatabaseManager.getInstance().initDatabaseIfAvailable();

        this.acceptThread = new Thread(this::listenForConnections, "server-accept-loop");
        this.acceptThread.setDaemon(true);
        this.acceptThread.start();

        String msg = "DreamsTop Server started successfully on port " + this.port;
        LOGGER.info(msg);
        notifyLog(msg);
        notifyStarted(this.port);
    }

    public synchronized void stop() {
        if (!running) {
            String msg = "Server is already stopped.";
            LOGGER.info(msg);
            notifyLog(msg);
            return;
        }

        String stopMsg = "Stopping DreamsTop Server...";
        LOGGER.info(stopMsg);
        notifyLog(stopMsg);
        this.running = false;

        // 1. Close ServerSocket first to stop accepting new connections
        closeServerSocket();

        // 2. Disconnect all active client connections cleanly
        disconnectAllClients();

        // 3. Clear all active sessions
        sessionManager.clearAll();

        // 4. Shutdown worker thread pool
        shutdownThreadPool();

        // 5. Interrupt accept thread if still active
        if (acceptThread != null) {
            acceptThread.interrupt();
            acceptThread = null;
        }

        String doneMsg = "DreamsTop Server stopped successfully.";
        LOGGER.info(doneMsg);
        notifyLog(doneMsg);
        notifyStopped();
    }

    public boolean isRunning() {
        return running && serverSocket != null && !serverSocket.isClosed();
    }

    public int getPort() {
        return port;
    }

    public int getActiveClientCount() {
        return activeClients.size();
    }

    public Set<ClientHandler> getActiveClients() {
        return Collections.unmodifiableSet(activeClients);
    }

    public RequestDispatcher getDispatcher() {
        return dispatcher;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void addListener(ServerListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(ServerListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    private void listenForConnections() {
        while (running && serverSocket != null && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                String remoteAddr = String.valueOf(clientSocket.getRemoteSocketAddress());
                LOGGER.info("Client connected from: " + remoteAddr);

                ClientHandler clientHandler = new ClientHandler(
                        clientSocket,
                        dispatcher,
                        sessionManager,
                        this::onClientDisconnected
                );

                activeClients.add(clientHandler);
                notifyClientConnected(activeClients.size(), remoteAddr);

                if (threadPool != null && !threadPool.isShutdown()) {
                    threadPool.submit(clientHandler);
                }

            } catch (IOException e) {
                if (running) {
                    LOGGER.log(Level.WARNING, "Error accepting client connection", e);
                    notifyError("Error accepting client connection", e);
                }
                break;
            }
        }
    }

    private void onClientDisconnected(ClientHandler handler) {
        if (activeClients.remove(handler)) {
            String remoteAddr = handler.getRemoteAddress();
            LOGGER.info("Client disconnected: " + remoteAddr);
            notifyClientDisconnected(activeClients.size(), remoteAddr);
        }
    }

    private void disconnectAllClients() {
        if (!activeClients.isEmpty()) {
            LOGGER.info("Disconnecting " + activeClients.size() + " active client(s)...");
            for (ClientHandler handler : activeClients) {
                try {
                    handler.disconnect();
                } catch (Exception e) {
                    LOGGER.log(Level.FINE, "Error disconnecting client", e);
                }
            }
            activeClients.clear();
        }
    }

    private void closeServerSocket() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error closing server socket", e);
        }
    }

    private void shutdownThreadPool() {
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(2, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void notifyStarted(int port) {
        for (ServerListener l : listeners) {
            try {
                l.onServerStarted(port);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error in listener onServerStarted", e);
            }
        }
    }

    private void notifyStopped() {
        for (ServerListener l : listeners) {
            try {
                l.onServerStopped();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error in listener onServerStopped", e);
            }
        }
    }

    private void notifyClientConnected(int count, String addr) {
        for (ServerListener l : listeners) {
            try {
                l.onClientConnected(count, addr);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error in listener onClientConnected", e);
            }
        }
    }

    private void notifyClientDisconnected(int count, String addr) {
        for (ServerListener l : listeners) {
            try {
                l.onClientDisconnected(count, addr);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error in listener onClientDisconnected", e);
            }
        }
    }

    private void notifyError(String msg, Throwable t) {
        for (ServerListener l : listeners) {
            try {
                l.onServerError(msg, t);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error in listener onServerError", e);
            }
        }
    }

    private void notifyLog(String message) {
        for (ServerListener l : listeners) {
            try {
                l.onServerLog(message);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error in listener onServerLog", e);
            }
        }
    }
}
