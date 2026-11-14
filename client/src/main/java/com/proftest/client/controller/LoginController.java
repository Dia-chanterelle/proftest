package com.proftest.client.controller;

import com.google.gson.reflect.TypeToken;
import com.proftest.client.util.AlertUtil;
import com.proftest.common.dto.AuthResultDto;
import com.proftest.common.dto.LoginDto;
import com.proftest.common.dto.Response;
import com.proftest.common.protocol.RequestType;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController extends BaseController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label statusLabel;
    @FXML private CheckBox rememberCheck;

    @FXML
    private void handleLogin(ActionEvent event) {
        String login = loginField.getText().trim();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Введите логин и пароль");
            return;
        }

        loginButton.setDisable(true);
        statusLabel.setText("Подключение...");

        new Thread(() -> {
            try {
                LoginDto dto = new LoginDto(login, password);
                Response response = connection.send(RequestType.LOGIN, dto);

                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    if (response.isSuccess()) {
                        AuthResultDto auth = connection.getGson().fromJson(response.getPayload(), AuthResultDto.class);
                        currentUser = auth;
                        connection.setAuthToken(auth.getToken());
                        logger.info("Logged in as: {}", auth.getUser().getLogin());
                        Stage stage = getStage(event);
                        stage.setResizable(true);
                        switchScene(stage, "/fxml/Main.fxml", "Главное меню", 1100, 720);
                    } else {
                        statusLabel.setText(response.getMessage());
                        AlertUtil.showError("Ошибка входа", response.getMessage());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    statusLabel.setText("Ошибка: " + e.getMessage());
                    AlertUtil.showError("Ошибка подключения", e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        switchScene(getStage(event), "/fxml/Register.fxml", "Регистрация", 520, 580);
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }
}
