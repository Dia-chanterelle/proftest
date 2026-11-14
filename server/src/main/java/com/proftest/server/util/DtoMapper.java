package com.proftest.server.util;

import com.proftest.common.dto.*;
import com.proftest.server.model.*;

import org.hibernate.Hibernate;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Утилита для преобразования Entity <-> DTO.
 */
public class DtoMapper {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter D_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DtoMapper() {}

    public static UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setLogin(user.getLogin());
        dto.setActive(user.isActive());
        if (user.getLastLogin() != null) dto.setLastLogin(user.getLastLogin().format(DT_FMT));
        dto.setPerson(toDto(user.getPerson()));
        dto.setRoles(user.getRoles().stream().map(Role::getRoleName).collect(Collectors.toList()));
        return dto;
    }

    public static PersonDto toDto(Person person) {
        if (person == null) return null;
        PersonDto dto = new PersonDto();
        dto.setPersonId(person.getPersonId());
        dto.setFullName(person.getFullName());
        dto.setEmail(person.getEmail());
        dto.setPhone(person.getPhone());
        dto.setAddress(person.getAddress());
        dto.setDepartment(person.getDepartment());
        dto.setPosition(person.getPosition());
        if (person.getDateOfBirth() != null) dto.setDateOfBirth(person.getDateOfBirth().format(D_FMT));
        return dto;
    }

    public static SkillDto toDto(Skill skill) {
        SkillDto dto = new SkillDto();
        dto.setSkillId(skill.getSkillId());
        dto.setSkillName(skill.getSkillName());
        dto.setCategory(skill.getCategory());
        dto.setDescription(skill.getDescription());
        dto.setDifficultyLevel(skill.getDifficultyLevel());
        return dto;
    }

    public static QuestionOptionDto toDto(QuestionOption opt) {
        QuestionOptionDto dto = new QuestionOptionDto();
        dto.setOptionId(opt.getOptionId());
        dto.setQuestionId(opt.getQuestion() != null ? opt.getQuestion().getQuestionId() : null);
        dto.setOptionText(opt.getOptionText());
        dto.setCorrect(opt.isCorrect());
        return dto;
    }

    public static QuestionDto toDto(Question q) {
        QuestionDto dto = new QuestionDto();
        dto.setQuestionId(q.getQuestionId());
        dto.setText(q.getText());
        dto.setQuestionType(q.getQuestionType().name());
        dto.setPoints(q.getPoints());
        dto.setDifficulty(q.getDifficulty());
        dto.setTopic(q.getTopic());
        if (q.getCreatedBy() != null) dto.setCreatedById(q.getCreatedBy().getUserId());
        if (q.getOptions() != null) {
            dto.setOptions(q.getOptions().stream().map(DtoMapper::toDto).collect(Collectors.toList()));
        }
        return dto;
    }

    public static QuestionDto toDtoForCandidate(Question q) {
        QuestionDto dto = toDto(q);
        if (dto.getOptions() != null) {
            dto.getOptions().forEach(o -> o.setCorrect(false));
        }
        return dto;
    }

    public static TestDto toDto(Test t) {
        TestDto dto = new TestDto();
        dto.setTestId(t.getTestId());
        dto.setTitle(t.getTitle());
        dto.setDescription(t.getDescription());
        dto.setTimeLimitMinutes(t.getTimeLimitMinutes());
        dto.setMaxAttempts(t.getMaxAttempts());
        dto.setPassingScore(t.getPassingScore());
        dto.setActive(t.isActive());
        if (t.getCreatedAt() != null) dto.setCreatedAt(t.getCreatedAt().format(DT_FMT));
        if (t.getCreator() != null) {
            dto.setCreatorId(t.getCreator().getUserId());
            dto.setCreatorName(t.getCreator().getLogin());
        }
        if (t.getSkills() != null && Hibernate.isInitialized(t.getSkills())) {
            dto.setSkills(t.getSkills().stream().map(DtoMapper::toDto).collect(Collectors.toList()));
        }
        return dto;
    }

    public static AssignmentDto toDto(Assignment a) {
        AssignmentDto dto = new AssignmentDto();
        dto.setAssignmentId(a.getAssignmentId());
        dto.setTestId(a.getTest().getTestId());
        dto.setTestTitle(a.getTest().getTitle());
        dto.setUserId(a.getUser().getUserId());
        dto.setUserLogin(a.getUser().getLogin());
        if (a.getUser().getPerson() != null) dto.setUserFullName(a.getUser().getPerson().getFullName());
        dto.setAssignedById(a.getAssignedBy().getUserId());
        dto.setAssignedByLogin(a.getAssignedBy().getLogin());
        if (a.getDeadline() != null) dto.setDeadline(a.getDeadline().format(DT_FMT));
        dto.setAllowedAttempts(a.getAllowedAttempts());
        if (a.getCreatedAt() != null) dto.setCreatedAt(a.getCreatedAt().format(DT_FMT));
        return dto;
    }

    public static TestAttemptDto toDto(TestAttempt ta) {
        TestAttemptDto dto = new TestAttemptDto();
        dto.setAttemptId(ta.getAttemptId());
        dto.setAssignmentId(ta.getAssignment().getAssignmentId());
        dto.setUserId(ta.getUser().getUserId());
        dto.setUserLogin(ta.getUser().getLogin());
        if (ta.getUser().getPerson() != null) dto.setUserFullName(ta.getUser().getPerson().getFullName());
        dto.setTestId(ta.getAssignment().getTest().getTestId());
        dto.setTestTitle(ta.getAssignment().getTest().getTitle());
        dto.setPassingScore(ta.getAssignment().getTest().getPassingScore());
        dto.setTimeLimitMinutes(ta.getAssignment().getTest().getTimeLimitMinutes());
        if (ta.getStartTime() != null) dto.setStartTime(ta.getStartTime().format(DT_FMT));
        if (ta.getEndTime() != null) dto.setEndTime(ta.getEndTime().format(DT_FMT));
        if (ta.getScore() != null) dto.setScore(ta.getScore().doubleValue());
        dto.setStatus(ta.getStatus().name());
        dto.setPassed(ta.getPassed());
        return dto;
    }

    public static UserAnswerDto toDto(UserAnswer ua) {
        UserAnswerDto dto = new UserAnswerDto();
        dto.setAnswerId(ua.getAnswerId());
        dto.setAttemptId(ua.getAttempt().getAttemptId());
        dto.setQuestionId(ua.getQuestion().getQuestionId());
        if (ua.getSelectedOption() != null) dto.setSelectedOptionId(ua.getSelectedOption().getOptionId());
        dto.setUserTextAnswer(ua.getUserTextAnswer());
        dto.setIsCorrect(ua.getIsCorrect());
        dto.setPointsEarned(ua.getPointsEarned());
        return dto;
    }

    public static List<UserDto> toUserDtoList(List<User> users) {
        return users.stream().map(DtoMapper::toDto).collect(Collectors.toList());
    }

    public static List<SkillDto> toSkillDtoList(List<Skill> skills) {
        return skills.stream().map(DtoMapper::toDto).collect(Collectors.toList());
    }

    public static List<TestDto> toTestDtoList(List<Test> tests) {
        return tests.stream().map(DtoMapper::toDto).collect(Collectors.toList());
    }

    public static List<QuestionDto> toQuestionDtoList(List<Question> questions) {
        return questions.stream().map(DtoMapper::toDto).collect(Collectors.toList());
    }

    public static List<AssignmentDto> toAssignmentDtoList(List<Assignment> assignments) {
        return assignments.stream().map(DtoMapper::toDto).collect(Collectors.toList());
    }

    public static List<TestAttemptDto> toAttemptDtoList(List<TestAttempt> attempts) {
        return attempts.stream().map(DtoMapper::toDto).collect(Collectors.toList());
    }
}
