package com.proftest.client.controller.candidate;

import com.proftest.client.controller.BaseController;
import com.proftest.client.util.AlertUtil;
import com.proftest.common.dto.*;
import com.proftest.common.protocol.RequestType;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;

public class TakeTestController extends BaseController implements Initializable {

    @FXML private Label testTitleLabel;
    @FXML private Label questionCountLabel;
    @FXML private Label timerLabel;
    @FXML private Label questionTextLabel;
    @FXML private VBox optionsBox;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button finishButton;
    @FXML private ProgressBar progressBar;
    @FXML private TextArea openAnswerArea;

    private TestAttemptDto attempt;
    private List<QuestionDto> questions = new ArrayList<>();
    private int currentIndex = 0;
    private Map<Long, Long> selectedOptions = new HashMap<>();
    private Map<Long, List<Long>> selectedMultipleOptions = new HashMap<>();
    private Map<Long, String> textAnswers = new HashMap<>();
    private Timeline timer;
    private int remainingSeconds;
    private ToggleGroup currentToggleGroup;

    /** true когда таймер истёк — блокирует ручное завершение и ввод */
    private volatile boolean timerExpired = false;

    /** Callback, вызываемый при закрытии окна (для обновления списка тестов) */
    private Runnable onCloseCallback;

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    public void startTest(Long assignmentId) {
        new Thread(() -> {
            try {
                Response resp = connection.send(RequestType.START_ATTEMPT, assignmentId);
                Platform.runLater(() -> {
                    if (resp.isSuccess()) {
                        attempt = connection.getGson().fromJson(resp.getPayload(), TestAttemptDto.class);
                        questions = attempt.getQuestions() != null ? attempt.getQuestions() : new ArrayList<>();
                        testTitleLabel.setText(attempt.getTestTitle());
                        int timeLimit = attempt.getTimeLimitMinutes() != null ? attempt.getTimeLimitMinutes() : 60;
                        startTimer(timeLimit);
                        showQuestion(0);
                    } else {
                        AlertUtil.showError("Ошибка", resp.getMessage());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.showError("Ошибка", e.getMessage()));
            }
        }).start();
    }

    private void startTimer(int timeLimitMinutes) {
        remainingSeconds = timeLimitMinutes * 60;
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingSeconds--;
            int min = remainingSeconds / 60;
            int sec = remainingSeconds % 60;
            timerLabel.setText(String.format("Время: %02d:%02d", min, sec));

            if (remainingSeconds <= 60) {
                timerLabel.setStyle("-fx-text-fill: #e53935; -fx-font-weight: bold;");
            }

            if (remainingSeconds <= 0) {
                timer.stop();
                timerExpired = true;
                timerLabel.setText("Время: 00:00");
                timerLabel.setStyle("-fx-text-fill: #e53935; -fx-font-weight: bold;");
                lockAllControls();
                // Defer dialog and server call out of animation callback
                Platform.runLater(this::onTimerExpired);
            }
        }));
        timer.setCycleCount(Animation.INDEFINITE);
        timer.play();
    }

    /**
     * Вызывается когда время истекло (через Platform.runLater, вне анимации).
     */
    private void onTimerExpired() {
        AlertUtil.showWarning("Время истекло",
                "Отведённое время на прохождение теста закончилось.\n" +
                "Тест считается не пройденным.");

        new Thread(() -> {
            try {
                Response finishResp = connection.send(RequestType.FINISH_ATTEMPT, attempt.getAttemptId());
                Platform.runLater(() -> {
                    if (finishResp.isSuccess()) {
                        TestAttemptDto result = connection.getGson().fromJson(finishResp.getPayload(), TestAttemptDto.class);
                        showExpiredResult(result);
                    } else {
                        closeWindow();
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(this::closeWindow);
            }
        }).start();
    }

    private void lockAllControls() {
        finishButton.setDisable(true);
        prevButton.setDisable(true);
        nextButton.setDisable(true);
        optionsBox.setDisable(true);
        openAnswerArea.setDisable(true);
    }

    private void showExpiredResult(TestAttemptDto result) {
        double score = result.getScore() != null ? result.getScore() : 0.0;
        AlertUtil.showWarning("Тест не пройден",
                "Время истекло!\n\n" +
                "Результат: НЕ ПРОЙДЕН ✗\n" +
                "Ваш балл: " + String.format("%.1f", score) + "%\n" +
                "Проходной балл: " + result.getPassingScore() + "%");
        closeWindow();
    }

    private void closeWindow() {
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
        if (optionsBox.getScene() != null && optionsBox.getScene().getWindow() != null) {
            optionsBox.getScene().getWindow().hide();
        }
    }

    private void showQuestion(int index) {
        if (questions.isEmpty()) return;
        currentIndex = index;
        QuestionDto q = questions.get(index);
        questionTextLabel.setText((index + 1) + ". " + q.getText());
        questionCountLabel.setText("Вопрос " + (index + 1) + " из " + questions.size());
        progressBar.setProgress((double)(index + 1) / questions.size());

        optionsBox.getChildren().clear();
        boolean isOpen = "OPEN".equals(q.getQuestionType());
        boolean isMultiple = "MULTIPLE_CHOICE".equals(q.getQuestionType());
        openAnswerArea.setVisible(isOpen);
        openAnswerArea.setManaged(isOpen);

        if (isOpen) {
            openAnswerArea.setText(textAnswers.getOrDefault(q.getQuestionId(), ""));
        } else if (isMultiple) {
            // Multiple choice — CheckBox, можно выбрать несколько
            List<Long> selected = selectedMultipleOptions.getOrDefault(q.getQuestionId(), new ArrayList<>());
            if (q.getOptions() != null) {
                for (QuestionOptionDto opt : q.getOptions()) {
                    CheckBox cb = new CheckBox(opt.getOptionText());
                    cb.setWrapText(true);
                    cb.setUserData(opt.getOptionId());
                    cb.setSelected(selected.contains(opt.getOptionId()));
                    optionsBox.getChildren().add(cb);
                }
            }
            currentToggleGroup = null;
        } else {
            // Single choice — RadioButton, только один вариант
            currentToggleGroup = new ToggleGroup();
            if (q.getOptions() != null) {
                for (QuestionOptionDto opt : q.getOptions()) {
                    RadioButton rb = new RadioButton(opt.getOptionText());
                    rb.setToggleGroup(currentToggleGroup);
                    rb.setWrapText(true);
                    rb.setUserData(opt.getOptionId());
                    if (opt.getOptionId().equals(selectedOptions.get(q.getQuestionId()))) {
                        rb.setSelected(true);
                    }
                    optionsBox.getChildren().add(rb);
                }
            }
        }

        prevButton.setDisable(index == 0);
        nextButton.setDisable(index == questions.size() - 1);
    }

    private void saveCurrentAnswer() {
        if (questions.isEmpty() || timerExpired) return;
        QuestionDto q = questions.get(currentIndex);
        if ("OPEN".equals(q.getQuestionType())) {
            textAnswers.put(q.getQuestionId(), openAnswerArea.getText());
        } else if ("MULTIPLE_CHOICE".equals(q.getQuestionType())) {
            List<Long> checked = new ArrayList<>();
            for (javafx.scene.Node node : optionsBox.getChildren()) {
                if (node instanceof CheckBox cb && cb.isSelected()) {
                    checked.add((Long) cb.getUserData());
                }
            }
            selectedMultipleOptions.put(q.getQuestionId(), checked);
        } else if (currentToggleGroup != null && currentToggleGroup.getSelectedToggle() != null) {
            selectedOptions.put(q.getQuestionId(), (Long) currentToggleGroup.getSelectedToggle().getUserData());
        }
    }

    @FXML private void handlePrev() {
        if (timerExpired) return;
        saveCurrentAnswer();
        if (currentIndex > 0) showQuestion(currentIndex - 1);
    }

    @FXML private void handleNext() {
        if (timerExpired) return;
        saveCurrentAnswer();
        if (currentIndex < questions.size() - 1) showQuestion(currentIndex + 1);
    }

    @FXML private void handleFinish() {
        if (timerExpired) return;
        if (!AlertUtil.showConfirm("Завершить", "Вы уверены, что хотите завершить тест?")) return;
        submitAndFinish();
    }

    private void submitAndFinish() {
        if (attempt == null) {
            AlertUtil.showError("Ошибка", "Тест не был запущен. Закройте окно и попробуйте снова.");
            return;
        }
        saveCurrentAnswer();
        if (timer != null) timer.stop();
        finishButton.setDisable(true);

        new Thread(() -> {
            try {
                for (QuestionDto q : questions) {
                    if ("MULTIPLE_CHOICE".equals(q.getQuestionType())) {
                        List<Long> chosen = selectedMultipleOptions.getOrDefault(q.getQuestionId(), new ArrayList<>());
                        if (chosen.isEmpty()) {
                            // Отправляем пустой ответ, чтобы зафиксировать вопрос
                            UserAnswerDto ans = new UserAnswerDto();
                            ans.setAttemptId(attempt.getAttemptId());
                            ans.setQuestionId(q.getQuestionId());
                            connection.send(RequestType.SUBMIT_ANSWER, ans);
                        } else {
                            for (Long optId : chosen) {
                                UserAnswerDto ans = new UserAnswerDto();
                                ans.setAttemptId(attempt.getAttemptId());
                                ans.setQuestionId(q.getQuestionId());
                                ans.setSelectedOptionId(optId);
                                connection.send(RequestType.SUBMIT_ANSWER, ans);
                            }
                        }
                    } else {
                        UserAnswerDto ans = new UserAnswerDto();
                        ans.setAttemptId(attempt.getAttemptId());
                        ans.setQuestionId(q.getQuestionId());
                        ans.setSelectedOptionId(selectedOptions.get(q.getQuestionId()));
                        ans.setUserTextAnswer(textAnswers.get(q.getQuestionId()));
                        connection.send(RequestType.SUBMIT_ANSWER, ans);
                    }
                }
                Response finishResp = connection.send(RequestType.FINISH_ATTEMPT, attempt.getAttemptId());
                Platform.runLater(() -> {
                    if (finishResp.isSuccess()) {
                        TestAttemptDto result = connection.getGson().fromJson(finishResp.getPayload(), TestAttemptDto.class);
                        showResult(result);
                    } else {
                        AlertUtil.showError("Ошибка", finishResp.getMessage());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtil.showError("Ошибка", e.getMessage()));
            }
        }).start();
    }

    private void showResult(TestAttemptDto result) {
        String passed = Boolean.TRUE.equals(result.getPassed()) ? "ПРОЙДЕН ✓" : "НЕ ПРОЙДЕН ✗";
        String msg = "Тест завершён!\n\n" +
                "Результат: " + passed + "\n" +
                "Ваш балл: " + String.format("%.1f", result.getScore()) + "%\n" +
                "Проходной балл: " + result.getPassingScore() + "%";
        AlertUtil.showInfo("Результат теста", msg);
        closeWindow();
    }
}
