package com.proftest.client.controller.candidate;

import com.google.gson.reflect.TypeToken;
import com.proftest.client.controller.BaseController;
import com.proftest.client.util.AlertUtil;
import com.proftest.common.dto.AssignmentDto;
import com.proftest.common.dto.Response;
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
import java.util.ResourceBundle;

public class MyTestsController extends BaseController implements Initializable {

    @FXML private TableView<AssignmentDto> table;
    @FXML private TableColumn<AssignmentDto, String> testCol;
    @FXML private TableColumn<AssignmentDto, String> deadlineCol;
    @FXML private TableColumn<AssignmentDto, Integer> attemptsCol;
    @FXML private TableColumn<AssignmentDto, Integer> usedCol;
    @FXML private TableColumn<AssignmentDto, String> statusCol;
    @FXML private Label statusLabel;

    private ObservableList<AssignmentDto> assignments = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        testCol.setCellValueFactory(new PropertyValueFactory<>("testTitle"));
        deadlineCol.setCellValueFactory(new PropertyValueFactory<>("deadline"));
        attemptsCol.setCellValueFactory(new PropertyValueFactory<>("allowedAttempts"));
        usedCol.setCellValueFactory(new PropertyValueFactory<>("attemptsUsed"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        table.setItems(assignments);
        loadMyTests();
    }

    private void loadMyTests() {
        statusLabel.setText("Загрузка...");
        new Thread(() -> {
            try {
                Response resp = connection.send(RequestType.GET_MY_ASSIGNMENTS, (String) null);
                Platform.runLater(() -> {
                    if (resp.isSuccess()) {
                        Type type = new TypeToken<List<AssignmentDto>>(){}.getType();
                        List<AssignmentDto> list = connection.getGson().fromJson(resp.getPayload(), type);
                        assignments.setAll(list);
                        statusLabel.setText("Назначений: " + list.size());
                    } else AlertUtil.showError("Ошибка", resp.getMessage());
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.showError("Ошибка", e.getMessage()));
            }
        }).start();
    }

    @FXML private void handleStartTest(ActionEvent event) {
        AssignmentDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtil.showWarning("Внимание", "Выберите тест"); return; }
        if ("COMPLETED".equals(selected.getStatus())) {
            AlertUtil.showWarning("Внимание", "Попытки исчерпаны"); return;
        }
        if ("EXPIRED".equals(selected.getStatus())) {
            AlertUtil.showWarning("Внимание", "Дедлайн истёк"); return;
        }
        if (!AlertUtil.showConfirm("Начать тест", "Начать тест \"" + selected.getTestTitle() + "\"?\nВремя пойдёт сразу после подтверждения.")) return;

        TakeTestController ctrl = openWindow("/fxml/candidate/TakeTest.fxml", "Прохождение теста: " + selected.getTestTitle(), 900, 700);
        if (ctrl != null) {
            ctrl.setOnCloseCallback(this::loadMyTests);
            ctrl.startTest(selected.getAssignmentId());
        }
    }

    @FXML private void handleRefresh() { loadMyTests(); }

    @FXML private void handleBack(ActionEvent event) {
        switchScene(getStage(event), "/fxml/Main.fxml", "Главное меню", 1100, 720);
    }
}
