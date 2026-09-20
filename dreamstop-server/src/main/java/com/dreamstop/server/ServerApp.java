package com.dreamstop.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ServerApp {

    private static final Logger LOGGER = Logger.getLogger(ServerApp.class.getName());
    private static final int DEFAULT_PORT = ServerDaemon.DEFAULT_PORT;

    public static void main(String[] args) {
        ServerDaemon daemon = new ServerDaemon();
        
        // Attach listener for clean console reporting
        daemon.addListener(new ServerListener() {
            @Override
            public void onServerStarted(int port) {
                System.out.println(">> [EVENT] Server is now RUNNING on port " + port);
            }

            @Override
            public void onServerStopped() {
                System.out.println(">> [EVENT] Server has STOPPED. All client connections closed.");
            }

            @Override
            public void onClientConnected(int activeClientsCount, String remoteAddress) {
                System.out.println(">> [EVENT] Client connected (" + remoteAddress + ") | Active clients: " + activeClientsCount);
            }

            @Override
            public void onClientDisconnected(int activeClientsCount, String remoteAddress) {
                System.out.println(">> [EVENT] Client disconnected (" + remoteAddress + ") | Active clients: " + activeClientsCount);
            }

            @Override
            public void onServerError(String message, Throwable throwable) {
                System.err.println(">> [ERROR] " + message + (throwable != null ? ": " + throwable.getMessage() : ""));
            }
        });

        int port = extractPort(args);

        try {
            daemon.start(port);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to start server on port " + port, e);
            System.err.println("Failed to start server on port " + port + ": " + e.getMessage());
        }

        printBanner(daemon.getPort());
        runCommandLoop(daemon);
    }

    private static int extractPort(String[] args) {
        if (args != null && args.length > 0) {
            try {
                return Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }
        return DEFAULT_PORT;
    }

    private static void printBanner(int port) {
        System.out.println("=================================================");
        System.out.println("       DreamsTop Server Controller (i-Wish)      ");
        System.out.println("=================================================");
        System.out.println(" Default port: " + port);
        System.out.println(" Available commands:");
        System.out.println("   start [port]  - Start the server on specified port");
        System.out.println("   stop          - Stop the server & disconnect clients");
        System.out.println("   status        - Show current server status");
        System.out.println("   clients       - Show connected clients count");
        System.out.println("   help          - Show this command guide");
        System.out.println("   exit          - Stop server and exit program");
        System.out.println("=================================================");
    }

    private static void runCommandLoop(ServerDaemon daemon) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String input = line.trim();
                if (input.isEmpty()) continue;

                String[] parts = input.split("\\s+");
                String command = parts[0].toLowerCase();

                if ("exit".equals(command)) {
                    if (daemon.isRunning()) {
                        daemon.stop();
                    }
                    System.out.println("Exiting DreamsTop Server Controller. Goodbye!");
                    break;
                }

                executeCommand(daemon, command, parts);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error reading console input", e);
        }
    }

    private static void executeCommand(ServerDaemon daemon, String command, String[] parts) {
        switch (command) {
            case "stop":
                if (!daemon.isRunning()) {
                    System.out.println("Server is not running.");
                } else {
                    daemon.stop();
                }
                break;

            case "start":
                if (daemon.isRunning()) {
                    System.out.println("Server is already running on port " + daemon.getPort());
                } else {
                    int targetPort = daemon.getPort();
                    if (parts.length > 1) {
                        try {
                            targetPort = Integer.parseInt(parts[1]);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid port number: " + parts[1] + ". Using " + targetPort);
                        }
                    }
                    try {
                        daemon.start(targetPort);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Error starting server on port " + targetPort, e);
                        System.err.println("Error starting server: " + e.getMessage());
                    }
                }
                break;

            case "status":
                if (daemon.isRunning()) {
                    System.out.println("Server Status: RUNNING on port " + daemon.getPort() +
                            " | Connected clients: " + daemon.getActiveClientCount());
                } else {
                    System.out.println("Server Status: STOPPED");
                }
                break;

            case "clients":
                System.out.println("Connected clients count: " + daemon.getActiveClientCount());
                break;

            case "help":
                printBanner(daemon.getPort());
                break;

            default:
                System.out.println("Unknown command: '" + command + "'. Type 'help' for available commands.");
                break;
        }
    }
}
