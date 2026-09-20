package com.dreamstop.server.network;

import com.dreamstop.common.model.RequestType;
import com.dreamstop.common.protocol.JsonUtils;
import com.dreamstop.common.protocol.Request;
import com.dreamstop.common.protocol.Response;
import com.dreamstop.server.handler.AuthRequestHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class ClientHandlerJsonTest {

    private ServerSocket serverSocket;
    private ExecutorService executor;
    private SessionManager sessionManager;
    private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws IOException {
        serverSocket = new ServerSocket(0); // bind to any free port
        executor = Executors.newCachedThreadPool();
        sessionManager = new SessionManager();
        dispatcher = new RequestDispatcher();

        AuthRequestHandler authHandler = new AuthRequestHandler();
        dispatcher.register(RequestType.LOGIN, authHandler::handleLogin);
        dispatcher.register(RequestType.REGISTER, authHandler::handleRegister);
        dispatcher.register(RequestType.LOGOUT, authHandler::handleLogout);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("Verify ClientHandler processes JSON request and returns valid JSON response")
    void testJsonRequestResponseFlow() throws Exception {
        int port = serverSocket.getLocalPort();

        // 1. Start server accept in background
        executor.submit(() -> {
            try {
                Socket serverSideSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(serverSideSocket, dispatcher, sessionManager);
                handler.run();
            } catch (IOException ignored) {
            }
        });

        // 2. Connect client
        try (Socket clientSocket = new Socket("127.0.0.1", port);
             BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            // 3. Send JSON LOGIN request
            String loginPayload = "{\"usernameOrEmail\":\"yousef\",\"password\":\"secret\"}";
            Request loginRequest = new Request(RequestType.LOGIN, null, loginPayload);
            String jsonToSend = JsonUtils.toJson(loginRequest);

            writer.println(jsonToSend);

            // 4. Read JSON response
            String receivedLine = reader.readLine();
            assertNotNull(receivedLine, "Server should respond with a non-null JSON line");

            Response response = JsonUtils.fromJson(receivedLine, Response.class);
            assertNotNull(response, "Response should parse cleanly from JSON");
            assertTrue(response.isSuccess(), "Login response should be successful");
            assertEquals("Login successful", response.getMessage());

            // 5. Send JSON LOGOUT request
            Request logoutRequest = new Request(RequestType.LOGOUT, null, null);
            writer.println(JsonUtils.toJson(logoutRequest));

            String logoutResponseLine = reader.readLine();
            assertNotNull(logoutResponseLine, "Server should respond to logout");

            Response logoutResponse = JsonUtils.fromJson(logoutResponseLine, Response.class);
            assertNotNull(logoutResponse);
            assertTrue(logoutResponse.isSuccess());
        }
    }
}
