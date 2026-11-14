package com.proftest.server.service;

import com.proftest.common.dto.*;
import com.proftest.server.dao.*;
import com.proftest.server.model.*;
import com.proftest.server.util.DtoMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TestService extends BaseService {

    private final TestDao testDao = new TestDao();
    private final QuestionDao questionDao = new QuestionDao();
    private final SkillDao skillDao = new SkillDao();
    private final UserDao userDao = new UserDao();
    private final AssignmentDao assignmentDao = new AssignmentDao();

    public List<TestDto> getAllTests() {
        return inTransaction(() -> DtoMapper.toTestDtoList(testDao.findAllWithSkills()));
    }

    public void setTestSkills(Long testId, List<Long> skillIds) {
        inTransactionVoid(() -> {
            Test test = testDao.findByIdWithSkills(testId)
                    .orElseThrow(() -> new RuntimeException("Test not found: " + testId));
            test.getSkills().clear();
            if (skillIds != null) {
                for (Long skillId : skillIds) {
                    skillDao.findById(skillId).ifPresent(s -> test.getSkills().add(s));
                }
            }
            testDao.update(test);
            logger.info("Skills updated for test {}: {}", testId, skillIds);
        });
    }

    public Optional<TestDto> getTestWithQuestions(Long testId) {
        return inTransaction(() -> {
            Optional<Test> testOpt = testDao.findByIdWithQuestions(testId);
            if (testOpt.isEmpty()) return Optional.empty();
            Test t = testOpt.get();
            TestDto dto = DtoMapper.toDto(t);
            dto.setQuestions(t.getQuestions().stream().map(DtoMapper::toDto).collect(Collectors.toList()));
            dto.setSkills(t.getSkills().stream().map(DtoMapper::toDto).collect(Collectors.toList()));
            return Optional.of(dto);
        });
    }

    public TestDto createTest(TestDto dto, Long creatorId) {
        return inTransaction(() -> {
            Test test = new Test(dto.getTitle(), dto.getTimeLimitMinutes(), dto.getPassingScore());
            test.setDescription(dto.getDescription());
            test.setMaxAttempts(dto.getMaxAttempts() != null ? dto.getMaxAttempts() : 1);
            test.setActive(dto.isActive());
            userDao.findById(creatorId).ifPresent(test::setCreator);
            testDao.save(test);
            logger.info("Test created: {}", dto.getTitle());
            return DtoMapper.toDto(test);
        });
    }

    public TestDto updateTest(TestDto dto) {
        return inTransaction(() -> {
            Test test = testDao.findById(dto.getTestId())
                    .orElseThrow(() -> new RuntimeException("Test not found: " + dto.getTestId()));
            test.setTitle(dto.getTitle());
            test.setDescription(dto.getDescription());
            test.setTimeLimitMinutes(dto.getTimeLimitMinutes());
            test.setMaxAttempts(dto.getMaxAttempts());
            test.setPassingScore(dto.getPassingScore());
            test.setActive(dto.isActive());
            return DtoMapper.toDto(testDao.update(test));
        });
    }

    public void deleteTest(Long testId) {
        inTransactionVoid(() -> testDao.deleteById(testId));
    }

    public void toggleTestActive(Long testId) {
        inTransactionVoid(() -> {
            testDao.findById(testId).ifPresent(t -> {
                boolean wasActive = t.isActive();
                t.setActive(!t.isActive());
                testDao.update(t);
                if (wasActive) {
                    int removed = assignmentDao.deleteByTestId(testId);
                    logger.info("Test {} deactivated — {} assignment(s) removed", testId, removed);
                }
            });
        });
    }

    public void addQuestionToTest(Long testId, Long questionId) {
        inTransactionVoid(() -> {
            Test test = testDao.findByIdWithQuestions(testId).orElseThrow();
            Question q = questionDao.findById(questionId).orElseThrow();
            boolean alreadyAdded = test.getQuestions().stream()
                    .anyMatch(existing -> existing.getQuestionId().equals(questionId));
            if (!alreadyAdded) {
                test.getQuestions().add(q);
            }
        });
    }

    public void removeQuestionFromTest(Long testId, Long questionId) {
        inTransactionVoid(() -> {
            Test test = testDao.findByIdWithQuestions(testId).orElseThrow();
            test.getQuestions().removeIf(q -> q.getQuestionId().equals(questionId));
        });
    }

    public List<QuestionDto> getAllQuestions() {
        return inTransaction(() -> DtoMapper.toQuestionDtoList(questionDao.findAll()));
    }

    public QuestionDto createQuestion(QuestionDto dto, Long creatorId) {
        return inTransaction(() -> {
            Question q = new Question(dto.getText(),
                    Question.QuestionType.valueOf(dto.getQuestionType()),
                    dto.getPoints() != null ? dto.getPoints() : 1);
            q.setDifficulty(dto.getDifficulty());
            q.setTopic(dto.getTopic());
            userDao.findById(creatorId).ifPresent(q::setCreatedBy);
            if (dto.getOptions() != null) {
                for (QuestionOptionDto optDto : dto.getOptions()) {
                    QuestionOption opt = new QuestionOption(q, optDto.getOptionText(), optDto.isCorrect());
                    q.addOption(opt);
                }
            }
            questionDao.save(q);
            logger.info("Question created: {}", dto.getText());
            return DtoMapper.toDto(q);
        });
    }

    public QuestionDto updateQuestion(QuestionDto dto) {
        return inTransaction(() -> {
            Question q = questionDao.findById(dto.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question not found: " + dto.getQuestionId()));
            q.setText(dto.getText());
            q.setQuestionType(Question.QuestionType.valueOf(dto.getQuestionType()));
            q.setPoints(dto.getPoints() != null ? dto.getPoints() : 1);
            q.setDifficulty(dto.getDifficulty());
            q.setTopic(dto.getTopic());
            q.getOptions().clear();
            if (dto.getOptions() != null) {
                for (QuestionOptionDto optDto : dto.getOptions()) {
                    QuestionOption opt = new QuestionOption(q, optDto.getOptionText(), optDto.isCorrect());
                    q.addOption(opt);
                }
            }
            return DtoMapper.toDto(questionDao.update(q));
        });
    }

    public void deleteQuestion(Long questionId) {
        inTransactionVoid(() -> questionDao.deleteById(questionId));
    }
}
