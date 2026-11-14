package com.proftest.client.controller.hr;

import com.google.gson.reflect.TypeToken;
import com.proftest.client.controller.BaseController;
import com.proftest.client.util.AlertUtil;
import com.proftest.common.dto.QuestionDto;
import com.proftest.common.dto.Response;
import com.proftest.common.dto.TestDto;
import com.proftest.common.protocol.RequestType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class TestQuestionsController extends BaseController implements Initializable {

    @FXML private Label testTitleLabel;
    @FXML private TableView<QuestionDto> testQuestionsTable;
    @FXML private TableColumn<QuestionDto, Long> tqIdCol;
    @FXML private TableColumn<QuestionDto, String> tqTextCol;
    @FXML private TableColumn<QuestionDto, String> tqTypeCol;
    @FXML private TableColumn<QuestionDto, Integer> tqPointsCol;

    @FXML private TableView<QuestionDto> allQuestionsTable;
    @FXML private TableColumn<QuestionDto, Long> aqIdCol;
    @FXML private TableColumn<QuestionDto, String> aqTextCol;
    @FXML private TableColumn<QuestionDto, String> aqTypeCol;
    @FXML private TableColumn<QuestionDto, String> aqTopicCol;

    private TestDto currentTest;
    private ObservableList<QuestionDto> testQuestions = FXCollections.observableArrayList();
    private ObservableList<QuestionDto> allQuestions = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTables();
    }

    private void setupTables() {
        tqIdCol.setCellValueFactory(new PropertyValueFactory<>("questionId"));
        tqTextCol.setCellValueFactory(new PropertyValueFactory<>("text"));
        tqTypeCol.setCellValueFactory(new PropertyValueFactory<>("questionType"));
        tqPointsCol.setCellValueFactory(new PropertyValueFactory<>("points"));
        testQuestionsTable.setItems(testQuestions);

        aqIdCol.setCellValueFactory(new PropertyValueFactory<>("questionId"));
        aqTextCol.setCellValueFactory(new PropertyValueFactory<>("text"));
        aqTypeCol.setCellValueFactory(new PropertyValueFactory<>("questionType"));
        aqTopicCol.setCellValueFactory(new PropertyValueFactory<>("topic"));
        allQuestionsTable.setItems(allQuestions);
    }

    public void setTest(TestDto test) {
        this.currentTest = test;
        testTitleLabel.setText("Тест: " + test.getTitle());
        loadTestQuestions();
        loadAllQuestions();
    }

    private void loadTestQuestions() {
        new Thread(() -> {
            Response resp = connection.send(RequestType.GET_TEST_BY_ID, currentTest.getTestId());
            Platform.runLater(() -> {
                if (resp.isSuccess()) {
                    TestDto full = connection.getGson().fromJson(resp.getPayload(), TestDto.class);
                    if (full.getQuestions() != null) testQuestions.setAll(full.getQuestions());
                }
            });
        }).start();
    }

    private void loadAllQuestions() {
        new Thread(() -> {
            Response resp = connection.send(RequestType.GET_ALL_QUESTIONS, (String) null);
            Platform.runLater(() -> {
                if (resp.isSuccess()) {
                    Type type = new TypeToken<List<QuestionDto>>(){}.getType();
                    List<QuestionDto> qs = connection.getGson().fromJson(resp.getPayload(), type);
                    allQuestions.setAll(qs);
                }
            });
        }).start();
    }

    @FXML private void handleAddQuestion() {
        QuestionDto selected = allQuestionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtil.showWarning("Внимание", "Выберите вопрос из банка"); return; }
        new Thread(() -> {
            Map<String, Long> payload = Map.of("testId", currentTest.getTestId(), "questionId", selected.getQuestionId());
            Response resp = connection.send(RequestType.ADD_QUESTION_TO_TEST, payload);
            Platform.runLater(() -> {
                if (resp.isSuccess()) loadTestQuestions();
                else AlertUtil.showError("Ошибка", resp.getMessage());
            });
        }).start();
    }

    @FXML private void handleRemoveQuestion() {
        QuestionDto selected = testQuestionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtil.showWarning("Внимание", "Выберите вопрос из теста"); return; }
        new Thread(() -> {
            Map<String, Long> payload = Map.of("testId", currentTest.getTestId(), "questionId", selected.getQuestionId());
            Response resp = connection.send(RequestType.REMOVE_QUESTION_FROM_TEST, payload);
            Platform.runLater(() -> {
                if (resp.isSuccess()) loadTestQuestions();
                else AlertUtil.showError("Ошибка", resp.getMessage());
            });
        }).start();
    }
}
