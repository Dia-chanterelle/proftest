package com.proftest.client.controller.hr;

import com.google.gson.reflect.TypeToken;
import com.proftest.client.controller.BaseController;
import com.proftest.client.util.AlertUtil;
import com.proftest.common.dto.Response;
import com.proftest.common.dto.SkillDto;
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
import java.util.Optional;
import java.util.ResourceBundle;

public class SkillManagementController extends BaseController implements Initializable {

    @FXML private TableView<SkillDto> table;
    @FXML private TableColumn<SkillDto, Long> idCol;
    @FXML private TableColumn<SkillDto, String> nameCol;
    @FXML private TableColumn<SkillDto, String> categoryCol;
    @FXML private TableColumn<SkillDto, Integer> difficultyCol;
    @FXML private Label statusLabel;
    @FXML private TextField searchField;

    private ObservableList<SkillDto> skills = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        idCol.setCellValueFactory(new PropertyValueFactory<>("skillId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("skillName"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        difficultyCol.setCellValueFactory(new PropertyValueFactory<>("difficultyLevel"));
        table.setItems(skills);
        if (searchField != null)
            searchField.textProperty().addListener((obs, o, val) ->
                table.setItems(skills.filtered(s -> val.isEmpty() || s.getSkillName().toLowerCase().contains(val.toLowerCase()))));
        loadSkills();
    }

    private void loadSkills() {
        new Thread(() -> {
            Response resp = connection.send(RequestType.GET_ALL_SKILLS, (String) null);
            Platform.runLater(() -> {
                if (resp.isSuccess()) {
                    Type type = new TypeToken<List<SkillDto>>(){}.getType();
                    List<SkillDto> loaded = connection.getGson().fromJson(resp.getPayload(), type);
                    skills.setAll((java.util.Collection<SkillDto>) loaded);
                    statusLabel.setText("Навыков: " + skills.size());
                }
            });
        }).start();
    }

    @FXML private void handleCreate() { openEditor(null); }
    @FXML private void handleEdit() {
        SkillDto s = table.getSelectionModel().getSelectedItem();
        if (s == null) { AlertUtil.showWarning("Внимание", "Выберите навык"); return; }
        openEditor(s);
    }

    private void openEditor(SkillDto existing) {
        Dialog<SkillDto> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Добавить навык" : "Редактировать навык");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField(existing != null ? existing.getSkillName() : "");
        TextField categoryField = new TextField(existing != null ? existing.getCategory() : "");
        TextArea descField = new TextArea(existing != null && existing.getDescription() != null ? existing.getDescription() : "");
        descField.setPrefRowCount(2);
        TextField diffField = new TextField(existing != null && existing.getDifficultyLevel() != null ? String.valueOf(existing.getDifficultyLevel()) : "1");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(8);
        grid.add(new Label("Название:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Категория:"), 0, 1); grid.add(categoryField, 1, 1);
        grid.add(new Label("Описание:"), 0, 2); grid.add(descField, 1, 2);
        grid.add(new Label("Сложность (1-5):"), 0, 3); grid.add(diffField, 1, 3);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            SkillDto dto = existing != null ? existing : new SkillDto();
            dto.setSkillName(nameField.getText().trim());
            dto.setCategory(categoryField.getText().trim());
            dto.setDescription(descField.getText().trim());
            try { dto.setDifficultyLevel(Integer.parseInt(diffField.getText().trim())); } catch (NumberFormatException e) { dto.setDifficultyLevel(1); }
            return dto;
        });

        dialog.showAndWait().ifPresent(dto -> {
            new Thread(() -> {
                Response resp = existing == null
                        ? connection.send(RequestType.CREATE_SKILL, dto)
                        : connection.send(RequestType.UPDATE_SKILL, dto);
                Platform.runLater(() -> { if (resp.isSuccess()) loadSkills(); else AlertUtil.showError("Ошибка", resp.getMessage()); });
            }).start();
        });
    }

    @FXML private void handleDelete() {
        SkillDto selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { AlertUtil.showWarning("Внимание", "Выберите навык"); return; }
        if (!AlertUtil.showConfirm("Удаление", "Удалить навык \"" + selected.getSkillName() + "\"?")) return;
        new Thread(() -> {
            Response resp = connection.send(RequestType.DELETE_SKILL, selected.getSkillId());
            Platform.runLater(() -> { if (resp.isSuccess()) loadSkills(); else AlertUtil.showError("Ошибка", resp.getMessage()); });
        }).start();
    }

    @FXML private void handleRefresh() { loadSkills(); }

    @FXML private void handleBack(ActionEvent event) {
        switchScene(getStage(event), "/fxml/Main.fxml", "Главное меню", 1100, 720);
    }
}
