package com.proftest.common.dto;

import java.io.Serializable;

public class AssignmentDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long assignmentId;
    private Long testId;
    private String testTitle;
    private Long userId;
    private String userLogin;
    private String userFullName;
    private Long assignedById;
    private String assignedByLogin;
    private String deadline;
    private Integer allowedAttempts;
    private String createdAt;
    private Integer attemptsUsed;
    private String status;

    public AssignmentDto() {}

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
    public Long getTestId() { return testId; }
    public void setTestId(Long testId) { this.testId = testId; }
    public String getTestTitle() { return testTitle; }
    public void setTestTitle(String testTitle) { this.testTitle = testTitle; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserLogin() { return userLogin; }
    public void setUserLogin(String userLogin) { this.userLogin = userLogin; }
    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }
    public Long getAssignedById() { return assignedById; }
    public void setAssignedById(Long assignedById) { this.assignedById = assignedById; }
    public String getAssignedByLogin() { return assignedByLogin; }
    public void setAssignedByLogin(String assignedByLogin) { this.assignedByLogin = assignedByLogin; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public Integer getAllowedAttempts() { return allowedAttempts; }
    public void setAllowedAttempts(Integer allowedAttempts) { this.allowedAttempts = allowedAttempts; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public Integer getAttemptsUsed() { return attemptsUsed; }
    public void setAttemptsUsed(Integer attemptsUsed) { this.attemptsUsed = attemptsUsed; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
