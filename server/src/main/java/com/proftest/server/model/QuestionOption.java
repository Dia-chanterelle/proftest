package com.proftest.server.model;

import jakarta.persistence.*;

@Entity
@Table(name = "QuestionOptions")
public class QuestionOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OptionID")
    private Long optionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "QuestionID", nullable = false)
    private Question question;

    @Column(name = "OptionText", nullable = false, columnDefinition = "TEXT")
    private String optionText;

    @Column(name = "IsCorrect", nullable = false)
    private boolean isCorrect = false;

    public QuestionOption() {}

    public QuestionOption(Question question, String optionText, boolean isCorrect) {
        this.question = question;
        this.optionText = optionText;
        this.isCorrect = isCorrect;
    }

    @Override
    public Long getId() { return optionId; }

    public Long getOptionId() { return optionId; }
    public void setOptionId(Long optionId) { this.optionId = optionId; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public String getOptionText() { return optionText; }
    public void setOptionText(String optionText) { this.optionText = optionText; }
    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }
}
