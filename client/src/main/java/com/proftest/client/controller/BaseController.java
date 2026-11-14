package com.proftest.client.controller;

import com.proftest.client.network.ServerConnection;
import com.proftest.client.util.AlertUtil;
import com.proftest.common.dto.AuthResultDto;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Базовый абстрактный контроллер — общий функционал для всех окон.
 * Расширяет базовые классы через JavaFX иерархию контроллеров.
 */
public abstract class BaseController {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final ServerConnection connection = ServerConnection.getInstance();
    protected static AuthResultDto currentUser;

    protected Stage getStage(javafx.event.ActionEvent event) {
        return (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
    }

    protected void switchScene(Stage stage, String fxmlPath, String title) {
        switchScene(stage, fxmlPath, title, 900, 650);
    }

    protected void switchScene(Stage stage, String fxmlPath, String title, double width, double height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            stage.setTitle(title + " — ProfTest");
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            logger.error("Failed to load FXML: {}", fxmlPath, e);
            AlertUtil.showError("Ошибка", "Не удалось открыть окно: " + e.getMessage());
        }
    }

    protected <T extends BaseController> T openWindow(String fxmlPath, String title) {
        return openWindow(fxmlPath, title, 900, 650);
    }

    protected <T extends BaseController> T openWindow(String fxmlPath, String title, double width, double height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, width, height);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            Stage stage = new Stage();
            stage.setTitle(title + " — ProfTest");
            stage.setScene(scene);
            stage.show();
            return loader.getController();
        } catch (IOException e) {
            logger.error("Failed to open window: {}", fxmlPath, e);
            AlertUtil.showError("Ошибка", "Не удалось открыть окно");
            return null;
        }
    }

    protected boolean isAdmin() {
        return currentUser != null && currentUser.getUser().hasRole("ADMIN");
    }

    protected boolean isHrManager() {
        return currentUser != null && currentUser.getUser().hasRole("HR_MANAGER");
    }

    protected boolean isCandidate() {
        return currentUser != null && currentUser.getUser().hasRole("CANDIDATE");
    }

    protected boolean hasManageAccess() {
        return isAdmin() || isHrManager();
    }
}
