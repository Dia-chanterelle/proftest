package com.proftest.server.scoring;

import com.proftest.server.model.Question;
import com.proftest.server.model.QuestionOption;
import com.proftest.server.model.UserAnswer;

/**
 * Стратегия для вопроса с одним правильным ответом.
 */
public class SingleChoiceScoringStrategy implements ScoringStrategy {

    @Override
    public int score(Question question, UserAnswer answer) {
        if (answer.getSelectedOption() == null) return 0;
        QuestionOption selected = answer.getSelectedOption();
        if (selected.isCorrect()) {
            answer.setIsCorrect(true);
            return question.getPoints();
        }
        answer.setIsCorrect(false);
        return 0;
    }
}
