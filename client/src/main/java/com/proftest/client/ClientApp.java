package com.proftest.client;

import com.proftest.client.network.ServerConnection;
import com.proftest.client.util.AlertUtil;
import com.proftest.client.util.ClientConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(ClientApp.class);

    @Override
    public void start(Stage primaryStage) {
        ClientConfig config = ClientConfig.getInstance();
        try {
            ServerConnection.getInstance().connect(config.getServerHost(), config.getServerPort());
            logger.info("Connected to server {}:{}", config.getServerHost(), config.getServerPort());
        } catch (Exception e) {
            logger.error("Could not connect to server: {}", e.getMessage());
            AlertUtil.showError("Ошибка подключения",
                    "Не удалось подключиться к серверу " + config.getServerHost() + ":" + config.getServerPort() +
                    "\n\nУбедитесь, что сервер запущен.");
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 480, 485);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            primaryStage.setTitle("ProfTest — Авторизация");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            AlertUtil.showError("Ошибка запуска", e.getMessage());
        }
    }

    @Override
    public void stop() {
        ServerConnection.getInstance().disconnect();
        logger.info("Application stopped");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
