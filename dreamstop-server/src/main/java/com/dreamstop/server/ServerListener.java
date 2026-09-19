package com.dreamstop.server;

/**
 * Listener interface for server lifecycle and connection events.
 */
public interface ServerListener {

    default void onServerStarted(int port) {}

    default void onServerStopped() {}

    default void onClientConnected(int activeClientsCount, String remoteAddress) {}

    default void onClientDisconnected(int activeClientsCount, String remoteAddress) {}

    default void onServerError(String message, Throwable throwable) {}

    default void onServerLog(String message) {}
}
