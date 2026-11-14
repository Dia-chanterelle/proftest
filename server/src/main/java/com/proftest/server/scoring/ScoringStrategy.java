package com.proftest.server.scoring;

import com.proftest.server.model.Question;
import com.proftest.server.model.UserAnswer;

/**
 * Паттерн Strategy — стратегия подсчёта баллов за ответ.
 */
public interface ScoringStrategy {
    int score(Question question, UserAnswer answer);
}
