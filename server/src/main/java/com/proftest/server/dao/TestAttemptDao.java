package com.proftest.server.dao;

import com.proftest.server.model.TestAttempt;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

public class TestAttemptDao extends AbstractDao<TestAttempt> {

    public List<TestAttempt> findByUser(Long userId) {
        Session session = getSession();
        return session.createQuery(
                "SELECT ta FROM TestAttempt ta " +
                "JOIN FETCH ta.assignment a JOIN FETCH a.test " +
                "WHERE ta.user.userId = :uid ORDER BY ta.startTime DESC", TestAttempt.class)
                .setParameter("uid", userId)
                .list();
    }

    public List<TestAttempt> findAll() {
        Session session = getSession();
        return session.createQuery(
                "SELECT ta FROM TestAttempt ta " +
                "JOIN FETCH ta.assignment a JOIN FETCH a.test t " +
                "JOIN FETCH ta.user u JOIN FETCH u.person " +
                "ORDER BY ta.startTime DESC", TestAttempt.class)
                .list();
    }

    public List<TestAttempt> findByTest(Long testId) {
        Session session = getSession();
        return session.createQuery(
                "SELECT ta FROM TestAttempt ta " +
                "JOIN FETCH ta.assignment a JOIN FETCH a.test t " +
                "JOIN FETCH ta.user u JOIN FETCH u.person " +
                "WHERE t.testId = :tid ORDER BY ta.startTime DESC", TestAttempt.class)
                .setParameter("tid", testId)
                .list();
    }

    public Optional<TestAttempt> findByIdWithAnswers(Long id) {
        Session session = getSession();
        // Question.options is EAGER — omit explicit JOIN FETCH for options to avoid
        // MultipleBagFetchException (ta.answers List + q.options List = two bags).
        return session.createQuery(
                "SELECT ta FROM TestAttempt ta " +
                "LEFT JOIN FETCH ta.answers ans LEFT JOIN FETCH ans.question " +
                "LEFT JOIN FETCH ans.selectedOption " +
                "JOIN FETCH ta.assignment a JOIN FETCH a.test " +
                "WHERE ta.attemptId = :id", TestAttempt.class)
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    public long countCompletedByAssignment(Long assignmentId) {
        Session session = getSession();
        Long count = session.createQuery(
                "SELECT COUNT(ta) FROM TestAttempt ta WHERE ta.assignment.assignmentId = :aid AND ta.status = :status", Long.class)
                .setParameter("aid", assignmentId)
                .setParameter("status", TestAttempt.Status.COMPLETED)
                .uniqueResult();
        return count != null ? count : 0;
    }
}
