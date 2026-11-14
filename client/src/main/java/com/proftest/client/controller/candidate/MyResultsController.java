package com.proftest.client.controller.candidate;

import com.google.gson.reflect.TypeToken;
import com.proftest.client.controller.BaseController;
import com.proftest.client.util.AlertUtil;
import com.proftest.common.dto.Response;
import com.proftest.common.dto.TestAttemptDto;
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

public class MyResultsController extends BaseController implements Initializable {

    @FXML private TableView<TestAttemptDto> table;
    @FXML private TableColumn<TestAttemptDto, String> testCol;
    @FXML private TableColumn<TestAttemptDto, String> startTimeCol;
    @FXML private TableColumn<TestAttemptDto, String> endTimeCol;
    @FXML private TableColumn<TestAttemptDto, Double> scoreCol;
    @FXML private TableColumn<TestAttemptDto, String> statusCol;
    @FXML private TableColumn<TestAttemptDto, Boolean> passedCol;
    @FXML private Label statusLabel;
    @FXML private Label summaryLabel;

    private ObservableList<TestAttemptDto> results = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        testCol.setCellValueFactory(new PropertyValueFactory<>("testTitle"));
        startTimeCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endTimeCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        passedCol.setCellValueFactory(new PropertyValueFactory<>("passed"));
        table.setItems(results);
        loadResults();
    }

    private void loadResults() {
        statusLabel.setText("Загрузка...");
        new Thread(() -> {
            try {
                Response resp = connection.send(RequestType.GET_MY_RESULTS, (String) null);
                Platform.runLater(() -> {
                    if (resp.isSuccess()) {
                        Type type = new TypeToken<List<TestAttemptDto>>(){}.getType();
                        List<TestAttemptDto> list = connection.getGson().fromJson(resp.getPayload(), type);
                        results.setAll(list);
                        statusLabel.setText("Результатов: " + list.size());
                        long passed = list.stream().filter(r -> Boolean.TRUE.equals(r.getPassed())).count();
                        if (summaryLabel != null)
                            summaryLabel.setText("Прошли: " + passed + " / " + list.size());
                    } else AlertUtil.showError("Ошибка", resp.getMessage());
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.showError("Ошибка", e.getMessage()));
            }
        }).start();
    }

    @FXML private void handleRefresh() { loadResults(); }

    @FXML private void handleBack(ActionEvent event) {
        switchScene(getStage(event), "/fxml/Main.fxml", "Главное меню", 1100, 720);
    }
}
