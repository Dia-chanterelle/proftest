package com.proftest.server.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.proftest.common.dto.*;
import com.proftest.common.protocol.RequestType;
import com.proftest.server.model.User;
import com.proftest.server.service.*;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

/**
 * Диспетчер запросов — маршрутизирует запросы к нужным сервисам.
 */
public class RequestDispatcher {

    private final Gson gson = new GsonBuilder().create();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final AuthService authService = new AuthService();
    private final UserService userService = new UserService();
    private final SkillService skillService = new SkillService();
    private final TestService testService = new TestService();
    private final AssignmentService assignmentService = new AssignmentService();
    private final TestingService testingService = new TestingService();

    public Response dispatch(Request request) {
        try {
            RequestType type = request.getType();

            if (type == RequestType.PING) return Response.ok("pong");
            if (type == RequestType.LOGIN) return handleLogin(request);
            if (type == RequestType.REGISTER) return handleRegister(request);

            // Все остальные запросы требуют аутентификации
            Optional<User> userOpt = sessionManager.getUserByToken(request.getToken());
            if (userOpt.isEmpty()) return Response.unauthorized("Необходима авторизация");
            User currentUser = userOpt.get();

            return switch (type) {
                case LOGOUT -> handleLogout(request);
                case CHANGE_PASSWORD -> handleChangePassword(request, currentUser);

                case GET_ALL_USERS -> requireAnyRole(currentUser, new String[]{"ADMIN", "HR_MANAGER"}, () -> handleGetAllUsers());
                case TOGGLE_USER_ACTIVE -> requireRole(currentUser, "ADMIN", () -> handleToggleUserActive(request));
                case SET_USER_ROLE -> requireRole(currentUser, "ADMIN", () -> handleSetUserRole(request));
                case DELETE_USER -> requireRole(currentUser, "ADMIN", () -> handleDeleteUser(request));
                case GET_ALL_ROLES -> handleGetAllRoles();

                case GET_ALL_SKILLS -> handleGetAllSkills();
                case CREATE_SKILL -> requireAnyRole(currentUser, new String[]{"ADMIN", "HR_MANAGER"}, () -> handleCreateSkill(request));
                case UPDATE_SKILL -> requireAnyRole(currentUser, new String[]{"ADMIN", "HR_MANAGER"}, () -> handleUpdateSkill(request));
                case DELETE_SKILL -> requireRole(currentUser, "ADMIN", () -> handleDeleteSkill(request));

                case GET_ALL_TESTS -> handleGetAllTests();
                case GET_TEST_BY_ID -> handleGetTestById(request);
                case CREATE_TEST -> requireAnyRole(currentUser, new String[]{"ADMIN", "HR_MANAGER"}, () -> handleCreateTest(request, currentUser));
                case UPDATE_TEST -> requireAnyRole(currentUser, new String[]{"ADMIN", "HR_MANAGER"}, () -> handleUpdateTest(request));
                case DELETE_TEST -> requireRole(currentUser, "ADMIN", () -> handleDeleteTest(request));
                case TOGGLE_TEST_ACTIVE -> requireAnyRole(currentUser, new String[]{"ADMIN", "HR_MANAGER"}, () -> handleToggleTestActive(request));
                case SET_TEST_SKILLS -> requireAnyRole(currentUser, new String[]{"ADMIN", "HR_MANAGER"}, () -> handleSetTestSkills(request));
                case ADD_QUESTION_TO_TEST -> requireAnyRole(currentUser, new String[]{"ADMIN", "HR_MANAGER"}, () -> handleAddQuestionToTest(request));
                case REMOVE_QUESTION_FROM_TEST -> requireAnyRole(currentUser, new String[]{"ADMIN", "HR_MANAGER"}, () -> handleRemoveQuestionFromTest(request));

                case GET_ALL_QUESTIONS -> handleGetAllQuestions();
                case CREATE_QUESTION -> requireAnyRole(currentUser, new String[]{"ADMIN", "HR_MANAGER"}, () -> handleCreateQuestion(request, currentUser));
                case UPDATE_QUESTION -> requireAnyRole(currentUser, new String[]{"ADMIN", "HR_MANAGER"}, () -> handleUpdateQuestion(request));
                case DELETE_QUESTION -> requireAnyRole(currentUser, new String[]{"ADMIN", "HR_MANAGER"}, () -> handleDeleteQuestion(request));

                case GET_ALL_ASSIGNMENTS -> requireAnyRole(currentUser, new String[]{"ADMIN", "HR_MANAGER"}, () -> handleGetAllAssignments());
                case GET_MY_ASSIGNMENTS -> handleGetMyAssignments(currentUser);
                case CREATE_ASSIGNMENT -> requireAnyRole(currentUser, new String[]{"ADMIN", "HR_MANAGER"}, () -> handleCreateAssignment(request, currentUser));
                case DELETE_ASSIGNMENT -> requireAnyRole(currentUser, new String[]{"ADMIN", "HR_MANAGER"}, () -> handleDeleteAssignment(request));

                case START_ATTEMPT -> handleStartAttempt(request, currentUser);
                case SUBMIT_ANSWER -> handleSubmitAnswer(request);
                case FINISH_ATTEMPT -> handleFinishAttempt(request);
                case GET_ATTEMPT_RESULT -> handleGetAttemptResult(request);

                case GET_MY_RESULTS -> handleGetMyResults(currentUser);
                case GET_ALL_RESULTS -> requireAnyRole(currentUser, new String[]{"ADMIN", "HR_MANAGER"}, () -> handleGetAllResults());
                case GET_REPORT_BY_TEST -> requireAnyRole(currentUser, new String[]{"ADMIN", "HR_MANAGER"}, () -> handleGetReportByTest(request));
                case GET_REPORT_BY_USER -> requireAnyRole(currentUser, new String[]{"ADMIN", "HR_MANAGER"}, () -> handleGetReportByUser(request));

                default -> Response.error("Неизвестный тип запроса: " + type);
            };
        } catch (RuntimeException e) {
            return Response.error(e.getMessage());
        } catch (Exception e) {
            return Response.error("Внутренняя ошибка сервера");
        }
    }

