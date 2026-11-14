package com.proftest.server.service;

import com.proftest.common.dto.*;
import com.proftest.server.dao.*;
import com.proftest.server.model.*;
import com.proftest.server.scoring.ScoringStrategy;
import com.proftest.server.scoring.ScoringStrategyFactory;
import com.proftest.server.util.DtoMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TestingService extends BaseService {

    private final AssignmentDao assignmentDao = new AssignmentDao();
    private final TestAttemptDao attemptDao = new TestAttemptDao();
    private final UserAnswerDao answerDao = new UserAnswerDao();
    private final UserDao userDao = new UserDao();
    private final QuestionDao questionDao = new QuestionDao();
    private final TestDao testDao = new TestDao();

    public TestAttemptDto startAttempt(Long assignmentId, Long userId) {
        return inTransaction(() -> {
            Assignment assignment = assignmentDao.findById(assignmentId)
                    .orElseThrow(() -> new RuntimeException("Assignment not found"));
            User user = userDao.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

            long completedAttempts = attemptDao.countCompletedByAssignment(assignmentId);
            if (completedAttempts >= assignment.getAllowedAttempts()) {
                throw new RuntimeException("Превышено количество попыток");
            }
            if (assignment.getDeadline() != null && assignment.getDeadline().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Дедлайн истёк");
            }

            TestAttempt attempt = new TestAttempt(assignment, user);
            attemptDao.save(attempt);

            Optional<Test> testOpt = testDao.findByIdWithQuestions(assignment.getTest().getTestId());
            if (testOpt.isEmpty()) throw new RuntimeException("Test not found");
            Test test = testOpt.get();

            TestAttemptDto dto = DtoMapper.toDto(attempt);
            dto.setQuestions(test.getQuestions().stream()
                    .map(DtoMapper::toDtoForCandidate)
                    .collect(Collectors.toList()));
            dto.setPassingScore(test.getPassingScore());
            logger.info("Attempt started: assignmentId={}, userId={}", assignmentId, userId);
            return dto;
        });
    }

    public UserAnswerDto submitAnswer(Long attemptId, UserAnswerDto answerDto) {
        return inTransaction(() -> {
            TestAttempt attempt = attemptDao.findById(attemptId)
                    .orElseThrow(() -> new RuntimeException("Attempt not found"));
            if (attempt.getStatus() != TestAttempt.Status.IN_PROGRESS) {
                throw new RuntimeException("Попытка уже завершена");
            }
            Question question = questionDao.findById(answerDto.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question not found"));

            UserAnswer answer = new UserAnswer(attempt, question);
            if (answerDto.getSelectedOptionId() != null) {
                question.getOptions().stream()
                        .filter(o -> o.getOptionId().equals(answerDto.getSelectedOptionId()))
                        .findFirst()
                        .ifPresent(answer::setSelectedOption);
            }
            answer.setUserTextAnswer(answerDto.getUserTextAnswer());

            ScoringStrategy strategy = ScoringStrategyFactory.getStrategy(question.getQuestionType());
            int points = strategy.score(question, answer);
            answer.setPointsEarned(points);

            answerDao.save(answer);
            return DtoMapper.toDto(answer);
        });
    }

    public TestAttemptDto finishAttempt(Long attemptId) {
        return inTransaction(() -> {
            TestAttempt attempt = attemptDao.findByIdWithAnswers(attemptId)
                    .orElseThrow(() -> new RuntimeException("Attempt not found"));
            if (attempt.getStatus() != TestAttempt.Status.IN_PROGRESS) {
                throw new RuntimeException("Попытка уже завершена");
            }

            attempt.setEndTime(LocalDateTime.now());
            attempt.setStatus(TestAttempt.Status.COMPLETED);

            // MULTIPLE_CHOICE: individual answers scored 0 each;
            // award full question points only if selected options match ALL correct options exactly.
            Map<Long, List<UserAnswer>> multipleByQuestion = attempt.getAnswers().stream()
                    .filter(ua -> ua.getQuestion().getQuestionType() == com.proftest.server.model.Question.QuestionType.MULTIPLE_CHOICE)
                    .collect(Collectors.groupingBy(ua -> ua.getQuestion().getQuestionId()));

            int multiplePoints = 0;
            for (Map.Entry<Long, List<UserAnswer>> entry : multipleByQuestion.entrySet()) {
                com.proftest.server.model.Question q = entry.getValue().get(0).getQuestion();
                Set<Long> correctIds = q.getOptions().stream()
                        .filter(com.proftest.server.model.QuestionOption::isCorrect)
                        .map(com.proftest.server.model.QuestionOption::getOptionId)
                        .collect(Collectors.toSet());
                Set<Long> selectedIds = entry.getValue().stream()
                        .filter(ua -> ua.getSelectedOption() != null)
                        .map(ua -> ua.getSelectedOption().getOptionId())
                        .collect(Collectors.toSet());
                if (!correctIds.isEmpty() && correctIds.equals(selectedIds)) {
                    multiplePoints += q.getPoints() != null ? q.getPoints() : 1;
                }
            }

            int totalPoints = attempt.getAnswers().stream()
                    .mapToInt(ua -> ua.getPointsEarned() != null ? ua.getPointsEarned() : 0)
                    .sum() + multiplePoints;
            int maxPoints = attempt.getAssignment().getTest().getQuestions().stream()
                    .mapToInt(q -> q.getPoints() != null ? q.getPoints() : 1)
                    .sum();

            double scorePercent = maxPoints > 0 ? (double) totalPoints / maxPoints * 100.0 : 0.0;
            attempt.setScore(BigDecimal.valueOf(scorePercent));
            attempt.setPassed(scorePercent >= attempt.getAssignment().getTest().getPassingScore());

            attemptDao.update(attempt);
            logger.info("Attempt finished: id={}, score={}, passed={}", attemptId, scorePercent, attempt.getPassed());

            TestAttemptDto dto = DtoMapper.toDto(attempt);
            dto.setAnswers(attempt.getAnswers().stream().map(DtoMapper::toDto).collect(Collectors.toList()));
            return dto;
        });
    }

    public List<TestAttemptDto> getMyResults(Long userId) {
        return inTransaction(() -> DtoMapper.toAttemptDtoList(attemptDao.findByUser(userId)));
    }

    public List<TestAttemptDto> getAllResults() {
        return inTransaction(() -> DtoMapper.toAttemptDtoList(attemptDao.findAll()));
    }

    public List<TestAttemptDto> getResultsByTest(Long testId) {
        return inTransaction(() -> DtoMapper.toAttemptDtoList(attemptDao.findByTest(testId)));
    }

    public List<TestAttemptDto> getResultsByUser(Long userId) {
        return inTransaction(() -> DtoMapper.toAttemptDtoList(attemptDao.findByUser(userId)));
    }

    public Optional<TestAttemptDto> getAttemptResult(Long attemptId) {
        return inTransaction(() -> {
            Optional<TestAttempt> opt = attemptDao.findByIdWithAnswers(attemptId);
            if (opt.isEmpty()) return Optional.empty();
            TestAttempt ta = opt.get();
            TestAttemptDto dto = DtoMapper.toDto(ta);
            dto.setAnswers(ta.getAnswers().stream().map(DtoMapper::toDto).collect(Collectors.toList()));
            return Optional.of(dto);
        });
    }
}
