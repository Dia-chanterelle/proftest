package com.proftest.client.controller.hr;

import com.google.gson.reflect.TypeToken;
import com.proftest.client.controller.BaseController;
import com.proftest.client.util.AlertUtil;
import com.proftest.common.dto.*;
import com.proftest.common.protocol.RequestType;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
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
import java.util.*;
import java.util.stream.Collectors;

public class ReportsController extends BaseController implements Initializable {

    // Summary tab
    @FXML private Label totalAttemptsLabel;
    @FXML private Label passRateLabel;
    @FXML private Label uniqueUsersLabel;
    @FXML private Label activeTestsLabel;
    @FXML private Label avgScoreLabel;
    @FXML private TableView<TestAttemptDto> recentTable;
    @FXML private TableColumn<TestAttemptDto, String> rUserCol;
    @FXML private TableColumn<TestAttemptDto, String> rTestCol;
    @FXML private TableColumn<TestAttemptDto, String> rStartCol;
    @FXML private TableColumn<TestAttemptDto, String> rScoreCol;
    @FXML private TableColumn<TestAttemptDto, String> rPassedCol;
    @FXML private TableColumn<TestAttemptDto, String> rStatusCol;

    // Tests tab
    @FXML private ComboBox<SkillDto> skillFilterCombo;
    @FXML private Label testStatsStatus;
    @FXML private TableView<TestStat> testStatsTable;
    @FXML private TableColumn<TestStat, String> tsTitle;
    @FXML private TableColumn<TestStat, String> tsSkills;
    @FXML private TableColumn<TestStat, Integer> tsAttempts;
    @FXML private TableColumn<TestStat, String> tsAvgScore;
    @FXML private TableColumn<TestStat, String> tsPassRate;
    @FXML private TableColumn<TestStat, Integer> tsPassed;
    @FXML private TableColumn<TestStat, Integer> tsFailed;

    // Skills tab
    @FXML private TableView<SkillStat> skillStatsTable;
    @FXML private TableColumn<SkillStat, String> ssName;
    @FXML private TableColumn<SkillStat, String> ssCategory;
    @FXML private TableColumn<SkillStat, Integer> ssTests;
    @FXML private TableColumn<SkillStat, Integer> ssAttempts;
    @FXML private TableColumn<SkillStat, String> ssPassRate;

    // User activity tab (admin only)
    @FXML private Tab userActivityTab;
    @FXML private TabPane tabPane;
    @FXML private TableView<UserStat> userStatsTable;
    @FXML private TableColumn<UserStat, String> usName;
    @FXML private TableColumn<UserStat, String> usLogin;
    @FXML private TableColumn<UserStat, Integer> usAttempts;
    @FXML private TableColumn<UserStat, Integer> usPassed;
    @FXML private TableColumn<UserStat, Integer> usFailed;
    @FXML private TableColumn<UserStat, String> usAvgScore;

    // Detailed tab
    @FXML private ComboBox<TestDto> testFilterCombo;
    @FXML private Label statusLabel;
    @FXML private Label summaryLabel;
    @FXML private TableView<TestAttemptDto> table;
    @FXML private TableColumn<TestAttemptDto, Long> idCol;
    @FXML private TableColumn<TestAttemptDto, String> userCol;
    @FXML private TableColumn<TestAttemptDto, String> testCol;
    @FXML private TableColumn<TestAttemptDto, String> startTimeCol;
    @FXML private TableColumn<TestAttemptDto, String> scoreCol;
    @FXML private TableColumn<TestAttemptDto, String> statusCol;
    @FXML private TableColumn<TestAttemptDto, String> passedCol;

