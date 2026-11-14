package com.proftest.server.service;

import com.proftest.common.dto.AssignmentDto;
import com.proftest.server.dao.*;
import com.proftest.server.model.*;
import com.proftest.server.util.DtoMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class AssignmentService extends BaseService {

    private static final DateTimeFormatter FMT_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FMT_DATETIME_SHORT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AssignmentDao assignmentDao = new AssignmentDao();
    private final TestDao testDao = new TestDao();
    private final UserDao userDao = new UserDao();

    public List<AssignmentDto> getAllAssignments() {
        return inTransaction(() -> DtoMapper.toAssignmentDtoList(assignmentDao.findAll()));
    }

    public List<AssignmentDto> getMyAssignments(Long userId) {
        return inTransaction(() -> {
            List<Assignment> assignments = assignmentDao.findByUser(userId);
            List<AssignmentDto> dtos = DtoMapper.toAssignmentDtoList(assignments);
            for (int i = 0; i < assignments.size(); i++) {
                long used = assignmentDao.countAttemptsByAssignment(assignments.get(i).getAssignmentId());
                dtos.get(i).setAttemptsUsed((int) used);
                Assignment a = assignments.get(i);
                String status = "PENDING";
                if (used >= a.getAllowedAttempts()) status = "COMPLETED";
                else if (a.getDeadline() != null && a.getDeadline().isBefore(LocalDateTime.now())) status = "EXPIRED";
                dtos.get(i).setStatus(status);
            }
            return dtos;
        });
    }

    public AssignmentDto createAssignment(AssignmentDto dto, Long assignedById) {
        return inTransaction(() -> {
            Test test = testDao.findById(dto.getTestId())
                    .orElseThrow(() -> new RuntimeException("Test not found"));
            if (!test.isActive()) {
                throw new RuntimeException("Нельзя назначить неактивный тест");
            }
            User user = userDao.findByIdWithDetails(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            User assignedBy = userDao.findByIdWithDetails(assignedById)
                    .orElseThrow(() -> new RuntimeException("Assigner not found"));
            Assignment a = new Assignment(test, user, assignedBy);
            a.setAllowedAttempts(dto.getAllowedAttempts() != null ? dto.getAllowedAttempts() : test.getMaxAttempts());
            if (dto.getDeadline() != null && !dto.getDeadline().isBlank()) {
                LocalDateTime deadline = parseDeadline(dto.getDeadline().trim());
                if (deadline != null) {
                    a.setDeadline(deadline);
                    logger.info("Assignment deadline set to: {}", deadline);
                } else {
                    logger.warn("Could not parse deadline string: '{}'", dto.getDeadline());
                }
            }
            assignmentDao.save(a);
            logger.info("Assignment created: test={} user={}", test.getTitle(), user.getLogin());
            return DtoMapper.toDto(a);
        });
    }

    /**
     * Парсит строку дедлайна в нескольких форматах.
     * Возвращает null если строка не распознана.
     */
    private LocalDateTime parseDeadline(String value) {
        // "yyyy-MM-dd HH:mm:ss"
        try { return LocalDateTime.parse(value, FMT_DATETIME); } catch (DateTimeParseException ignored) {}
        // "yyyy-MM-dd HH:mm"
        try { return LocalDateTime.parse(value, FMT_DATETIME_SHORT); } catch (DateTimeParseException ignored) {}
        // "yyyy-MM-dd" → конец дня
        try { return LocalDate.parse(value, FMT_DATE).atTime(23, 59, 59); } catch (DateTimeParseException ignored) {}
        return null;
    }

    public void deleteAssignment(Long assignmentId) {
        inTransactionVoid(() -> assignmentDao.deleteById(assignmentId));
    }

    public Optional<Assignment> findById(Long assignmentId) {
        return inTransaction(() -> {
            Optional<Assignment> a = assignmentDao.findById(assignmentId);
            a.ifPresent(assignment -> {
                assignment.getTest().getTitle();
                assignment.getUser().getLogin();
            });
            return a;
        });
    }

    public long countAttempts(Long assignmentId) {
        return inTransaction(() -> assignmentDao.countAttemptsByAssignment(assignmentId));
    }
}
