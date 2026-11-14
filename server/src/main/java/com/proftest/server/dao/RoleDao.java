package com.proftest.server.dao;

import com.proftest.server.model.Role;
import org.hibernate.Session;

import java.util.Optional;

public class RoleDao extends AbstractDao<Role> {

    public Optional<Role> findByName(String roleName) {
        Session session = getSession();
        return session.createQuery("FROM Role r WHERE r.roleName = :name", Role.class)
                .setParameter("name", roleName)
                .uniqueResultOptional();
    }
}
