package com.dreamstop.server.network;

import com.dreamstop.common.protocol.Request;
import com.dreamstop.common.protocol.Response;

/****
 * Functional interface for handling a single request type.
 */
@FunctionalInterface
public interface RequestHandler {
    Response handle(Request request, ClientHandler client);
}