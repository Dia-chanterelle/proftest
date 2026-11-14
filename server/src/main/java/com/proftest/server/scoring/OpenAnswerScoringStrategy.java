package com.proftest.server.scoring;

import com.proftest.server.model.Question;
import com.proftest.server.model.UserAnswer;

/**
 * Стратегия для открытых вопросов — баллы не начисляются автоматически (требует проверки).
 */
public class OpenAnswerScoringStrategy implements ScoringStrategy {

    @Override
    public int score(Question question, UserAnswer answer) {
        answer.setIsCorrect(null);
        return 0;
    }
}
