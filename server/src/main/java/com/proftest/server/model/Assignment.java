package com.proftest.server.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Assignments")
public class Assignment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AssignmentID")
    private Long assignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TestID", nullable = false)
    private Test test;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AssignedByID", nullable = false)
    private User assignedBy;

    @Column(name = "Deadline")
    private LocalDateTime deadline;

    @Column(name = "AllowedAttempts")
    private Integer allowedAttempts = 1;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    public Assignment() {}

    public Assignment(Test test, User user, User assignedBy) {
        this.test = test;
        this.user = user;
        this.assignedBy = assignedBy;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (allowedAttempts == null) allowedAttempts = test != null ? test.getMaxAttempts() : 1;
    }

    @Override
    public Long getId() { return assignmentId; }

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
    public Test getTest() { return test; }
    public void setTest(Test test) { this.test = test; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public User getAssignedBy() { return assignedBy; }
    public void setAssignedBy(User assignedBy) { this.assignedBy = assignedBy; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public Integer getAllowedAttempts() { return allowedAttempts; }
    public void setAllowedAttempts(Integer allowedAttempts) { this.allowedAttempts = allowedAttempts; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
