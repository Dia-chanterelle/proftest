package com.proftest.server.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Long userId;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "PersonID", nullable = false, unique = true)
    private Person person;

    @Column(name = "Login", unique = true, nullable = false, length = 50)
    private String login;

    @Column(name = "PasswordHash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "IsActive")
    private boolean isActive = true;

    @Column(name = "LastLogin")
    private LocalDateTime lastLogin;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "UserRoles",
        joinColumns = @JoinColumn(name = "UserID"),
        inverseJoinColumns = @JoinColumn(name = "RoleID")
    )
    private Set<Role> roles = new HashSet<>();

    public User() {}

    public User(Person person, String login, String passwordHash) {
        this.person = person;
        this.login = login;
        this.passwordHash = passwordHash;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @Override
    public Long getId() { return userId; }

    public boolean hasRole(String roleName) {
        return roles.stream().anyMatch(r -> r.getRoleName().equals(roleName));
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Person getPerson() { return person; }
    public void setPerson(Person person) { this.person = person; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
}
