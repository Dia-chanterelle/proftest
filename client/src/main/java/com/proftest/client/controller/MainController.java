package com.proftest.client.controller;

import com.proftest.client.util.AlertUtil;
import com.proftest.common.protocol.RequestType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController extends BaseController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private VBox adminMenu;
    @FXML private VBox hrMenu;
    @FXML private VBox candidateMenu;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (currentUser != null) {
            welcomeLabel.setText("Добро пожаловать, " + currentUser.getUser().getPerson().getFullName() + "!");
            String roles = String.join(", ", currentUser.getUser().getRoles());
            roleLabel.setText("Роль: " + roles);

            if (adminMenu != null) adminMenu.setVisible(isAdmin());
            if (hrMenu != null) hrMenu.setVisible(hasManageAccess());
            if (candidateMenu != null) candidateMenu.setVisible(true);
        }
    }

    // --- Admin section ---
    @FXML private void openUserManagement(ActionEvent event) {
        switchScene(getStage(event), "/fxml/admin/UserManagement.fxml", "Управление пользователями");
    }

    @FXML private void openRoleManagement(ActionEvent event) {
        openWindow("/fxml/admin/RoleManagement.fxml", "Управление ролями", 650, 500);
    }

    // --- HR section ---
    @FXML private void openHrUsers(ActionEvent event) {
        switchScene(getStage(event), "/fxml/hr/HrUsers.fxml", "Пользователи", 1100, 720);
    }

    @FXML private void openTestManagement(ActionEvent event) {
        switchScene(getStage(event), "/fxml/hr/TestManagement.fxml", "Управление тестами");
    }

    @FXML private void openQuestionBank(ActionEvent event) {
        switchScene(getStage(event), "/fxml/hr/QuestionBank.fxml", "Банк вопросов");
    }

    @FXML private void openSkillManagement(ActionEvent event) {
        switchScene(getStage(event), "/fxml/hr/SkillManagement.fxml", "Навыки");
    }

    @FXML private void openAssignmentManagement(ActionEvent event) {
        switchScene(getStage(event), "/fxml/hr/AssignmentManagement.fxml", "Назначения тестов");
    }

    @FXML private void openReports(ActionEvent event) {
        switchScene(getStage(event), "/fxml/hr/Reports.fxml", "Отчёты");
    }

    // --- Candidate section ---
    @FXML private void openMyTests(ActionEvent event) {
        switchScene(getStage(event), "/fxml/candidate/MyTests.fxml", "Мои тесты");
    }

    @FXML private void openMyResults(ActionEvent event) {
        switchScene(getStage(event), "/fxml/candidate/MyResults.fxml", "Мои результаты");
    }

    @FXML private void openProfile(ActionEvent event) {
        openWindow("/fxml/candidate/Profile.fxml", "Профиль", 520, 480);
    }

    // --- Logout ---
    @FXML private void handleLogout(ActionEvent event) {
        if (!AlertUtil.showConfirm("Выход", "Вы уверены, что хотите выйти?")) return;
        try {
            connection.send(RequestType.LOGOUT, (String) null);
        } catch (Exception ignored) {}
        connection.setAuthToken(null);
        currentUser = null;
        Stage stage = getStage(event);
        stage.setResizable(true);
        switchScene(stage, "/fxml/Login.fxml", "Авторизация", 480, 485);
    }
}
