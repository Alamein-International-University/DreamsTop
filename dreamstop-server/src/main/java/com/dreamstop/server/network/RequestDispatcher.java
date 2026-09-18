package com.dreamstop.server.network;

import com.dreamstop.common.model.RequestType;
import com.dreamstop.common.protocol.Request;
import com.dreamstop.common.protocol.Response;

import java.util.EnumMap;
import java.util.Map;

/****
 * Routes incoming requests to the correct handler based on RequestType.
 */
public final class RequestDispatcher {

    private final Map<RequestType, RequestHandler> handlers = new EnumMap<>(RequestType.class);

    public void register(RequestType type, RequestHandler handler) {
        handlers.put(type, handler);
    }

    public Response dispatch(Request request, ClientHandler client) {
        if (request == null || request.getType() == null) {
            return Response.badRequest("Malformed request");
        }

        RequestHandler handler = handlers.get(request.getType());
        if (handler == null) {
            return Response.badRequest("Unsupported request: " + request.getType());
        }

        try {
            return handler.handle(request, client);
        } catch (Exception e) {
            return Response.error("Server error while processing " + request.getType() + ": " + e.getMessage());
        }
    }
}