    private Response requireRole(User user, String role, java.util.concurrent.Callable<Response> action) {
        if (!user.hasRole(role)) return Response.forbidden();
        try { return action.call(); } catch (Exception e) { return Response.error(e.getMessage()); }
    }

    private Response requireAnyRole(User user, String[] roles, java.util.concurrent.Callable<Response> action) {
        for (String role : roles) { if (user.hasRole(role)) {
            try { return action.call(); } catch (Exception e) { return Response.error(e.getMessage()); }
        }}
        return Response.forbidden();
    }

    private Response handleLogin(Request req) {
        LoginDto dto = gson.fromJson(req.getPayload(), LoginDto.class);
        AuthResultDto result = authService.login(dto);
        return Response.ok(gson.toJson(result));
    }

    private Response handleLogout(Request req) {
        authService.logout(req.getToken());
        return Response.ok("Выход выполнен");
    }

    private Response handleRegister(Request req) {
        RegisterDto dto = gson.fromJson(req.getPayload(), RegisterDto.class);
        UserDto result = authService.register(dto);
        return Response.ok(gson.toJson(result));
    }

    private Response handleChangePassword(Request req, User user) {
        Type t = new TypeToken<java.util.Map<String, String>>(){}.getType();
        java.util.Map<String, String> map = gson.fromJson(req.getPayload(), t);
        authService.changePassword(user.getUserId(), map.get("oldPassword"), map.get("newPassword"));
        return Response.ok("Пароль изменён");
    }

    private Response handleGetAllUsers() {
        return Response.ok(gson.toJson(userService.getAllUsers()));
    }

    private Response handleToggleUserActive(Request req) {
        Long id = gson.fromJson(req.getPayload(), Long.class);
        userService.toggleUserActive(id);
        return Response.ok("Статус изменён");
    }

    private Response handleSetUserRole(Request req) {
        Type t = new TypeToken<java.util.Map<String, String>>(){}.getType();
        java.util.Map<String, String> map = gson.fromJson(req.getPayload(), t);
        userService.setUserRole(Long.parseLong(map.get("userId")), map.get("role"));
        return Response.ok("Роль назначена");
    }

    private Response handleDeleteUser(Request req) {
        Long id = gson.fromJson(req.getPayload(), Long.class);
        userService.deleteUser(id);
        return Response.ok("Пользователь удалён");
    }

    private Response handleGetAllRoles() {
        return Response.ok(gson.toJson(List.of("ADMIN", "HR_MANAGER", "CANDIDATE", "EMPLOYEE")));
    }

    private Response handleGetAllSkills() {
        return Response.ok(gson.toJson(skillService.getAllSkills()));
    }

    private Response handleCreateSkill(Request req) {
        SkillDto dto = gson.fromJson(req.getPayload(), SkillDto.class);
        return Response.ok(gson.toJson(skillService.createSkill(dto)));
    }

    private Response handleUpdateSkill(Request req) {
        SkillDto dto = gson.fromJson(req.getPayload(), SkillDto.class);
        return Response.ok(gson.toJson(skillService.updateSkill(dto)));
    }

    private Response handleDeleteSkill(Request req) {
        Long id = gson.fromJson(req.getPayload(), Long.class);
        skillService.deleteSkill(id);
        return Response.ok("Навык удалён");
    }

    private Response handleGetAllTests() {
        return Response.ok(gson.toJson(testService.getAllTests()));
    }

    private Response handleGetTestById(Request req) {
        Long id = gson.fromJson(req.getPayload(), Long.class);
        return testService.getTestWithQuestions(id)
                .map(dto -> Response.ok(gson.toJson(dto)))
                .orElse(Response.notFound("Тест не найден"));
    }

    private Response handleCreateTest(Request req, User user) {
        TestDto dto = gson.fromJson(req.getPayload(), TestDto.class);
        return Response.ok(gson.toJson(testService.createTest(dto, user.getUserId())));
    }

