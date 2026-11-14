package com.proftest.server.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Questions")
public class Question extends BaseEntity {

    public enum QuestionType { SINGLE_CHOICE, MULTIPLE_CHOICE, OPEN }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "QuestionID")
    private Long questionId;

    @Column(name = "Text", nullable = false, columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(name = "QuestionType", nullable = false)
    private QuestionType questionType;

    @Column(name = "Points", nullable = false)
    private Integer points = 1;

    @Column(name = "Difficulty")
    private Integer difficulty;

    @Column(name = "Topic", length = 100)
    private String topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatedByID")
    private User createdBy;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<QuestionOption> options = new ArrayList<>();

    public Question() {}

    public Question(String text, QuestionType questionType, Integer points) {
        this.text = text;
        this.questionType = questionType;
        this.points = points;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @Override
    public Long getId() { return questionId; }

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public QuestionType getQuestionType() { return questionType; }
    public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public Integer getDifficulty() { return difficulty; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<QuestionOption> getOptions() { return options; }
    public void setOptions(List<QuestionOption> options) { this.options = options; }

    public void addOption(QuestionOption option) {
        options.add(option);
        option.setQuestion(this);
    }
}
