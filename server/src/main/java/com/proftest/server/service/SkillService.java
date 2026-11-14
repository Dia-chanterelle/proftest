package com.proftest.server.service;

import com.proftest.common.dto.SkillDto;
import com.proftest.server.dao.SkillDao;
import com.proftest.server.model.Skill;
import com.proftest.server.util.DtoMapper;

import java.util.List;

public class SkillService extends BaseService {

    private final SkillDao skillDao = new SkillDao();

    public List<SkillDto> getAllSkills() {
        return inTransaction(() -> DtoMapper.toSkillDtoList(skillDao.findAll()));
    }

    public SkillDto createSkill(SkillDto dto) {
        return inTransaction(() -> {
            Skill skill = new Skill(dto.getSkillName(), dto.getCategory());
            skill.setDescription(dto.getDescription());
            skill.setDifficultyLevel(dto.getDifficultyLevel());
            skillDao.save(skill);
            logger.info("Skill created: {}", dto.getSkillName());
            return DtoMapper.toDto(skill);
        });
    }

    public SkillDto updateSkill(SkillDto dto) {
        return inTransaction(() -> {
            Skill skill = skillDao.findById(dto.getSkillId())
                    .orElseThrow(() -> new RuntimeException("Skill not found: " + dto.getSkillId()));
            skill.setSkillName(dto.getSkillName());
            skill.setCategory(dto.getCategory());
            skill.setDescription(dto.getDescription());
            skill.setDifficultyLevel(dto.getDifficultyLevel());
            return DtoMapper.toDto(skillDao.update(skill));
        });
    }

    public void deleteSkill(Long skillId) {
        inTransactionVoid(() -> skillDao.deleteById(skillId));
    }
}
