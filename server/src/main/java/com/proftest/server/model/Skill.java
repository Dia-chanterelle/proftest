package com.proftest.server.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Skills")
public class Skill extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SkillID")
    private Long skillId;

    @Column(name = "SkillName", nullable = false, length = 100)
    private String skillName;

    @Column(name = "Category", nullable = false, length = 80)
    private String category;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "DifficultyLevel")
    private Integer difficultyLevel;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    public Skill() {}

    public Skill(String skillName, String category) {
        this.skillName = skillName;
        this.category = category;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @Override
    public Long getId() { return skillId; }

    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }
    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(Integer difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() { return skillName; }
}
