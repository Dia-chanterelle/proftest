package com.proftest.server.service;

import com.proftest.common.dto.*;
import com.proftest.server.dao.PersonDao;
import com.proftest.server.dao.RoleDao;
import com.proftest.server.dao.UserDao;
import com.proftest.server.model.*;
import com.proftest.server.network.SessionManager;
import com.proftest.server.util.DtoMapper;
import com.proftest.server.util.PasswordUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class AuthService extends BaseService {

    private final UserDao userDao = new UserDao();
    private final PersonDao personDao = new PersonDao();
    private final RoleDao roleDao = new RoleDao();
    private final SessionManager sessionManager = SessionManager.getInstance();

    public AuthResultDto login(LoginDto loginDto) {
        return inTransaction(() -> {
            Optional<User> userOpt = userDao.findByLogin(loginDto.getLogin());
            if (userOpt.isEmpty()) throw new RuntimeException("Неверный логин или пароль");
            User user = userOpt.get();
            if (!user.isActive()) throw new RuntimeException("Учётная запись отключена");
            if (!PasswordUtil.verify(loginDto.getPassword(), user.getPasswordHash())) {
                throw new RuntimeException("Неверный логин или пароль");
            }
            user.setLastLogin(LocalDateTime.now());
            userDao.update(user);
            String token = sessionManager.createSession(user);
            logger.info("User logged in: {}", user.getLogin());
            return new AuthResultDto(token, DtoMapper.toDto(user));
        });
    }

    public void logout(String token) {
        sessionManager.invalidateToken(token);
    }

    public UserDto register(RegisterDto dto) {
        return inTransaction(() -> {
            if (userDao.existsByLogin(dto.getLogin())) {
                throw new RuntimeException("Логин '" + dto.getLogin() + "' уже занят");
            }
            if (personDao.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email '" + dto.getEmail() + "' уже используется");
            }
            Person person = new Person(dto.getFullName(), dto.getEmail());
            person.setPhone(dto.getPhone());
            person.setAddress(dto.getAddress());
            person.setDepartment(dto.getDepartment());
            person.setPosition(dto.getPosition());
            if (dto.getDateOfBirth() != null && !dto.getDateOfBirth().isBlank()) {
                try {
                    person.setDateOfBirth(LocalDate.parse(dto.getDateOfBirth()));
                } catch (DateTimeParseException ignored) {}
            }
            personDao.save(person);

            String hash = PasswordUtil.hash(dto.getPassword());
            User user = new User(person, dto.getLogin(), hash);
            userDao.save(user);

            String roleName = (dto.getRole() != null && !dto.getRole().isBlank()) ? dto.getRole() : "CANDIDATE";
            roleDao.findByName(roleName).ifPresent(r -> user.getRoles().add(r));
            userDao.update(user);

            logger.info("User registered: {}", dto.getLogin());
            return DtoMapper.toDto(user);
        });
    }

    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        return inTransaction(() -> {
            Optional<User> userOpt = userDao.findByIdWithDetails(userId);
            if (userOpt.isEmpty()) return false;
            User user = userOpt.get();
            if (!PasswordUtil.verify(oldPassword, user.getPasswordHash())) {
                throw new RuntimeException("Неверный текущий пароль");
            }
            user.setPasswordHash(PasswordUtil.hash(newPassword));
            userDao.update(user);
            return true;
        });
    }

    public void initDefaultAdmin() {
        try {
            inTransactionVoid(() -> {
                if (!userDao.existsByLogin("admin")) {
                    for (String rn : new String[]{"ADMIN", "HR_MANAGER", "CANDIDATE", "EMPLOYEE"}) {
                        if (roleDao.findByName(rn).isEmpty()) {
                            roleDao.save(new Role(rn));
                        }
                    }
                    Person p = new Person("Administrator", "admin@proftest.local");
                    p.setDepartment("IT");
                    p.setPosition("System Administrator");
                    personDao.save(p);
                    User admin = new User(p, "admin", PasswordUtil.hash("admin123"));
                    userDao.save(admin);
                    roleDao.findByName("ADMIN").ifPresent(r -> admin.getRoles().add(r));
                    userDao.update(admin);
                    logger.info("Default admin created: admin / admin123");
                }
            });
        } catch (Exception e) {
            logger.warn("Could not init default admin (may already exist): {}", e.getMessage());
        }
    }
}
