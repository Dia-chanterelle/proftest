package com.proftest.client.controller;

import com.proftest.client.util.AlertUtil;
import com.proftest.common.dto.RegisterDto;
import com.proftest.common.dto.Response;
import com.proftest.common.protocol.RequestType;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController extends BaseController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField departmentField;
    @FXML private TextField positionField;
    @FXML private Button registerButton;
    @FXML private Label statusLabel;

    @FXML
    private void handleRegister(ActionEvent event) {
        String login = loginField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();

        if (login.isEmpty() || password.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
            statusLabel.setText("Заполните обязательные поля");
            return;
        }
        if (!password.equals(confirm)) {
            statusLabel.setText("Пароли не совпадают");
            return;
        }
        if (password.length() < 6) {
            statusLabel.setText("Пароль должен содержать минимум 6 символов");
            return;
        }

        registerButton.setDisable(true);
        new Thread(() -> {
            try {
                RegisterDto dto = new RegisterDto();
                dto.setLogin(login);
                dto.setPassword(password);
                dto.setFullName(fullName);
                dto.setEmail(email);
                dto.setPhone(phoneField.getText().trim());
                dto.setDepartment(departmentField.getText().trim());
                dto.setPosition(positionField.getText().trim());
                dto.setRole("CANDIDATE");

                Response response = connection.send(RequestType.REGISTER, dto);
                Platform.runLater(() -> {
                    registerButton.setDisable(false);
                    if (response.isSuccess()) {
                        AlertUtil.showInfo("Успех", "Регистрация выполнена!\nВойдите под своими данными.");
                        switchScene(getStage(event), "/fxml/Login.fxml", "Авторизация", 480, 420);
                    } else {
                        statusLabel.setText(response.getMessage());
                        AlertUtil.showError("Ошибка", response.getMessage());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    registerButton.setDisable(false);
                    statusLabel.setText(e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        switchScene(getStage(event), "/fxml/Login.fxml", "Авторизация", 480, 420);
    }
}
