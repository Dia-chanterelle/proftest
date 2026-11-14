package com.proftest.server.model;

import jakarta.persistence.*;

@Entity
@Table(name = "UserAnswers")
public class UserAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AnswerID")
    private Long answerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AttemptID", nullable = false)
    private TestAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "QuestionID", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SelectedOptionID")
    private QuestionOption selectedOption;

    @Column(name = "UserTextAnswer", columnDefinition = "TEXT")
    private String userTextAnswer;

    @Column(name = "IsCorrect")
    private Boolean isCorrect;

    @Column(name = "PointsEarned")
    private Integer pointsEarned;

    public UserAnswer() {}

    public UserAnswer(TestAttempt attempt, Question question) {
        this.attempt = attempt;
        this.question = question;
    }

    @Override
    public Long getId() { return answerId; }

    public Long getAnswerId() { return answerId; }
    public void setAnswerId(Long answerId) { this.answerId = answerId; }
    public TestAttempt getAttempt() { return attempt; }
    public void setAttempt(TestAttempt attempt) { this.attempt = attempt; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public QuestionOption getSelectedOption() { return selectedOption; }
    public void setSelectedOption(QuestionOption selectedOption) { this.selectedOption = selectedOption; }
    public String getUserTextAnswer() { return userTextAnswer; }
    public void setUserTextAnswer(String userTextAnswer) { this.userTextAnswer = userTextAnswer; }
    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }
    public Integer getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(Integer pointsEarned) { this.pointsEarned = pointsEarned; }
}
