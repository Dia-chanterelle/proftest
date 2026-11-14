package com.proftest.common.dto;

import java.io.Serializable;

public class UserAnswerDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long answerId;
    private Long attemptId;
    private Long questionId;
    private Long selectedOptionId;
    private String userTextAnswer;
    private Boolean isCorrect;
    private Integer pointsEarned;

    public UserAnswerDto() {}

    public UserAnswerDto(Long attemptId, Long questionId, Long selectedOptionId, String userTextAnswer) {
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.selectedOptionId = selectedOptionId;
        this.userTextAnswer = userTextAnswer;
    }

    public Long getAnswerId() { return answerId; }
    public void setAnswerId(Long answerId) { this.answerId = answerId; }
    public Long getAttemptId() { return attemptId; }
    public void setAttemptId(Long attemptId) { this.attemptId = attemptId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public Long getSelectedOptionId() { return selectedOptionId; }
    public void setSelectedOptionId(Long selectedOptionId) { this.selectedOptionId = selectedOptionId; }
    public String getUserTextAnswer() { return userTextAnswer; }
    public void setUserTextAnswer(String userTextAnswer) { this.userTextAnswer = userTextAnswer; }
    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }
    public Integer getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(Integer pointsEarned) { this.pointsEarned = pointsEarned; }
}
