package com.proftest.common.dto;

import java.io.Serializable;

public class QuestionOptionDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long optionId;
    private Long questionId;
    private String optionText;
    private boolean isCorrect;

    public QuestionOptionDto() {}

    public QuestionOptionDto(Long optionId, String optionText, boolean isCorrect) {
        this.optionId = optionId;
        this.optionText = optionText;
        this.isCorrect = isCorrect;
    }

    public Long getOptionId() { return optionId; }
    public void setOptionId(Long optionId) { this.optionId = optionId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getOptionText() { return optionText; }
    public void setOptionText(String optionText) { this.optionText = optionText; }
    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }

    @Override
    public String toString() { return optionText; }
}
