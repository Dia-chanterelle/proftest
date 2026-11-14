package com.proftest.client.controller.hr;

import com.google.gson.reflect.TypeToken;
import com.proftest.client.controller.BaseController;
import com.proftest.client.util.AlertUtil;
import com.proftest.common.dto.*;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AssignmentController extends BaseController implements Initializable {

    @FXML private TableView<AssignmentDto> table;
    @FXML private TableColumn<AssignmentDto, Long> idCol;
    @FXML private TableColumn<AssignmentDto, String> testCol;
    @FXML private TableColumn<AssignmentDto, String> userCol;
    @FXML private TableColumn<AssignmentDto, String> deadlineCol;
    @FXML private TableColumn<AssignmentDto, Integer> attemptsCol;
    @FXML private Label statusLabel;

    private ObservableList<AssignmentDto> assignments = FXCollections.observableArrayList();
    private List<UserDto> users;
    private List<TestDto> tests;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        idCol.setCellValueFactory(new PropertyValueFactory<>("assignmentId"));
        testCol.setCellValueFactory(new PropertyValueFactory<>("testTitle"));
        userCol.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getUserFullName() + " (" + data.getValue().getUserLogin() + ")"));
        deadlineCol.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        attemptsCol.setCellValueFactory(new PropertyValueFactory<>("allowedAttempts"));
        table.setItems(assignments);
        loadData();
    }

    private void loadData() {
        statusLabel.setText("Загрузка...");
        new Thread(() -> {
            try {
                Response aResp = connection.send(RequestType.GET_ALL_ASSIGNMENTS, (String) null);
                Response uResp = connection.send(RequestType.GET_ALL_USERS, (String) null);
                Response tResp = connection.send(RequestType.GET_ALL_TESTS, (String) null);
                Platform.runLater(() -> {
                    if (aResp.isSuccess()) {
                        Type type = new TypeToken<List<AssignmentDto>>(){}.getType();
                        List<AssignmentDto> loaded = connection.getGson().fromJson(aResp.getPayload(), type);
                        assignments.setAll((java.util.Collection<AssignmentDto>) loaded);
                        statusLabel.setText("Назначений: " + assignments.size());
                    }
                    if (uResp.isSuccess()) {
                        Type ut = new TypeToken<List<UserDto>>(){}.getType();
                        users = connection.getGson().fromJson(uResp.getPayload(), ut);
                    }
                    if (tResp.isSuccess()) {
                        Type tt = new TypeToken<List<TestDto>>(){}.getType();
                        tests = connection.getGson().fromJson(tResp.getPayload(), tt);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.showError("Ошибка", e.getMessage()));
            }
        }).start();
    }

    @FXML private void handleCreate() {
        if (users == null || tests == null) { AlertUtil.showWarning("Подождите", "Данные ещё загружаются"); return; }

        Dialog<AssignmentDto> dialog = new Dialog<>();
        dialog.setTitle("Назначить тест");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<UserDto> userCombo = new ComboBox<>(FXCollections.observableArrayList(users));
        userCombo.setConverter(new javafx.util.StringConverter<>() {
            public String toString(UserDto u) { return u == null ? "" : u.getLogin() + " (" + (u.getPerson() != null ? u.getPerson().getFullName() : "") + ")"; }
            public UserDto fromString(String s) { return null; }
        });
        List<TestDto> activeTests = tests.stream().filter(TestDto::isActive).collect(java.util.stream.Collectors.toList());
        ComboBox<TestDto> testCombo = new ComboBox<>(FXCollections.observableArrayList(activeTests));
        testCombo.setConverter(new javafx.util.StringConverter<>() {
            public String toString(TestDto t) { return t == null ? "" : t.getTitle(); }
            public TestDto fromString(String s) { return null; }
        });
        TextField attemptsField = new TextField("1");

        // Deadline: DatePicker + time field (prevents manual format errors)
        javafx.scene.control.DatePicker datePicker = new javafx.scene.control.DatePicker();
        datePicker.setPromptText("Выберите дату (необязательно)");
        datePicker.setPrefWidth(160);
        TextField timeField = new TextField("23:59:59");
        timeField.setPromptText("HH:mm:ss");
        timeField.setPrefWidth(90);
        javafx.scene.layout.HBox deadlineBox = new javafx.scene.layout.HBox(6, datePicker,
                new Label("Время:"), timeField);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(8);
        grid.add(new Label("Пользователь:"), 0, 0); grid.add(userCombo, 1, 0);
        grid.add(new Label("Тест:"), 0, 1); grid.add(testCombo, 1, 1);
        grid.add(new Label("Попыток:"), 0, 2); grid.add(attemptsField, 1, 2);
        grid.add(new Label("Дедлайн:"), 0, 3); grid.add(deadlineBox, 1, 3);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            if (userCombo.getValue() == null || testCombo.getValue() == null) return null;
            AssignmentDto dto = new AssignmentDto();
            dto.setUserId(userCombo.getValue().getUserId());
            dto.setTestId(testCombo.getValue().getTestId());
            try { dto.setAllowedAttempts(Integer.parseInt(attemptsField.getText().trim())); } catch (NumberFormatException e) { dto.setAllowedAttempts(1); }
            if (datePicker.getValue() != null) {
                String time = timeField.getText().trim().isEmpty() ? "23:59:59" : timeField.getText().trim();
                // Ensure time has seconds (accept HH:mm as well)
                if (time.matches("\\d{2}:\\d{2}")) time = time + ":00";
                dto.setDeadline(datePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " " + time);
            }
            return dto;
        });

        dialog.showAndWait().ifPresent(dto -> {
            new Thread(() -> {
                Response resp = connection.send(RequestType.CREATE_ASSIGNMENT, dto);
                Platform.runLater(() -> {
                    if (resp.isSuccess()) { loadData(); AlertUtil.showInfo("Успех", "Тест назначен"); }
                    else AlertUtil.showError("Ошибка", resp.getMessage());
                });
            }).start();
        });
    }

    @FXML private void handleDelete() {
        AssignmentDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtil.showWarning("Внимание", "Выберите назначение"); return; }
        if (!AlertUtil.showConfirm("Удаление", "Удалить назначение?")) return;
        new Thread(() -> {
            Response resp = connection.send(RequestType.DELETE_ASSIGNMENT, selected.getAssignmentId());
            Platform.runLater(() -> { if (resp.isSuccess()) loadData(); else AlertUtil.showError("Ошибка", resp.getMessage()); });
        }).start();
    }

    @FXML private void handleRefresh() { loadData(); }

    @FXML private void handleBack(ActionEvent event) {
        switchScene(getStage(event), "/fxml/Main.fxml", "Главное меню", 1100, 720);
    }
}
