package com.proftest.server.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Persons")
public class Person extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PersonID")
    private Long personId;

    @Column(name = "FullName", nullable = false, length = 150)
    private String fullName;

    @Column(name = "DateOfBirth")
    private LocalDate dateOfBirth;

    @Column(name = "Email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "Phone", length = 20)
    private String phone;

    @Column(name = "Address", length = 255)
    private String address;

    @Column(name = "Department", length = 100)
    private String department;

    @Column(name = "Position", length = 100)
    private String position;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "person")
    private User user;

    public Person() {}

    public Person(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @Override
    public Long getId() { return personId; }

    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
