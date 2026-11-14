package com.proftest.common.dto;

import java.io.Serializable;
import java.util.List;

public class QuestionDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long questionId;
    private String text;
    private String questionType;
    private Integer points;
    private Integer difficulty;
    private String topic;
    private Long createdById;
    private List<QuestionOptionDto> options;

    public QuestionDto() {}

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public Integer getDifficulty() { return difficulty; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }
    public List<QuestionOptionDto> getOptions() { return options; }
    public void setOptions(List<QuestionOptionDto> options) { this.options = options; }

    @Override
    public String toString() { return "[" + questionType + "] " + text; }
}
