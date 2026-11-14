package com.proftest.server.dao;

import com.proftest.server.model.Question;
import org.hibernate.Session;

import java.util.List;

public class QuestionDao extends AbstractDao<Question> {

    @Override
    public List<Question> findAll() {
        Session session = getSession();
        return session.createQuery(
                "SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.options ORDER BY q.questionId", Question.class)
                .list();
    }

    public List<Question> findByTopic(String topic) {
        Session session = getSession();
        return session.createQuery(
                "SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.options WHERE q.topic = :topic", Question.class)
                .setParameter("topic", topic)
                .list();
    }

    public List<String> findAllTopics() {
        Session session = getSession();
        return session.createQuery("SELECT DISTINCT q.topic FROM Question q WHERE q.topic IS NOT NULL ORDER BY q.topic", String.class).list();
    }

    public List<Question> findByTest(Long testId) {
        Session session = getSession();
        return session.createQuery(
                "SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.options WHERE q IN " +
                "(SELECT tq FROM Test t JOIN t.questions tq WHERE t.testId = :testId)", Question.class)
                .setParameter("testId", testId)
                .list();
    }
}
