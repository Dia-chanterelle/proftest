package com.proftest.server.dao;

import com.proftest.server.model.Assignment;
import org.hibernate.Session;

import java.util.List;

public class AssignmentDao extends AbstractDao<Assignment> {

    @Override
    public List<Assignment> findAll() {
        Session session = getSession();
        return session.createQuery(
                "SELECT a FROM Assignment a " +
                "JOIN FETCH a.test JOIN FETCH a.user u JOIN FETCH u.person " +
                "JOIN FETCH a.assignedBy ab JOIN FETCH ab.person " +
                "ORDER BY a.createdAt DESC", Assignment.class)
                .list();
    }

    public List<Assignment> findByUser(Long userId) {
        Session session = getSession();
        return session.createQuery(
                "SELECT a FROM Assignment a JOIN FETCH a.test WHERE a.user.userId = :uid ORDER BY a.createdAt DESC", Assignment.class)
                .setParameter("uid", userId)
                .list();
    }

    public long countAttemptsByAssignment(Long assignmentId) {
        Session session = getSession();
        Long count = session.createQuery(
                "SELECT COUNT(ta) FROM TestAttempt ta WHERE ta.assignment.assignmentId = :aid AND ta.status = 'COMPLETED'", Long.class)
                .setParameter("aid", assignmentId)
                .uniqueResult();
        return count != null ? count : 0;
    }

    public int deleteByTestId(Long testId) {
        Session session = getSession();
        return session.createMutationQuery(
                "DELETE FROM Assignment a WHERE a.test.testId = :tid")
                .setParameter("tid", testId)
                .executeUpdate();
    }

    public boolean existsForUserAndTest(Long userId, Long testId) {
        Session session = getSession();
        Long count = session.createQuery(
                "SELECT COUNT(a) FROM Assignment a WHERE a.user.userId = :uid AND a.test.testId = :tid", Long.class)
                .setParameter("uid", userId)
                .setParameter("tid", testId)
                .uniqueResult();
        return count != null && count > 0;
    }
}
