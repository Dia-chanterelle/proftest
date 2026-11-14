package com.proftest.client.controller.hr;

import com.google.gson.reflect.TypeToken;
import com.proftest.client.controller.BaseController;
import com.proftest.client.util.AlertUtil;
import com.proftest.common.dto.QuestionDto;
import com.proftest.common.dto.QuestionOptionDto;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class QuestionBankController extends BaseController implements Initializable {

    @FXML private TableView<QuestionDto> questionsTable;
    @FXML private TableColumn<QuestionDto, Long> idCol;
    @FXML private TableColumn<QuestionDto, String> textCol;
    @FXML private TableColumn<QuestionDto, String> typeCol;
    @FXML private TableColumn<QuestionDto, String> topicCol;
    @FXML private TableColumn<QuestionDto, Integer> pointsCol;
    @FXML private TableColumn<QuestionDto, Integer> difficultyCol;
    @FXML private Label statusLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilterCombo;

    private ObservableList<QuestionDto> allQuestions = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        idCol.setCellValueFactory(new PropertyValueFactory<>("questionId"));
        textCol.setCellValueFactory(new PropertyValueFactory<>("text"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("questionType"));
        topicCol.setCellValueFactory(new PropertyValueFactory<>("topic"));
        pointsCol.setCellValueFactory(new PropertyValueFactory<>("points"));
        difficultyCol.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
        searchField.textProperty().addListener((obs, old, val) -> filterQuestions());
        if (typeFilterCombo != null) {
            typeFilterCombo.setItems(FXCollections.observableArrayList("ВСЕ", "SINGLE_CHOICE", "MULTIPLE_CHOICE"));
            typeFilterCombo.setValue("ВСЕ");
            typeFilterCombo.valueProperty().addListener((obs, old, val) -> filterQuestions());
        }
        loadQuestions();
    }

    private void loadQuestions() {
        statusLabel.setText("Загрузка...");
        new Thread(() -> {
            try {
                Response response = connection.send(RequestType.GET_ALL_QUESTIONS, (String) null);
                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        Type type = new TypeToken<List<QuestionDto>>(){}.getType();
                        List<QuestionDto> qs = connection.getGson().fromJson(response.getPayload(), type);
                        allQuestions.setAll(qs);
                        questionsTable.setItems(allQuestions);
                        statusLabel.setText("Вопросов: " + qs.size());
                    } else AlertUtil.showError("Ошибка", response.getMessage());
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.showError("Ошибка", e.getMessage()));
            }
        }).start();
    }

    private void filterQuestions() {
        String text = searchField.getText().toLowerCase();
        String typeFilter = typeFilterCombo != null ? typeFilterCombo.getValue() : "ВСЕ";
        questionsTable.setItems(allQuestions.filtered(q -> {
            boolean matchText = text.isEmpty() || q.getText().toLowerCase().contains(text) ||
                    (q.getTopic() != null && q.getTopic().toLowerCase().contains(text));
            boolean matchType = "ВСЕ".equals(typeFilter) || q.getQuestionType().equals(typeFilter);
            return matchText && matchType;
        }));
    }

    @FXML private void handleCreate(ActionEvent event) {
        openQuestionEditor(null);
    }

    @FXML private void handleEdit() {
        QuestionDto selected = questionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtil.showWarning("Внимание", "Выберите вопрос"); return; }
        openQuestionEditor(selected);
    }

    private void openQuestionEditor(QuestionDto existing) {
        Dialog<QuestionDto> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Создать вопрос" : "Редактировать вопрос");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefSize(600, 500);

        TextArea textArea = new TextArea(existing != null ? existing.getText() : "");
        textArea.setPrefRowCount(3);
        ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList("SINGLE_CHOICE", "MULTIPLE_CHOICE"));
        String existingType = existing != null ? existing.getQuestionType() : "SINGLE_CHOICE";
        typeCombo.setValue("SINGLE_CHOICE".equals(existingType) || "MULTIPLE_CHOICE".equals(existingType) ? existingType : "SINGLE_CHOICE");
        TextField topicField = new TextField(existing != null && existing.getTopic() != null ? existing.getTopic() : "");
        TextField pointsField = new TextField(existing != null ? String.valueOf(existing.getPoints()) : "1");
        TextField difficultyField = new TextField(existing != null && existing.getDifficulty() != null ? String.valueOf(existing.getDifficulty()) : "1");

        // Options editor area
        TextArea optionsArea = new TextArea();
        optionsArea.setPromptText("Варианты ответов (один на строку, * перед правильным):\n* Правильный ответ\nНеправильный ответ");
        if (existing != null && existing.getOptions() != null) {
            StringBuilder sb = new StringBuilder();
            for (QuestionOptionDto o : existing.getOptions()) {
                sb.append(o.isCorrect() ? "* " : "").append(o.getOptionText()).append("\n");
            }
            optionsArea.setText(sb.toString().trim());
        }

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(8);
        grid.add(new Label("Текст вопроса:"), 0, 0); grid.add(textArea, 1, 0);
        grid.add(new Label("Тип:"), 0, 1); grid.add(typeCombo, 1, 1);
        grid.add(new Label("Тема:"), 0, 2); grid.add(topicField, 1, 2);
        grid.add(new Label("Баллы:"), 0, 3); grid.add(pointsField, 1, 3);
        grid.add(new Label("Сложность (1-5):"), 0, 4); grid.add(difficultyField, 1, 4);
        grid.add(new Label("Варианты ответов:"), 0, 5); grid.add(optionsArea, 1, 5);
        dialog.getDialogPane().setContent(grid);

        // Валидация: SINGLE_CHOICE не может иметь более одного правильного ответа
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, evt -> {
            if ("SINGLE_CHOICE".equals(typeCombo.getValue())) {
                long correctCount = java.util.Arrays.stream(optionsArea.getText().split("\n"))
                        .filter(line -> !line.isBlank() && line.startsWith("*"))
                        .count();
                if (correctCount > 1) {
                    AlertUtil.showWarning("Ошибка валидации",
                            "Вопрос типа «Один из списка» (SINGLE_CHOICE) может иметь только один правильный ответ.\n" +
                            "Обозначьте правильный ответ символом * только у одного варианта.");
                    evt.consume();
                }
            }
        });

        dialog.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            QuestionDto dto = existing != null ? existing : new QuestionDto();
            dto.setText(textArea.getText().trim());
            dto.setQuestionType(typeCombo.getValue());
            dto.setTopic(topicField.getText().trim());
            try { dto.setPoints(Integer.parseInt(pointsField.getText().trim())); } catch (NumberFormatException e) { dto.setPoints(1); }
            try { dto.setDifficulty(Integer.parseInt(difficultyField.getText().trim())); } catch (NumberFormatException e) { dto.setDifficulty(1); }

            List<QuestionOptionDto> opts = new ArrayList<>();
            for (String line : optionsArea.getText().split("\n")) {
                if (line.isBlank()) continue;
                boolean correct = line.startsWith("*");
                String text = correct ? line.substring(1).trim() : line.trim();
                QuestionOptionDto opt = new QuestionOptionDto(null, text, correct);
                opts.add(opt);
            }
            dto.setOptions(opts);
            return dto;
        });

        dialog.showAndWait().ifPresent(dto -> {
            new Thread(() -> {
                Response resp = existing == null
                        ? connection.send(RequestType.CREATE_QUESTION, dto)
                        : connection.send(RequestType.UPDATE_QUESTION, dto);
                Platform.runLater(() -> {
                    if (resp.isSuccess()) { loadQuestions(); AlertUtil.showInfo("Успех", "Сохранено"); }
                    else AlertUtil.showError("Ошибка", resp.getMessage());
                });
            }).start();
        });
    }

    @FXML private void handleDelete() {
        QuestionDto selected = questionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtil.showWarning("Внимание", "Выберите вопрос"); return; }
        if (!AlertUtil.showConfirm("Удаление", "Удалить вопрос?")) return;
        new Thread(() -> {
            Response resp = connection.send(RequestType.DELETE_QUESTION, selected.getQuestionId());
            Platform.runLater(() -> { if (resp.isSuccess()) loadQuestions(); else AlertUtil.showError("Ошибка", resp.getMessage()); });
        }).start();
    }

    @FXML private void handleRefresh() { loadQuestions(); }

    @FXML private void handleBack(ActionEvent event) {
        switchScene(getStage(event), "/fxml/Main.fxml", "Главное меню", 1100, 720);
    }
}