    private Response handleUpdateTest(Request req) {
        TestDto dto = gson.fromJson(req.getPayload(), TestDto.class);
        return Response.ok(gson.toJson(testService.updateTest(dto)));
    }

    private Response handleDeleteTest(Request req) {
        Long id = gson.fromJson(req.getPayload(), Long.class);
        testService.deleteTest(id);
        return Response.ok("Тест удалён");
    }

    private Response handleToggleTestActive(Request req) {
        Long id = gson.fromJson(req.getPayload(), Long.class);
        testService.toggleTestActive(id);
        return Response.ok("Статус теста изменён");
    }

    private Response handleSetTestSkills(Request req) {
        TestDto dto = gson.fromJson(req.getPayload(), TestDto.class);
        List<Long> skillIds = dto.getSkills() != null
                ? dto.getSkills().stream()
                      .filter(s -> s.getSkillId() != null)
                      .map(com.proftest.common.dto.SkillDto::getSkillId)
                      .collect(java.util.stream.Collectors.toList())
                : new java.util.ArrayList<>();
        testService.setTestSkills(dto.getTestId(), skillIds);
        return Response.ok("Навыки теста обновлены");
    }

    private Response handleAddQuestionToTest(Request req) {
        Type t = new TypeToken<java.util.Map<String, Long>>(){}.getType();
        java.util.Map<String, Long> map = gson.fromJson(req.getPayload(), t);
        testService.addQuestionToTest(map.get("testId"), map.get("questionId"));
        return Response.ok("Вопрос добавлен в тест");
    }

    private Response handleRemoveQuestionFromTest(Request req) {
        Type t = new TypeToken<java.util.Map<String, Long>>(){}.getType();
        java.util.Map<String, Long> map = gson.fromJson(req.getPayload(), t);
        testService.removeQuestionFromTest(map.get("testId"), map.get("questionId"));
        return Response.ok("Вопрос удалён из теста");
    }

    private Response handleGetAllQuestions() {
        return Response.ok(gson.toJson(testService.getAllQuestions()));
    }

    private Response handleCreateQuestion(Request req, User user) {
        QuestionDto dto = gson.fromJson(req.getPayload(), QuestionDto.class);
        return Response.ok(gson.toJson(testService.createQuestion(dto, user.getUserId())));
    }

    private Response handleUpdateQuestion(Request req) {
        QuestionDto dto = gson.fromJson(req.getPayload(), QuestionDto.class);
        return Response.ok(gson.toJson(testService.updateQuestion(dto)));
    }

    private Response handleDeleteQuestion(Request req) {
        Long id = gson.fromJson(req.getPayload(), Long.class);
        testService.deleteQuestion(id);
        return Response.ok("Вопрос удалён");
    }

    private Response handleGetAllAssignments() {
        return Response.ok(gson.toJson(assignmentService.getAllAssignments()));
    }

    private Response handleGetMyAssignments(User user) {
        return Response.ok(gson.toJson(assignmentService.getMyAssignments(user.getUserId())));
    }

    private Response handleCreateAssignment(Request req, User user) {
        AssignmentDto dto = gson.fromJson(req.getPayload(), AssignmentDto.class);
        return Response.ok(gson.toJson(assignmentService.createAssignment(dto, user.getUserId())));
    }

    private Response handleDeleteAssignment(Request req) {
        Long id = gson.fromJson(req.getPayload(), Long.class);
        assignmentService.deleteAssignment(id);
        return Response.ok("Назначение удалено");
    }

    private Response handleStartAttempt(Request req, User user) {
        Long assignmentId = gson.fromJson(req.getPayload(), Long.class);
        return Response.ok(gson.toJson(testingService.startAttempt(assignmentId, user.getUserId())));
    }

    private Response handleSubmitAnswer(Request req) {
        UserAnswerDto dto = gson.fromJson(req.getPayload(), UserAnswerDto.class);
        return Response.ok(gson.toJson(testingService.submitAnswer(dto.getAttemptId(), dto)));
    }

    private Response handleFinishAttempt(Request req) {
        Long attemptId = gson.fromJson(req.getPayload(), Long.class);
        return Response.ok(gson.toJson(testingService.finishAttempt(attemptId)));
    }

    private Response handleGetAttemptResult(Request req) {
        Long id = gson.fromJson(req.getPayload(), Long.class);
        return testingService.getAttemptResult(id)
                .map(dto -> Response.ok(gson.toJson(dto)))
                .orElse(Response.notFound("Попытка не найдена"));
    }

    private Response handleGetMyResults(User user) {
        return Response.ok(gson.toJson(testingService.getMyResults(user.getUserId())));
    }

    private Response handleGetAllResults() {
        return Response.ok(gson.toJson(testingService.getAllResults()));
    }

    private Response handleGetReportByTest(Request req) {
        Long testId = gson.fromJson(req.getPayload(), Long.class);
        return Response.ok(gson.toJson(testingService.getResultsByTest(testId)));
    }

    private Response handleGetReportByUser(Request req) {
        Long userId = gson.fromJson(req.getPayload(), Long.class);
        return Response.ok(gson.toJson(testingService.getResultsByUser(userId)));
    }
}
