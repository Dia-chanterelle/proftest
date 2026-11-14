package com.proftest.client.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.proftest.common.dto.Request;
import com.proftest.common.dto.Response;
import com.proftest.common.protocol.RequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

/**
 * Паттерн Singleton — единственное TCP-подключение клиента к серверу.
 * Использует BufferedReader/PrintWriter для обмена JSON-сообщениями.
 */
public class ServerConnection {

    private static final Logger logger = LoggerFactory.getLogger(ServerConnection.class);
    private static volatile ServerConnection instance;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private final Gson gson = new GsonBuilder().create();
    private String authToken;

    private ServerConnection() {}

    public static ServerConnection getInstance() {
        if (instance == null) {
            synchronized (ServerConnection.class) {
                if (instance == null) {
                    instance = new ServerConnection();
                }
            }
        }
        return instance;
    }

    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        logger.info("Connected to server {}:{}", host, port);
    }

    public void disconnect() {
        authToken = null;
        try {
            if (socket != null && !socket.isClosed()) socket.close();
            logger.info("Disconnected from server");
        } catch (IOException e) {
            logger.warn("Error disconnecting: {}", e.getMessage());
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public synchronized Response send(RequestType type, Object payload) {
        return send(type, payload != null ? gson.toJson(payload) : null);
    }

    public synchronized Response send(RequestType type, String payloadJson) {
        try {
            Request request = new Request(type, authToken, payloadJson);
            String json = gson.toJson(request);
            writer.println(json);
            String responseJson = reader.readLine();
            if (responseJson == null) throw new IOException("Server closed connection");
            return gson.fromJson(responseJson, Response.class);
        } catch (IOException e) {
            logger.error("Communication error: {}", e.getMessage());
            throw new RuntimeException("Ошибка связи с сервером: " + e.getMessage(), e);
        }
    }

    public void setAuthToken(String token) {
        this.authToken = token;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Gson getGson() {
        return gson;
    }
}
