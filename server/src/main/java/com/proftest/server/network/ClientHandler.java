package com.proftest.server.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.proftest.common.dto.Request;
import com.proftest.common.dto.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * Обработчик клиентского соединения.
 * Реализует Runnable, управляется через Thread (многопоточность).
 */
public class ClientHandler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private final Socket socket;
    private final RequestDispatcher dispatcher;
    private final Gson gson = new GsonBuilder().create();
    private volatile boolean running = true;

    public ClientHandler(Socket socket, RequestDispatcher dispatcher) {
        this.socket = socket;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
        String clientAddr = socket.getRemoteSocketAddress().toString();
        logger.info("Client connected: {}", clientAddr);

        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true)
        ) {
            while (running && !socket.isClosed()) {
                String message = reader.readLine();
                if (message == null) break;

                logger.debug("Received from {}: {}", clientAddr, message.length() > 200 ? message.substring(0, 200) + "..." : message);

                Request request = gson.fromJson(message, Request.class);
                Response response = dispatcher.dispatch(request);
                String responseJson = gson.toJson(response);
                writer.println(responseJson);

                logger.debug("Sent to {}: {}", clientAddr, responseJson.length() > 200 ? responseJson.substring(0, 200) + "..." : responseJson);
            }
        } catch (SocketException e) {
            logger.info("Client disconnected: {}", clientAddr);
        } catch (IOException e) {
            logger.error("IO error with client {}: {}", clientAddr, e.getMessage());
        } finally {
            closeSocket();
            logger.info("Handler finished for: {}", clientAddr);
        }
    }

    public void stop() {
        running = false;
        closeSocket();
    }

    private void closeSocket() {
        try {
            if (!socket.isClosed()) socket.close();
        } catch (IOException e) {
            logger.warn("Error closing socket: {}", e.getMessage());
        }
    }
}
