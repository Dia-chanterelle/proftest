package com.proftest.common.dto;

import java.io.Serializable;
import java.util.List;

public class TestDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long testId;
    private String title;
    private String description;
    private Long creatorId;
    private String creatorName;
    private Integer timeLimitMinutes;
    private Integer maxAttempts;
    private Integer passingScore;
    private boolean isActive;
    private String createdAt;
    private List<QuestionDto> questions;
    private List<SkillDto> skills;

    public TestDto() {}

    public Long getTestId() { return testId; }
    public void setTestId(Long testId) { this.testId = testId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    public Integer getTimeLimitMinutes() { return timeLimitMinutes; }
    public void setTimeLimitMinutes(Integer timeLimitMinutes) { this.timeLimitMinutes = timeLimitMinutes; }
    public Integer getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(Integer maxAttempts) { this.maxAttempts = maxAttempts; }
    public Integer getPassingScore() { return passingScore; }
    public void setPassingScore(Integer passingScore) { this.passingScore = passingScore; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public List<QuestionDto> getQuestions() { return questions; }
    public void setQuestions(List<QuestionDto> questions) { this.questions = questions; }
    public List<SkillDto> getSkills() { return skills; }
    public void setSkills(List<SkillDto> skills) { this.skills = skills; }

    @Override
    public String toString() { return title; }
}
