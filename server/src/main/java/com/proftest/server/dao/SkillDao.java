package com.proftest.server.dao;

import com.proftest.server.model.Skill;
import org.hibernate.Session;

import java.util.List;

public class SkillDao extends AbstractDao<Skill> {

    public List<Skill> findByCategory(String category) {
        Session session = getSession();
        return session.createQuery("FROM Skill s WHERE s.category = :cat ORDER BY s.skillName", Skill.class)
                .setParameter("cat", category)
                .list();
    }

    public List<String> findAllCategories() {
        Session session = getSession();
        return session.createQuery("SELECT DISTINCT s.category FROM Skill s ORDER BY s.category", String.class).list();
    }
}
