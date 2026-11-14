package com.proftest.server.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TestAttempts")
public class TestAttempt extends BaseEntity {

    public enum Status { IN_PROGRESS, COMPLETED, EXPIRED, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AttemptID")
    private Long attemptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AssignmentID", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @Column(name = "StartTime", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "EndTime")
    private LocalDateTime endTime;

    @Column(name = "Score", precision = 5, scale = 2)
    private BigDecimal score;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private Status status = Status.IN_PROGRESS;

    @Column(name = "Passed")
    private Boolean passed;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserAnswer> answers = new ArrayList<>();

    public TestAttempt() {}

    public TestAttempt(Assignment assignment, User user) {
        this.assignment = assignment;
        this.user = user;
        this.startTime = LocalDateTime.now();
        this.status = Status.IN_PROGRESS;
    }

    @Override
    public Long getId() { return attemptId; }

    public Long getAttemptId() { return attemptId; }
    public void setAttemptId(Long attemptId) { this.attemptId = attemptId; }
    public Assignment getAssignment() { return assignment; }
    public void setAssignment(Assignment assignment) { this.assignment = assignment; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }
    public List<UserAnswer> getAnswers() { return answers; }
    public void setAnswers(List<UserAnswer> answers) { this.answers = answers; }
}
