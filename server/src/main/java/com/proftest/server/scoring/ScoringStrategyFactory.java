package com.proftest.server.scoring;

import com.proftest.server.model.Question;

/**
 * Паттерн Factory Method — создаёт нужную стратегию подсчёта баллов по типу вопроса.
 */
public class ScoringStrategyFactory {

    private ScoringStrategyFactory() {}

    public static ScoringStrategy getStrategy(Question.QuestionType type) {
        return switch (type) {
            case SINGLE_CHOICE -> new SingleChoiceScoringStrategy();
            case MULTIPLE_CHOICE -> new MultipleChoiceScoringStrategy();
            case OPEN -> new OpenAnswerScoringStrategy();
        };
    }
}
