package com.proftest.server.dao;

import com.proftest.server.model.Test;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

public class TestDao extends AbstractDao<Test> {

    @Override
    public List<Test> findAll() {
        Session session = getSession();
        return session.createQuery(
                "SELECT DISTINCT t FROM Test t LEFT JOIN FETCH t.creator c LEFT JOIN FETCH c.person ORDER BY t.testId", Test.class)
                .list();
    }

    public Optional<Test> findByIdWithQuestions(Long id) {
        Session session = getSession();
        // Question.options is EAGER — do NOT join-fetch it here, fetching two bags simultaneously
        // causes MultipleBagFetchException. Options are loaded automatically with each question.
        return session.createQuery(
                "SELECT DISTINCT t FROM Test t LEFT JOIN FETCH t.questions WHERE t.testId = :id", Test.class)
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    public List<Test> findAllWithSkills() {
        Session session = getSession();
        return session.createQuery(
                "SELECT DISTINCT t FROM Test t " +
                "LEFT JOIN FETCH t.creator c LEFT JOIN FETCH c.person " +
                "LEFT JOIN FETCH t.skills " +
                "ORDER BY t.testId", Test.class)
                .list();
    }

    public Optional<Test> findByIdWithSkills(Long id) {
        Session session = getSession();
        return session.createQuery(
                "SELECT t FROM Test t LEFT JOIN FETCH t.skills WHERE t.testId = :id",
                Test.class)
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    public List<Test> findAllActive() {
        Session session = getSession();
        return session.createQuery(
                "SELECT t FROM Test t WHERE t.isActive = true ORDER BY t.title", Test.class)
                .list();
    }

    public List<Test> findByCreator(Long creatorId) {
        Session session = getSession();
        return session.createQuery(
                "SELECT t FROM Test t WHERE t.creator.userId = :cid ORDER BY t.createdAt DESC", Test.class)
                .setParameter("cid", creatorId)
                .list();
    }
}
