package com.proftest.client.controller.hr;

import com.google.gson.reflect.TypeToken;
import com.proftest.client.controller.BaseController;
import com.proftest.client.util.AlertUtil;
import com.proftest.common.dto.*;
import com.proftest.common.protocol.RequestType;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
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
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class HrUsersController extends BaseController implements Initializable {

    // --- Левая панель: список пользователей ---
    @FXML private TableView<UserDto> usersTable;
    @FXML private TableColumn<UserDto, String> loginCol;
    @FXML private TableColumn<UserDto, String> fullNameCol;
    @FXML private TableColumn<UserDto, String> departmentCol;
    @FXML private TableColumn<UserDto, String> rolesCol;
    @FXML private TableColumn<UserDto, String> activeCol;
    @FXML private TextField searchField;
    @FXML private Label userCountLabel;

    // --- Правая панель: детали выбранного пользователя ---
    @FXML private Label selectedUserLabel;
    @FXML private Label selectedEmailLabel;
    @FXML private Label selectedPositionLabel;
    @FXML private Label selectedDeptLabel;
    @FXML private TabPane detailTabs;

    // Назначения
    @FXML private TableView<AssignmentDto> assignmentsTable;
    @FXML private TableColumn<AssignmentDto, String> aTestCol;
    @FXML private TableColumn<AssignmentDto, String> aDeadlineCol;
    @FXML private TableColumn<AssignmentDto, Integer> aAttemptsCol;
    @FXML private TableColumn<AssignmentDto, String> aAssignedByCol;

    // Результаты
    @FXML private TableView<TestAttemptDto> resultsTable;
    @FXML private TableColumn<TestAttemptDto, String> rTestCol;
    @FXML private TableColumn<TestAttemptDto, String> rStartCol;
    @FXML private TableColumn<TestAttemptDto, String> rScoreCol;
    @FXML private TableColumn<TestAttemptDto, String> rPassedCol;
    @FXML private TableColumn<TestAttemptDto, String> rStatusCol;

    private final ObservableList<UserDto> allUsers = FXCollections.observableArrayList();
    private final ObservableList<UserDto> filteredUsers = FXCollections.observableArrayList();
    private final ObservableList<AssignmentDto> userAssignments = FXCollections.observableArrayList();
    private final ObservableList<TestAttemptDto> userResults = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupUsersTable();
        setupAssignmentsTable();
        setupResultsTable();
        setupSearch();
        setupSelectionListener();
        loadUsers();
    }

    private void setupUsersTable() {
        loginCol.setCellValueFactory(new PropertyValueFactory<>("login"));
        fullNameCol.setCellValueFactory(data -> {
            PersonDto p = data.getValue().getPerson();
            return new SimpleStringProperty(p != null ? p.getFullName() : "—");
        });
        departmentCol.setCellValueFactory(data -> {
            PersonDto p = data.getValue().getPerson();
            return new SimpleStringProperty(p != null && p.getDepartment() != null ? p.getDepartment() : "—");
        });
        rolesCol.setCellValueFactory(data -> {
            List<String> roles = data.getValue().getRoles();
            return new SimpleStringProperty(roles != null ? String.join(", ", roles) : "—");
        });
        activeCol.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().isActive() ? "✓ Активен" : "✗ Заблокирован"));
        usersTable.setItems(filteredUsers);
    }

    private void setupAssignmentsTable() {
        aTestCol.setCellValueFactory(new PropertyValueFactory<>("testTitle"));
        aDeadlineCol.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getDeadline() != null ? data.getValue().getDeadline() : "Без дедлайна"));
        aAttemptsCol.setCellValueFactory(new PropertyValueFactory<>("allowedAttempts"));
        aAssignedByCol.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getAssignedByLogin() != null ? data.getValue().getAssignedByLogin() : "—"));
        assignmentsTable.setItems(userAssignments);
    }

    private void setupResultsTable() {
        rTestCol.setCellValueFactory(new PropertyValueFactory<>("testTitle"));
        rStartCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        rScoreCol.setCellValueFactory(data -> {
            Double score = data.getValue().getScore();
            return new SimpleStringProperty(score != null ? String.format("%.1f%%", score) : "—");
        });
        rPassedCol.setCellValueFactory(data -> {
            Boolean passed = data.getValue().getPassed();
            if (passed == null) return new SimpleStringProperty("—");
            return new SimpleStringProperty(passed ? "✓ Пройден" : "✗ Не пройден");
        });
        rStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        resultsTable.setItems(userResults);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterUsers(newVal));
    }

    private void filterUsers(String query) {
        if (query == null || query.isBlank()) {
            filteredUsers.setAll((java.util.Collection<UserDto>) allUsers);
        } else {
            String low = query.toLowerCase();
            List<UserDto> result = allUsers.stream()
                    .filter(u -> u.getLogin().toLowerCase().contains(low)
                            || (u.getPerson() != null && u.getPerson().getFullName() != null
                                && u.getPerson().getFullName().toLowerCase().contains(low))
                            || (u.getPerson() != null && u.getPerson().getDepartment() != null
                                && u.getPerson().getDepartment().toLowerCase().contains(low)))
                    .collect(Collectors.toList());
            filteredUsers.setAll((java.util.Collection<UserDto>) result);
        }
        userCountLabel.setText("Пользователей: " + filteredUsers.size());
    }

    private void setupSelectionListener() {
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected == null) {
                clearDetails();
                return;
            }
            showUserDetails(selected);
            loadUserData(selected.getUserId());
        });
    }

    private void showUserDetails(UserDto u) {
        PersonDto p = u.getPerson();
        selectedUserLabel.setText(p != null ? p.getFullName() : u.getLogin());
        selectedEmailLabel.setText("Email: " + (p != null && p.getEmail() != null ? p.getEmail() : "—"));
        selectedPositionLabel.setText("Должность: " + (p != null && p.getPosition() != null ? p.getPosition() : "—"));
        selectedDeptLabel.setText("Отдел: " + (p != null && p.getDepartment() != null ? p.getDepartment() : "—"));
    }

    private void clearDetails() {
        selectedUserLabel.setText("Выберите пользователя");
        selectedEmailLabel.setText("Email: —");
        selectedPositionLabel.setText("Должность: —");
        selectedDeptLabel.setText("Отдел: —");
        userAssignments.clear();
        userResults.clear();
    }

    private void loadUsers() {
        userCountLabel.setText("Загрузка...");
        new Thread(() -> {
            Response resp = connection.send(RequestType.GET_ALL_USERS, (String) null);
            Platform.runLater(() -> {
                if (resp.isSuccess()) {
                    Type type = new TypeToken<List<UserDto>>(){}.getType();
                    List<UserDto> users = connection.getGson().fromJson(resp.getPayload(), type);
                    allUsers.setAll((java.util.Collection<UserDto>) users);
                    filteredUsers.setAll((java.util.Collection<UserDto>) users);
                    userCountLabel.setText("Пользователей: " + users.size());
                } else {
                    AlertUtil.showError("Ошибка", resp.getMessage());
                }
            });
        }).start();
    }

    private void loadUserData(Long userId) {
        userAssignments.clear();
        userResults.clear();
        new Thread(() -> {
            Response aResp = connection.send(RequestType.GET_ALL_ASSIGNMENTS, (String) null);
            Response rResp = connection.send(RequestType.GET_REPORT_BY_USER, userId);
            Platform.runLater(() -> {
                if (aResp.isSuccess()) {
                    Type t = new TypeToken<List<AssignmentDto>>(){}.getType();
                    List<AssignmentDto> all = connection.getGson().fromJson(aResp.getPayload(), t);
                    List<AssignmentDto> mine = all.stream()
                            .filter(a -> userId.equals(a.getUserId()))
                            .collect(Collectors.toList());
                    userAssignments.setAll((java.util.Collection<AssignmentDto>) mine);
                }
                if (rResp.isSuccess()) {
                    Type t = new TypeToken<List<TestAttemptDto>>(){}.getType();
                    List<TestAttemptDto> results = connection.getGson().fromJson(rResp.getPayload(), t);
                    userResults.setAll((java.util.Collection<TestAttemptDto>) results);
                }
            });
        }).start();
    }

    @FXML private void handleSearch() {
        filterUsers(searchField.getText());
    }

    @FXML private void handleRefresh() {
        searchField.clear();
        loadUsers();
        clearDetails();
    }

    @FXML private void handleBack(ActionEvent event) {
        switchScene(getStage(event), "/fxml/Main.fxml", "Главное меню", 1100, 720);
    }
}
