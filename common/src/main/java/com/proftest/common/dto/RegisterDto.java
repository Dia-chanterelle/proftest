package com.proftest.common.dto;

import java.io.Serializable;

public class RegisterDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String login;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String dateOfBirth;
    private String address;
    private String department;
    private String position;
    private String role;

    public RegisterDto() {}

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
