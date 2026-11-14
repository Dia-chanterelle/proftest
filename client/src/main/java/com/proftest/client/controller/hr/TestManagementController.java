package com.proftest.client.controller.hr;

import com.google.gson.reflect.TypeToken;
import com.proftest.client.controller.BaseController;
import com.proftest.client.util.AlertUtil;
import com.proftest.common.dto.Response;
import com.proftest.common.dto.SkillDto;
import com.proftest.common.dto.TestDto;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class TestManagementController extends BaseController implements Initializable {

    @FXML private TableView<TestDto> testsTable;
    @FXML private TableColumn<TestDto, Long> idCol;
    @FXML private TableColumn<TestDto, String> titleCol;
    @FXML private TableColumn<TestDto, Integer> timeLimitCol;
    @FXML private TableColumn<TestDto, Integer> passingScoreCol;
    @FXML private TableColumn<TestDto, Boolean> activeCol;
    @FXML private TableColumn<TestDto, String> creatorCol;
    @FXML private TableColumn<TestDto, String> skillsCol;
    @FXML private Label statusLabel;
    @FXML private TextField searchField;

    private final ObservableList<TestDto> allTests = FXCollections.observableArrayList();
    private List<SkillDto> allSkills = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        idCol.setCellValueFactory(new PropertyValueFactory<>("testId"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        timeLimitCol.setCellValueFactory(new PropertyValueFactory<>("timeLimitMinutes"));
        passingScoreCol.setCellValueFactory(new PropertyValueFactory<>("passingScore"));
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        creatorCol.setCellValueFactory(new PropertyValueFactory<>("creatorName"));
        if (skillsCol != null) {
            skillsCol.setCellValueFactory(data -> {
                List<SkillDto> skills = data.getValue().getSkills();
                if (skills == null || skills.isEmpty()) return new SimpleStringProperty("—");
                return new SimpleStringProperty(skills.stream()
                        .map(SkillDto::getSkillName).collect(Collectors.joining(", ")));
            });
        }
        if (searchField != null) {
            searchField.textProperty().addListener((obs, old, val) -> filterTests(val));
        }
        loadData();
    }

    private void loadData() {
        statusLabel.setText("Загрузка...");
        new Thread(() -> {
            try {
                Response tResp = connection.send(RequestType.GET_ALL_TESTS, (String) null);
                Response sResp = connection.send(RequestType.GET_ALL_SKILLS, (String) null);
                Platform.runLater(() -> {
                    if (tResp.isSuccess()) {
                        Type type = new TypeToken<List<TestDto>>(){}.getType();
                        List<TestDto> tests = connection.getGson().fromJson(tResp.getPayload(), type);
                        allTests.setAll(tests);
                        testsTable.setItems(allTests);
                        statusLabel.setText("Тестов: " + tests.size());
                    } else AlertUtil.showError("Ошибка", tResp.getMessage());
                    if (sResp.isSuccess()) {
                        Type st = new TypeToken<List<SkillDto>>(){}.getType();
                        allSkills = connection.getGson().fromJson(sResp.getPayload(), st);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.showError("Ошибка", e.getMessage()));
            }
        }).start();
    }

    private void filterTests(String text) {
        if (text == null || text.isEmpty()) { testsTable.setItems(allTests); return; }
        String lower = text.toLowerCase();
        testsTable.setItems(allTests.filtered(t -> t.getTitle().toLowerCase().contains(lower)));
    }

    @FXML private void handleCreateTest(ActionEvent event) { openTestEditor(null); }

    @FXML private void handleEditTest(ActionEvent event) {
        TestDto selected = testsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtil.showWarning("Внимание", "Выберите тест"); return; }
        openTestEditor(selected);
    }

    private void openTestEditor(TestDto existing) {
        Dialog<TestDto> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Создать тест" : "Редактировать тест");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField titleField = new TextField(existing != null ? existing.getTitle() : "");
        TextField descField = new TextField(existing != null && existing.getDescription() != null ? existing.getDescription() : "");
        TextField timeField = new TextField(existing != null ? String.valueOf(existing.getTimeLimitMinutes()) : "60");
        TextField scoreField = new TextField(existing != null ? String.valueOf(existing.getPassingScore()) : "70");
        TextField attemptsField = new TextField(existing != null ? String.valueOf(existing.getMaxAttempts()) : "1");

        // Skill checkboxes
        Set<Long> currentSkillIds = new HashSet<>();
        if (existing != null && existing.getSkills() != null) {
            existing.getSkills().forEach(s -> currentSkillIds.add(s.getSkillId()));
        }
        Map<Long, CheckBox> skillCheckMap = new LinkedHashMap<>();
        VBox skillsBox = new VBox(4);
        for (SkillDto skill : allSkills) {
            CheckBox cb = new CheckBox(skill.getSkillName() + " (" + skill.getCategory() + ")");
            cb.setSelected(currentSkillIds.contains(skill.getSkillId()));
            skillsBox.getChildren().add(cb);
            skillCheckMap.put(skill.getSkillId(), cb);
        }
        if (allSkills.isEmpty()) {
            skillsBox.getChildren().add(new Label("— Нет навыков. Создайте их в разделе «Навыки» —"));
        }
        ScrollPane skillsScroll = new ScrollPane(skillsBox);
        skillsScroll.setPrefHeight(110);
        skillsScroll.setFitToWidth(true);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);
        grid.setPrefWidth(420);
        grid.add(new Label("Название:"), 0, 0);          grid.add(titleField, 1, 0);
        grid.add(new Label("Описание:"), 0, 1);          grid.add(descField, 1, 1);
        grid.add(new Label("Время (мин):"), 0, 2);       grid.add(timeField, 1, 2);
        grid.add(new Label("Проходной балл (%):"), 0, 3); grid.add(scoreField, 1, 3);
        grid.add(new Label("Попыток:"), 0, 4);           grid.add(attemptsField, 1, 4);
        grid.add(new Label("Навыки:"), 0, 5);            grid.add(skillsScroll, 1, 5);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(500);

        dialog.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            TestDto dto = existing != null ? existing : new TestDto();
            dto.setTitle(titleField.getText().trim());
            dto.setDescription(descField.getText().trim());
            try {
                dto.setTimeLimitMinutes(Integer.parseInt(timeField.getText().trim()));
                dto.setPassingScore(Integer.parseInt(scoreField.getText().trim()));
                dto.setMaxAttempts(Integer.parseInt(attemptsField.getText().trim()));
            } catch (NumberFormatException e) { return null; }
            dto.setActive(true);
            // Collect selected skills
            List<SkillDto> selectedSkills = new ArrayList<>();
            for (Map.Entry<Long, CheckBox> entry : skillCheckMap.entrySet()) {
                if (entry.getValue().isSelected()) {
                    SkillDto s = new SkillDto();
                    s.setSkillId(entry.getKey());
                    selectedSkills.add(s);
                }
            }
            dto.setSkills(selectedSkills);
            return dto;
        });

        dialog.showAndWait().ifPresent(dto -> {
            List<SkillDto> selectedSkills = dto.getSkills();
            new Thread(() -> {
                Response resp = existing == null
                        ? connection.send(RequestType.CREATE_TEST, dto)
                        : connection.send(RequestType.UPDATE_TEST, dto);
                Platform.runLater(() -> {
                    if (resp.isSuccess()) {
                        // After save, update skills
                        TestDto saved = connection.getGson().fromJson(resp.getPayload(), TestDto.class);
                        if (saved != null && saved.getTestId() != null) {
                            TestDto skillPayload = new TestDto();
                            skillPayload.setTestId(saved.getTestId());
                            skillPayload.setSkills(selectedSkills);
                            new Thread(() -> {
                                connection.send(RequestType.SET_TEST_SKILLS, skillPayload);
                                Platform.runLater(() -> { loadData(); AlertUtil.showInfo("Успех", "Тест сохранён"); });
                            }).start();
                        } else {
                            loadData();
                            AlertUtil.showInfo("Успех", "Тест сохранён");
                        }
                    } else AlertUtil.showError("Ошибка", resp.getMessage());
                });
            }).start();
        });
    }

    @FXML private void handleToggleActive() {
        TestDto selected = testsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtil.showWarning("Внимание", "Выберите тест"); return; }
        new Thread(() -> {
            Response response = connection.send(RequestType.TOGGLE_TEST_ACTIVE, selected.getTestId());
            Platform.runLater(() -> { if (response.isSuccess()) loadData(); else AlertUtil.showError("Ошибка", response.getMessage()); });
        }).start();
    }

    @FXML private void handleDeleteTest() {
        TestDto selected = testsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtil.showWarning("Внимание", "Выберите тест"); return; }
        if (!AlertUtil.showConfirm("Удаление", "Удалить тест \"" + selected.getTitle() + "\"?")) return;
        new Thread(() -> {
            Response response = connection.send(RequestType.DELETE_TEST, selected.getTestId());
            Platform.runLater(() -> { if (response.isSuccess()) loadData(); else AlertUtil.showError("Ошибка", response.getMessage()); });
        }).start();
    }

    @FXML private void handleManageQuestions(ActionEvent event) {
        TestDto selected = testsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtil.showWarning("Внимание", "Выберите тест"); return; }
        TestQuestionsController ctrl = openWindow("/fxml/hr/TestQuestions.fxml", "Вопросы теста: " + selected.getTitle(), 900, 650);
        if (ctrl != null) ctrl.setTest(selected);
    }

    @FXML private void handleRefresh() { loadData(); }

    @FXML private void handleBack(ActionEvent event) {
        switchScene(getStage(event), "/fxml/Main.fxml", "Главное меню", 1100, 720);
    }
}
