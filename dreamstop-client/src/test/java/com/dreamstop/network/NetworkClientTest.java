package com.dreamstop.network;

import com.dreamstop.common.model.NotificationType;
import com.dreamstop.common.model.RequestType;
import com.dreamstop.common.protocol.JsonUtils;
import com.dreamstop.common.protocol.Request;
import com.dreamstop.common.protocol.Response;
import com.dreamstop.common.protocol.ServerNotification;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class NetworkClientTest {

    private ServerSocket serverSocket;
    private ExecutorService serverExecutor;
    private NetworkClient client;

    @BeforeEach
    void setUp() throws IOException {
        serverSocket = new ServerSocket(0); // bind to free port
        serverExecutor = Executors.newCachedThreadPool();
        client = NetworkClient.getInstance();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (client.isConnected()) {
            client.disconnect();
        }
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        if (serverExecutor != null) {
            serverExecutor.shutdownNow();
        }
    }

    @Test
    @DisplayName("Verify NetworkClient connects, sends request, and parses JSON response")
    void testSendRequest() throws Exception {
        int port = serverSocket.getLocalPort();

        serverExecutor.submit(() -> {
            try (Socket serverSideSocket = serverSocket.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(serverSideSocket.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter out = new PrintWriter(new OutputStreamWriter(serverSideSocket.getOutputStream(), StandardCharsets.UTF_8), true)) {

                String requestLine = in.readLine();
                if (requestLine != null) {
                    Request req = JsonUtils.fromJson(requestLine, Request.class);
                    if (req.getType() == RequestType.PING) {
                        Response res = Response.success("Pong from mock server");
                        out.println(JsonUtils.toJson(res));
                    }
                }
            } catch (IOException ignored) {
            }
        });

        client.connect("127.0.0.1", port);
        assertTrue(client.isConnected(), "Client should be connected");

        Request pingRequest = new Request(RequestType.PING, null, null);
        Response response = client.sendRequest(pingRequest, 3);

        assertNotNull(response, "Response should not be null");
        assertTrue(response.isSuccess(), "Response should be success");
        assertEquals("Pong from mock server", response.getMessage());
    }

    @Test
    @DisplayName("Verify NetworkClient receives asynchronous real-time server notification")
    void testReceiveServerNotification() throws Exception {
        int port = serverSocket.getLocalPort();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ServerNotification> receivedNotification = new AtomicReference<>();

        client.addNotificationListener(notification -> {
            receivedNotification.set(notification);
            latch.countDown();
        });

        serverExecutor.submit(() -> {
            try (Socket serverSideSocket = serverSocket.accept();
                 PrintWriter out = new PrintWriter(new OutputStreamWriter(serverSideSocket.getOutputStream(), StandardCharsets.UTF_8), true)) {

                // Wait briefly for client listener thread to start, then push notification
                Thread.sleep(150);
                ServerNotification notif = new ServerNotification(
                        NotificationType.CONTRIBUTION_RECEIVED,
                        "Gift Contribution",
                        "Kady contributed 500 EGP",
                        10
                );
                out.println(JsonUtils.toJson(notif));
            } catch (Exception ignored) {
            }
        });

        client.connect("127.0.0.1", port);
        boolean notified = latch.await(3, TimeUnit.SECONDS);

        assertTrue(notified, "Client should receive notification within 3 seconds");
        assertNotNull(receivedNotification.get());
        assertEquals("Gift Contribution", receivedNotification.get().getTitle());
        assertEquals("Kady contributed 500 EGP", receivedNotification.get().getMessage());
    }

    @Test
    @DisplayName("Verify sendRequestAsync returns CompletableFuture with valid response")
    void testSendRequestAsync() throws Exception {
        int port = serverSocket.getLocalPort();

        serverExecutor.submit(() -> {
            try (Socket serverSideSocket = serverSocket.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(serverSideSocket.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter out = new PrintWriter(new OutputStreamWriter(serverSideSocket.getOutputStream(), StandardCharsets.UTF_8), true)) {

                String line = in.readLine();
                if (line != null) {
                    out.println(JsonUtils.toJson(Response.success("Async OK")));
                }
            } catch (IOException ignored) {
            }
        });

        client.connect("127.0.0.1", port);
        CompletableFuture<Response> future = client.sendRequestAsync(new Request(RequestType.PING, null, null));
        Response response = future.get(3, TimeUnit.SECONDS);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Async OK", response.getMessage());
    }
}
