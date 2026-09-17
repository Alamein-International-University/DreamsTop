package com.dreamstop.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ServerDaemon {

    private static final Logger LOGGER = Logger.getLogger(ServerDaemon.class.getName());
    private static final int DEFAULT_PORT = 5005;

    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private Thread acceptThread;
    private volatile boolean running;
    private int port = DEFAULT_PORT;

    public synchronized void start(int listenPort) throws IOException {
        if (running) {
            LOGGER.info("Server is already running on port " + port);
            return;
        }

        this.port = listenPort;
        this.serverSocket = new ServerSocket(this.port);
        this.threadPool = Executors.newCachedThreadPool();
        this.running = true;

        this.acceptThread = new Thread(this::listenForConnections, "server-accept-loop");
        this.acceptThread.setDaemon(true);
        this.acceptThread.start();

        LOGGER.info("DreamsTop Server started successfully on port " + this.port);
    }

    public synchronized void stop() {
        if (!running) {
            LOGGER.info("Server is already stopped.");
            return;
        }

        LOGGER.info("Stopping DreamsTop Server...");
        this.running = false;

        closeServerSocket();
        shutdownThreadPool();

        if (acceptThread != null) {
            acceptThread.interrupt();
        }

        LOGGER.info("DreamsTop Server stopped successfully.");
    }

    public boolean isRunning() {
        return running && serverSocket != null && !serverSocket.isClosed();
    }

    public int getPort() {
        return port;
    }

    private void listenForConnections() {
        while (running && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                LOGGER.info("Client connected from: " + clientSocket.getRemoteSocketAddress());
            } catch (IOException e) {
                if (running) {
                    LOGGER.log(Level.WARNING, "Error accepting client connection", e);
                }
                break;
            }
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
                if (!threadPool.awaitTermination(3, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
