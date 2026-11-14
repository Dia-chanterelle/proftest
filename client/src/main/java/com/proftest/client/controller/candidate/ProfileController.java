package com.proftest.client.controller.candidate;

import com.proftest.client.controller.BaseController;
import com.proftest.client.util.AlertUtil;
import com.proftest.common.dto.Response;
import com.proftest.common.protocol.RequestType;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class ProfileController extends BaseController implements Initializable {

    @FXML private Label loginLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label departmentLabel;
    @FXML private Label positionLabel;
    @FXML private Label rolesLabel;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label statusLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (currentUser != null) {
            loginLabel.setText(currentUser.getUser().getLogin());
            if (currentUser.getUser().getPerson() != null) {
                fullNameLabel.setText(currentUser.getUser().getPerson().getFullName());
                emailLabel.setText(currentUser.getUser().getPerson().getEmail());
                phoneLabel.setText(currentUser.getUser().getPerson().getPhone() != null ? currentUser.getUser().getPerson().getPhone() : "—");
                departmentLabel.setText(currentUser.getUser().getPerson().getDepartment() != null ? currentUser.getUser().getPerson().getDepartment() : "—");
                positionLabel.setText(currentUser.getUser().getPerson().getPosition() != null ? currentUser.getUser().getPerson().getPosition() : "—");
            }
            if (currentUser.getUser().getRoles() != null) {
                rolesLabel.setText(String.join(", ", currentUser.getUser().getRoles()));
            }
        }
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        String oldPwd = oldPasswordField.getText();
        String newPwd = newPasswordField.getText();
        String confirmPwd = confirmPasswordField.getText();

        if (oldPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
            statusLabel.setText("Заполните все поля");
            return;
        }
        if (!newPwd.equals(confirmPwd)) {
            statusLabel.setText("Новые пароли не совпадают");
            return;
        }
        if (newPwd.length() < 6) {
            statusLabel.setText("Пароль должен содержать минимум 6 символов");
            return;
        }

        Map<String, String> payload = Map.of("oldPassword", oldPwd, "newPassword", newPwd);
        new Thread(() -> {
            Response resp = connection.send(RequestType.CHANGE_PASSWORD, payload);
            Platform.runLater(() -> {
                if (resp.isSuccess()) {
                    statusLabel.setText("Пароль успешно изменён");
                    oldPasswordField.clear();
                    newPasswordField.clear();
                    confirmPasswordField.clear();
                    AlertUtil.showInfo("Успех", "Пароль изменён успешно");
                } else {
                    statusLabel.setText(resp.getMessage());
                    AlertUtil.showError("Ошибка", resp.getMessage());
                }
            });
        }).start();
    }
}
