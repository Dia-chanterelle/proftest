package com.proftest.common.dto;

import java.io.Serializable;

public class PersonDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long personId;
    private String fullName;
    private String dateOfBirth;
    private String email;
    private String phone;
    private String address;
    private String department;
    private String position;

    public PersonDto() {}

    public PersonDto(Long personId, String fullName, String email) {
        this.personId = personId;
        this.fullName = fullName;
        this.email = email;
    }

    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
}
