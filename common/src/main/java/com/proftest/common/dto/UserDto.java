package com.proftest.common.dto;

import java.io.Serializable;
import java.util.List;

public class UserDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String login;
    private String password;
    private boolean isActive;
    private String lastLogin;
    private PersonDto person;
    private List<String> roles;

    public UserDto() {}

    public UserDto(Long userId, String login) {
        this.userId = userId;
        this.login = login;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public String getLastLogin() { return lastLogin; }
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }
    public PersonDto getPerson() { return person; }
    public void setPerson(PersonDto person) { this.person = person; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
}
