package com.dreamstop.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ServerApp {

    private static final Logger LOGGER = Logger.getLogger(ServerApp.class.getName());
    private static final int DEFAULT_PORT = 5005;

    public static void main(String[] args) {
        ServerDaemon daemon = new ServerDaemon();
        int port = extractPort(args);

        try {
            daemon.start(port);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to start server on port " + port, e);
            return;
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
        System.out.println(" Server running on port: " + port);
        System.out.println(" Available commands: start, stop, status, exit");
        System.out.println("=================================================");
    }

    private static void runCommandLoop(ServerDaemon daemon) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String command = line.trim().toLowerCase();
                if ("exit".equals(command)) {
                    daemon.stop();
                    break;
                }
                executeCommand(daemon, command);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error reading console input", e);
        }
    }

    private static void executeCommand(ServerDaemon daemon, String command) {
        if ("stop".equals(command)) {
            daemon.stop();
        } else if ("start".equals(command)) {
            try {
                daemon.start(daemon.getPort());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error starting server", e);
            }
        } else if ("status".equals(command)) {
            String status = daemon.isRunning() ? "RUNNING on port " + daemon.getPort() : "STOPPED";
            System.out.println("Server Status: " + status);
        } else if (!command.isEmpty()) {
            System.out.println("Unknown command. Type: start, stop, status, or exit");
        }
    }
}
