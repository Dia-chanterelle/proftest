package com.proftest.server.scoring;

import com.proftest.server.model.Question;
import com.proftest.server.model.UserAnswer;

/**
 * Стратегия для вопроса с несколькими правильными ответами.
 * Каждый отдельный ответ получает 0 баллов.
 * Итоговая проверка (все ли правильные выбраны и нет ли лишних)
 * производится в TestingService.finishAttempt при закрытии попытки.
 */
public class MultipleChoiceScoringStrategy implements ScoringStrategy {

    @Override
    public int score(Question question, UserAnswer answer) {
        answer.setIsCorrect(answer.getSelectedOption() != null && answer.getSelectedOption().isCorrect());
        return 0;
    }
}
