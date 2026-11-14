package com.proftest.server.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Roles")
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoleID")
    private Long roleId;

    @Column(name = "RoleName", nullable = false, unique = true, length = 50)
    private String roleName;

    public Role() {}

    public Role(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public Long getId() { return roleId; }

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    @Override
    public String toString() { return roleName; }
}
