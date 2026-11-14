package com.proftest.server.dao;

import com.proftest.server.model.Person;
import org.hibernate.Session;

import java.util.Optional;

public class PersonDao extends AbstractDao<Person> {

    public Optional<Person> findByEmail(String email) {
        Session session = getSession();
        return session.createQuery("FROM Person p WHERE p.email = :email", Person.class)
                .setParameter("email", email)
                .uniqueResultOptional();
    }

    public boolean existsByEmail(String email) {
        Session session = getSession();
        Long count = session.createQuery("SELECT COUNT(p) FROM Person p WHERE p.email = :email", Long.class)
                .setParameter("email", email)
                .uniqueResult();
        return count != null && count > 0;
    }
}
