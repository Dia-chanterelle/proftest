package com.proftest.server;

import com.proftest.server.config.ServerConfig;
import com.proftest.server.network.AttemptExpiryScheduler;
import com.proftest.server.network.ClientHandler;
import com.proftest.server.network.RequestDispatcher;
import com.proftest.server.network.SessionManager;
import com.proftest.server.service.AuthService;
import com.proftest.server.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private final ServerConfig config;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private volatile boolean running = false;
    private final List<ClientHandler> activeHandlers = Collections.synchronizedList(new ArrayList<>());
    private final RequestDispatcher dispatcher = new RequestDispatcher();

    public Server(ServerConfig config) {
        this.config = config;
    }

    public void start() {
        try {
            logger.info("Initializing Hibernate...");
            HibernateUtil.getInstance();

            logger.info("Initializing default data...");
            new AuthService().initDefaultAdmin();

            threadPool = Executors.newFixedThreadPool(config.getMaxThreads());
            serverSocket = new ServerSocket(config.getPort());
            running = true;

            logger.info("Server started on port {}", config.getPort());
            logger.info("Max threads: {}", config.getMaxThreads());
            AttemptExpiryScheduler.getInstance().start();
            logger.info("Waiting for connections...");

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(clientSocket, dispatcher);
                    activeHandlers.add(handler);
                    threadPool.execute(() -> {
                        try {
                            handler.run();
                        } finally {
                            activeHandlers.remove(handler);
                        }
                    });
                } catch (IOException e) {
                    if (running) logger.error("Error accepting connection: {}", e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("Server error: {}", e.getMessage(), e);
        }
    }

    public void stop() {
        running = false;
        logger.info("Stopping server...");
        try {
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException e) {
            logger.warn("Error closing server socket: {}", e.getMessage());
        }
        activeHandlers.forEach(ClientHandler::stop);
        if (threadPool != null) {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) threadPool.shutdownNow();
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        AttemptExpiryScheduler.getInstance().stop();
        HibernateUtil.getInstance().shutdown();
        logger.info("Server stopped. Active sessions: {}", SessionManager.getInstance().getActiveSessionCount());
    }

    public int getActiveClients() {
        return activeHandlers.size();
    }

    public static void main(String[] args) {
        ServerConfig config = ServerConfig.getInstance();
        config.loadFromClasspath();
        config.applyArgs(args);
        logger.info("Starting server with config: {}", config);

        Server server = new Server(config);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        server.start();
    }
}
