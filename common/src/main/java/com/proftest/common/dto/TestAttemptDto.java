package com.proftest.common.dto;

import java.io.Serializable;
import java.util.List;

public class TestAttemptDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long attemptId;
    private Long assignmentId;
    private Long userId;
    private String userLogin;
    private String userFullName;
    private Long testId;
    private String testTitle;
    private String startTime;
    private String endTime;
    private Double score;
    private String status;
    private Boolean passed;
    private Integer passingScore;
    private Integer timeLimitMinutes;
    private List<QuestionDto> questions;
    private List<UserAnswerDto> answers;

    public TestAttemptDto() {}

    public Long getAttemptId() { return attemptId; }
    public void setAttemptId(Long attemptId) { this.attemptId = attemptId; }
    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserLogin() { return userLogin; }
    public void setUserLogin(String userLogin) { this.userLogin = userLogin; }
    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }
    public Long getTestId() { return testId; }
    public void setTestId(Long testId) { this.testId = testId; }
    public String getTestTitle() { return testTitle; }
    public void setTestTitle(String testTitle) { this.testTitle = testTitle; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }
    public Integer getPassingScore() { return passingScore; }
    public void setPassingScore(Integer passingScore) { this.passingScore = passingScore; }
    public Integer getTimeLimitMinutes() { return timeLimitMinutes; }
    public void setTimeLimitMinutes(Integer timeLimitMinutes) { this.timeLimitMinutes = timeLimitMinutes; }
    public List<QuestionDto> getQuestions() { return questions; }
    public void setQuestions(List<QuestionDto> questions) { this.questions = questions; }
    public List<UserAnswerDto> getAnswers() { return answers; }
    public void setAnswers(List<UserAnswerDto> answers) { this.answers = answers; }
}
