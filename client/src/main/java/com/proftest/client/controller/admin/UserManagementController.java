package com.proftest.client.controller.admin;

import com.google.gson.reflect.TypeToken;
import com.proftest.client.controller.BaseController;
import com.proftest.client.util.AlertUtil;
import com.proftest.common.dto.RegisterDto;
import com.proftest.common.dto.Response;
import com.proftest.common.dto.UserDto;
import com.proftest.common.protocol.RequestType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class UserManagementController extends BaseController implements Initializable {

    @FXML private TableView<UserDto> usersTable;
    @FXML private TableColumn<UserDto, Long> idCol;
    @FXML private TableColumn<UserDto, String> loginCol;
    @FXML private TableColumn<UserDto, String> fullNameCol;
    @FXML private TableColumn<UserDto, String> emailCol;
    @FXML private TableColumn<UserDto, Boolean> activeCol;
    @FXML private TableColumn<UserDto, String> rolesCol;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private ComboBox<String> roleFilterCombo;

    private ObservableList<UserDto> allUsers = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadUsers();
    }

    private void setupTable() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        loginCol.setCellValueFactory(new PropertyValueFactory<>("login"));
        fullNameCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getPerson() != null ? data.getValue().getPerson().getFullName() : ""));
        emailCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getPerson() != null ? data.getValue().getPerson().getEmail() : ""));
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        rolesCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getRoles() != null ? String.join(", ", data.getValue().getRoles()) : ""));

        searchField.textProperty().addListener((obs, old, val) -> filterUsers(val));
    }

    private void loadUsers() {
        statusLabel.setText("Загрузка...");
        new Thread(() -> {
            try {
                Response response = connection.send(RequestType.GET_ALL_USERS, (String) null);
                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        Type type = new TypeToken<List<UserDto>>(){}.getType();
                        List<UserDto> users = connection.getGson().fromJson(response.getPayload(), type);
                        allUsers.setAll(users);
                        usersTable.setItems(allUsers);
                        statusLabel.setText("Пользователей: " + users.size());
                    } else {
                        AlertUtil.showError("Ошибка", response.getMessage());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.showError("Ошибка", e.getMessage()));
            }
        }).start();
    }

    private void filterUsers(String text) {
        if (text == null || text.isEmpty()) {
            usersTable.setItems(allUsers);
        } else {
            String lower = text.toLowerCase();
            ObservableList<UserDto> filtered = allUsers.filtered(u ->
                u.getLogin().toLowerCase().contains(lower) ||
                (u.getPerson() != null && u.getPerson().getFullName().toLowerCase().contains(lower)));
            usersTable.setItems(filtered);
        }
    }

    @FXML private void handleToggleActive() {
        UserDto selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtil.showWarning("Внимание", "Выберите пользователя"); return; }
        new Thread(() -> {
            Response response = connection.send(RequestType.TOGGLE_USER_ACTIVE, selected.getUserId());
            Platform.runLater(() -> {
                if (response.isSuccess()) loadUsers();
                else AlertUtil.showError("Ошибка", response.getMessage());
            });
        }).start();
    }

    @FXML private void handleSetRole() {
        UserDto selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtil.showWarning("Внимание", "Выберите пользователя"); return; }

        ChoiceDialog<String> dialog = new ChoiceDialog<>("CANDIDATE",
                List.of("ADMIN", "HR_MANAGER", "CANDIDATE", "EMPLOYEE"));
        dialog.setTitle("Изменить роль");
        dialog.setHeaderText("Пользователь: " + selected.getLogin());
        dialog.setContentText("Выберите роль:");
        dialog.showAndWait().ifPresent(role -> {
            new Thread(() -> {
                Map<String, String> payload = Map.of("userId", String.valueOf(selected.getUserId()), "role", role);
                Response response = connection.send(RequestType.SET_USER_ROLE, payload);
                Platform.runLater(() -> {
                    if (response.isSuccess()) { loadUsers(); AlertUtil.showInfo("Успех", "Роль назначена"); }
                    else AlertUtil.showError("Ошибка", response.getMessage());
                });
            }).start();
        });
    }

    @FXML private void handleDeleteUser() {
        UserDto selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtil.showWarning("Внимание", "Выберите пользователя"); return; }
        if (!AlertUtil.showConfirm("Удаление", "Удалить пользователя " + selected.getLogin() + "?")) return;
        new Thread(() -> {
            Response response = connection.send(RequestType.DELETE_USER, selected.getUserId());
            Platform.runLater(() -> {
                if (response.isSuccess()) loadUsers();
                else AlertUtil.showError("Ошибка", response.getMessage());
            });
        }).start();
    }

    @FXML private void handleAddUser(ActionEvent event) {
        openWindow("/fxml/Register.fxml", "Добавить пользователя", 520, 580);
    }

    @FXML private void handleRefresh() { loadUsers(); }

    @FXML private void handleBack(ActionEvent event) {
        switchScene(getStage(event), "/fxml/Main.fxml", "Главное меню", 1100, 720);
    }
}
