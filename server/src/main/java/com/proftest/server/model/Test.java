package com.proftest.server.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Tests")
public class Test extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TestID")
    private Long testId;

    @Column(name = "Title", nullable = false, length = 200)
    private String title;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatorID")
    private User creator;

    @Column(name = "TimeLimitMinutes", nullable = false)
    private Integer timeLimitMinutes;

    @Column(name = "MaxAttempts")
    private Integer maxAttempts = 1;

    @Column(name = "PassingScore", nullable = false)
    private Integer passingScore;

    @Column(name = "IsActive")
    private boolean isActive = true;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "TestQuestions",
        joinColumns = @JoinColumn(name = "TestID"),
        inverseJoinColumns = @JoinColumn(name = "QuestionID")
    )
    private List<Question> questions = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "TestSkills",
        joinColumns = @JoinColumn(name = "TestID"),
        inverseJoinColumns = @JoinColumn(name = "SkillID")
    )
    private Set<Skill> skills = new HashSet<>();

    public Test() {}

    public Test(String title, Integer timeLimitMinutes, Integer passingScore) {
        this.title = title;
        this.timeLimitMinutes = timeLimitMinutes;
        this.passingScore = passingScore;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @Override
    public Long getId() { return testId; }

    public Long getTestId() { return testId; }
    public void setTestId(Long testId) { this.testId = testId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }
    public Integer getTimeLimitMinutes() { return timeLimitMinutes; }
    public void setTimeLimitMinutes(Integer timeLimitMinutes) { this.timeLimitMinutes = timeLimitMinutes; }
    public Integer getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(Integer maxAttempts) { this.maxAttempts = maxAttempts; }
    public Integer getPassingScore() { return passingScore; }
    public void setPassingScore(Integer passingScore) { this.passingScore = passingScore; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
    public Set<Skill> getSkills() { return skills; }
    public void setSkills(Set<Skill> skills) { this.skills = skills; }

    @Override
    public String toString() { return title; }
}
