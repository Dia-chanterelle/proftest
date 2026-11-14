package com.proftest.common.dto;

import java.io.Serializable;

public class SkillDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long skillId;
    private String skillName;
    private String category;
    private String description;
    private Integer difficultyLevel;

    public SkillDto() {}

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

    @Override
    public String toString() {
        return skillName + " (" + category + ")";
    }
}