    private final ObservableList<TestAttemptDto> allResults = FXCollections.observableArrayList();
    private final ObservableList<TestStat> allTestStats = FXCollections.observableArrayList();
    private final ObservableList<SkillStat> allSkillStats = FXCollections.observableArrayList();
    private final ObservableList<UserStat> allUserStats = FXCollections.observableArrayList();
    private List<TestDto> allTests = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!isAdmin() && tabPane != null && userActivityTab != null) {
            tabPane.getTabs().remove(userActivityTab);
        }
        setupSummaryTable();
        setupTestStatsTable();
        setupSkillStatsTable();
        setupUserStatsTable();
        setupDetailedTable();
        loadData();
    }

    // ─── Setup ────────────────────────────────────────────────────────────────

    private void setupSummaryTable() {
        rUserCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getUserFullName() != null ? d.getValue().getUserFullName() : d.getValue().getUserLogin()));
        rTestCol.setCellValueFactory(new PropertyValueFactory<>("testTitle"));
        rStartCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        rScoreCol.setCellValueFactory(d -> {
            Double s = d.getValue().getScore();
            return new SimpleStringProperty(s != null ? String.format("%.1f%%", s) : "—");
        });
        rPassedCol.setCellValueFactory(d -> {
            Boolean p = d.getValue().getPassed();
            return new SimpleStringProperty(p == null ? "—" : (p ? "✓ Пройден" : "✗ Не пройден"));
        });
        rStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        recentTable.setItems(FXCollections.observableArrayList());
    }

    private void setupTestStatsTable() {
        tsTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().testTitle));
        tsSkills.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().skills));
        tsAttempts.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().totalAttempts).asObject());
        tsAvgScore.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().totalAttempts > 0 ? String.format("%.1f%%", d.getValue().totalScore / d.getValue().totalAttempts) : "—"));
        tsPassRate.setCellValueFactory(d -> {
            int total = d.getValue().passed + d.getValue().failed;
            return new SimpleStringProperty(total > 0 ? String.format("%.0f%%", 100.0 * d.getValue().passed / total) : "—");
        });
        tsPassed.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().passed).asObject());
        tsFailed.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().failed).asObject());
        testStatsTable.setItems(allTestStats);
    }

    private void setupSkillStatsTable() {
        ssName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().skillName));
        ssCategory.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().category));
        ssTests.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().testCount).asObject());
        ssAttempts.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().totalAttempts).asObject());
        ssPassRate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().totalAttempts > 0 ? String.format("%.0f%%", d.getValue().passedAttempts * 100.0 / d.getValue().totalAttempts) : "—"));
        skillStatsTable.setItems(allSkillStats);
    }

    private void setupUserStatsTable() {
        if (userStatsTable == null) return;
        usName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().fullName));
        usLogin.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().login));
        usAttempts.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().totalAttempts).asObject());
        usPassed.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().passed).asObject());
        usFailed.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().failed).asObject());
        usAvgScore.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().totalAttempts > 0 ? String.format("%.1f%%", d.getValue().totalScore / d.getValue().totalAttempts) : "—"));
        userStatsTable.setItems(allUserStats);
    }

    private void setupDetailedTable() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("attemptId"));
        userCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getUserFullName() != null ? d.getValue().getUserFullName() : d.getValue().getUserLogin()));
        testCol.setCellValueFactory(new PropertyValueFactory<>("testTitle"));
        startTimeCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        scoreCol.setCellValueFactory(d -> {
            Double s = d.getValue().getScore();
            return new SimpleStringProperty(s != null ? String.format("%.1f%%", s) : "—");
        });
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        passedCol.setCellValueFactory(d -> {
            Boolean p = d.getValue().getPassed();
            return new SimpleStringProperty(p == null ? "—" : (p ? "✓" : "✗"));
        });
        table.setItems(allResults);
    }

    // ─── Loading & computation ────────────────────────────────────────────────

    private void loadData() {
        if (statusLabel != null) statusLabel.setText("Загрузка...");
        new Thread(() -> {
            try {
                Response rResp = connection.send(RequestType.GET_ALL_RESULTS, (String) null);
                Response tResp = connection.send(RequestType.GET_ALL_TESTS, (String) null);
                Response sResp = connection.send(RequestType.GET_ALL_SKILLS, (String) null);
                Platform.runLater(() -> {
                    List<TestAttemptDto> results = new ArrayList<>();
                    List<SkillDto> skills = new ArrayList<>();

                    if (rResp.isSuccess()) {
                        Type rt = new TypeToken<List<TestAttemptDto>>(){}.getType();
                        results = connection.getGson().fromJson(rResp.getPayload(), rt);
                        allResults.setAll(results);
                        if (statusLabel != null) statusLabel.setText("Попыток: " + results.size());
                    }
                    if (tResp.isSuccess()) {
                        Type tt = new TypeToken<List<TestDto>>(){}.getType();
                        allTests = connection.getGson().fromJson(tResp.getPayload(), tt);
                        populateTestFilter();
                    }
                    if (sResp.isSuccess()) {
                        Type st = new TypeToken<List<SkillDto>>(){}.getType();
                        skills = connection.getGson().fromJson(sResp.getPayload(), st);
                        populateSkillFilter(skills);
                    }

                    computeAndShowStats(results, skills);
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.showError("Ошибка", e.getMessage()));
            }
        }).start();
    }

    private void computeAndShowStats(List<TestAttemptDto> results, List<SkillDto> allSkillsList) {
        // ── Summary ──────────────────────────────────────────────────────────
        int total = results.size();
        long passedCount = results.stream().filter(r -> Boolean.TRUE.equals(r.getPassed())).count();
        long failedCount = results.stream().filter(r -> Boolean.FALSE.equals(r.getPassed())).count();
        long uniqueUsers = results.stream().map(TestAttemptDto::getUserId).filter(Objects::nonNull).distinct().count();
        long activeTests = allTests.stream().filter(TestDto::isActive).count();
        OptionalDouble avgScore = results.stream()
                .filter(r -> r.getScore() != null)
                .mapToDouble(TestAttemptDto::getScore)
                .average();

        totalAttemptsLabel.setText(String.valueOf(total));
        passRateLabel.setText(total > 0 ? String.format("%.0f%%", 100.0 * passedCount / (passedCount + failedCount > 0 ? passedCount + failedCount : 1)) : "—");
        uniqueUsersLabel.setText(String.valueOf(uniqueUsers));
        activeTestsLabel.setText(String.valueOf(activeTests));
        avgScoreLabel.setText(avgScore.isPresent() ? String.format("%.1f%%", avgScore.getAsDouble()) : "—");
        if (summaryLabel != null) summaryLabel.setText("Прошли: " + passedCount + " / " + total);

        List<TestAttemptDto> recent = results.stream()
                .sorted(Comparator.comparing(r -> r.getStartTime() != null ? r.getStartTime() : "", Comparator.reverseOrder()))
                .limit(10)
                .collect(Collectors.toList());
        recentTable.setItems(FXCollections.observableArrayList(recent));

        // ── Per-test stats ───────────────────────────────────────────────────
        Map<String, String> titleToSkills = new HashMap<>();
        for (TestDto t : allTests) {
            if (t.getSkills() != null && !t.getSkills().isEmpty()) {
                titleToSkills.put(t.getTitle(),
                        t.getSkills().stream().map(SkillDto::getSkillName).collect(Collectors.joining(", ")));
            }
        }

        Map<String, TestStat> testStatMap = new LinkedHashMap<>();
        for (TestAttemptDto r : results) {
            TestStat stat = testStatMap.computeIfAbsent(r.getTestTitle(), TestStat::new);
            stat.totalAttempts++;
            stat.skills = titleToSkills.getOrDefault(r.getTestTitle(), "—");
            if (r.getScore() != null) stat.totalScore += r.getScore();
            if (Boolean.TRUE.equals(r.getPassed())) stat.passed++;
            else if (Boolean.FALSE.equals(r.getPassed())) stat.failed++;
        }
        List<TestStat> testStats = testStatMap.values().stream()
                .sorted(Comparator.comparingInt((TestStat s) -> s.totalAttempts).reversed())
                .collect(Collectors.toList());
        allTestStats.setAll(testStats);
        if (testStatsStatus != null) testStatsStatus.setText("Тестов: " + testStats.size());

        // ── Per-skill stats ──────────────────────────────────────────────────
        Map<Long, SkillStat> skillStatMap = new LinkedHashMap<>();
        for (SkillDto skill : allSkillsList) {
            skillStatMap.put(skill.getSkillId(), new SkillStat(skill.getSkillName(), skill.getCategory()));
        }
        for (TestDto t : allTests) {
            if (t.getSkills() == null || t.getSkills().isEmpty()) continue;
            // Count attempts for this test
            int testAttempts = (int) results.stream().filter(r -> r.getTestTitle() != null && r.getTestTitle().equals(t.getTitle())).count();
            int testPassed = (int) results.stream().filter(r -> r.getTestTitle() != null && r.getTestTitle().equals(t.getTitle()) && Boolean.TRUE.equals(r.getPassed())).count();
            for (SkillDto skill : t.getSkills()) {
                SkillStat ss = skillStatMap.get(skill.getSkillId());
                if (ss != null) {
                    ss.testCount++;
                    ss.totalAttempts += testAttempts;
                    ss.passedAttempts += testPassed;
                }
            }
        }
        List<SkillStat> skillStats = skillStatMap.values().stream()
                .filter(ss -> ss.testCount > 0)
                .sorted(Comparator.comparingInt((SkillStat ss) -> ss.totalAttempts).reversed())
                .collect(Collectors.toList());
        allSkillStats.setAll(skillStats);

        // ── User activity (admin only) ────────────────────────────────────────
        if (isAdmin() && userStatsTable != null) {
            Map<Long, UserStat> userStatMap = new LinkedHashMap<>();
            for (TestAttemptDto r : results) {
                if (r.getUserId() == null) continue;
                UserStat us = userStatMap.computeIfAbsent(r.getUserId(), id -> {
                    String name = r.getUserFullName() != null ? r.getUserFullName() : r.getUserLogin();
                    return new UserStat(name, r.getUserLogin() != null ? r.getUserLogin() : "");
                });
                us.totalAttempts++;
                if (Boolean.TRUE.equals(r.getPassed())) us.passed++;
                else if (Boolean.FALSE.equals(r.getPassed())) us.failed++;
                if (r.getScore() != null) us.totalScore += r.getScore();
            }
            List<UserStat> userStats = userStatMap.values().stream()
                    .sorted(Comparator.comparingInt((UserStat us) -> us.totalAttempts).reversed())
                    .collect(Collectors.toList());
            allUserStats.setAll(userStats);
        }
    }

    private void populateTestFilter() {
        if (testFilterCombo == null) return;
        TestDto all = new TestDto(); all.setTitle("— Все тесты —");
        List<TestDto> items = new ArrayList<>();
        items.add(all);
        items.addAll(allTests);
        testFilterCombo.setItems(FXCollections.observableArrayList(items));
        testFilterCombo.setValue(items.get(0));
        testFilterCombo.setConverter(new javafx.util.StringConverter<>() {
            public String toString(TestDto t) { return t == null ? "" : t.getTitle(); }
            public TestDto fromString(String s) { return null; }
        });
    }

    private void populateSkillFilter(List<SkillDto> skills) {
        if (skillFilterCombo == null) return;
        SkillDto all = new SkillDto(); all.setSkillName("— Все навыки —");
        List<SkillDto> items = new ArrayList<>();
        items.add(all);
        items.addAll(skills);
        skillFilterCombo.setItems(FXCollections.observableArrayList(items));
        skillFilterCombo.setValue(items.get(0));
        skillFilterCombo.setConverter(new javafx.util.StringConverter<>() {
            public String toString(SkillDto s) { return s == null ? "" : s.getSkillName(); }
            public SkillDto fromString(String s) { return null; }
        });
    }

    // ─── Filters ──────────────────────────────────────────────────────────────

    @FXML private void handleFilterByTest() {
        TestDto selected = testFilterCombo != null ? testFilterCombo.getValue() : null;
        if (selected == null || selected.getTestId() == null) {
            table.setItems(allResults);
            if (statusLabel != null) statusLabel.setText("Попыток: " + allResults.size());
            return;
        }
        ObservableList<TestAttemptDto> filtered = allResults.filtered(r ->
                selected.getTestId().equals(r.getTestId()));
        table.setItems(filtered);
        if (statusLabel != null) statusLabel.setText("Попыток: " + filtered.size());
    }

    @FXML private void handleClearTestFilter() {
        if (testFilterCombo != null && !testFilterCombo.getItems().isEmpty()) {
            testFilterCombo.setValue(testFilterCombo.getItems().get(0));
        }
        table.setItems(allResults);
        if (statusLabel != null) statusLabel.setText("Попыток: " + allResults.size());
    }

    @FXML private void handleSkillFilter() {
        SkillDto selected = skillFilterCombo != null ? skillFilterCombo.getValue() : null;
        if (selected == null || selected.getSkillId() == null) {
            testStatsTable.setItems(allTestStats);
            return;
        }
        // Find test titles that have this skill
        Set<String> matchingTitles = allTests.stream()
                .filter(t -> t.getSkills() != null && t.getSkills().stream()
                        .anyMatch(s -> selected.getSkillId().equals(s.getSkillId())))
                .map(TestDto::getTitle)
                .collect(Collectors.toSet());
        ObservableList<TestStat> filtered = allTestStats.filtered(ts -> matchingTitles.contains(ts.testTitle));
        testStatsTable.setItems(filtered);
        if (testStatsStatus != null) testStatsStatus.setText("Тестов: " + filtered.size() + " (фильтр: " + selected.getSkillName() + ")");
    }

    @FXML private void handleClearSkillFilter() {
        if (skillFilterCombo != null && !skillFilterCombo.getItems().isEmpty()) {
            skillFilterCombo.setValue(skillFilterCombo.getItems().get(0));
        }
        testStatsTable.setItems(allTestStats);
        if (testStatsStatus != null) testStatsStatus.setText("Тестов: " + allTestStats.size());
    }

    @FXML private void handleRefresh() { loadData(); }

    @FXML private void handleBack(ActionEvent event) {
        switchScene(getStage(event), "/fxml/Main.fxml", "Главное меню", 1100, 720);
    }

    // ─── Inner stat classes ───────────────────────────────────────────────────

    static class TestStat {
        String testTitle;
        String skills = "—";
        int totalAttempts = 0;
        double totalScore = 0;
        int passed = 0;
        int failed = 0;
        TestStat(String title) { this.testTitle = title; }
    }

    static class SkillStat {
        String skillName;
        String category;
        int testCount = 0;
        int totalAttempts = 0;
        int passedAttempts = 0;
        SkillStat(String name, String cat) { this.skillName = name; this.category = cat; }
    }

    static class UserStat {
        String fullName;
        String login;
        int totalAttempts = 0;
        int passed = 0;
        int failed = 0;
        double totalScore = 0;
        UserStat(String name, String login) { this.fullName = name; this.login = login; }
    }
}
