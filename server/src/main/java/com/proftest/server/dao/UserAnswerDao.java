package com.proftest.server.dao;

import com.proftest.server.model.UserAnswer;
import org.hibernate.Session;

import java.util.List;

public class UserAnswerDao extends AbstractDao<UserAnswer> {

    public List<UserAnswer> findByAttempt(Long attemptId) {
        Session session = getSession();
        return session.createQuery(
                "SELECT ua FROM UserAnswer ua JOIN FETCH ua.question q LEFT JOIN FETCH q.options " +
                "LEFT JOIN FETCH ua.selectedOption " +
                "WHERE ua.attempt.attemptId = :aid", UserAnswer.class)
                .setParameter("aid", attemptId)
                .list();
    }
}
