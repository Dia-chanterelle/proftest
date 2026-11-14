package com.proftest.server.dao;

import com.proftest.server.model.User;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

public class UserDao extends AbstractDao<User> {

    public Optional<User> findByLogin(String login) {
        Session session = getSession();
        return session.createQuery(
                "SELECT u FROM User u JOIN FETCH u.person JOIN FETCH u.roles WHERE u.login = :login", User.class)
                .setParameter("login", login)
                .uniqueResultOptional();
    }

    public Optional<User> findByIdWithDetails(Long id) {
        Session session = getSession();
        return session.createQuery(
                "SELECT u FROM User u JOIN FETCH u.person JOIN FETCH u.roles WHERE u.userId = :id", User.class)
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    @Override
    public List<User> findAll() {
        Session session = getSession();
        return session.createQuery(
                "SELECT DISTINCT u FROM User u JOIN FETCH u.person LEFT JOIN FETCH u.roles ORDER BY u.userId", User.class)
                .list();
    }

    public List<User> findByRole(String roleName) {
        Session session = getSession();
        return session.createQuery(
                "SELECT DISTINCT u FROM User u JOIN FETCH u.person JOIN FETCH u.roles r WHERE r.roleName = :roleName", User.class)
                .setParameter("roleName", roleName)
                .list();
    }

    public boolean existsByLogin(String login) {
        Session session = getSession();
        Long count = session.createQuery("SELECT COUNT(u) FROM User u WHERE u.login = :login", Long.class)
                .setParameter("login", login)
                .uniqueResult();
        return count != null && count > 0;
    }
}
