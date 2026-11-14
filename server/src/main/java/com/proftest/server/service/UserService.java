package com.proftest.server.service;

import com.proftest.common.dto.UserDto;
import com.proftest.server.dao.RoleDao;
import com.proftest.server.dao.UserDao;
import com.proftest.server.model.User;
import com.proftest.server.util.DtoMapper;
import com.proftest.server.util.PasswordUtil;

import java.util.List;
import java.util.Optional;

public class UserService extends BaseService {

    private final UserDao userDao = new UserDao();
    private final RoleDao roleDao = new RoleDao();

    public List<UserDto> getAllUsers() {
        return inTransaction(() -> DtoMapper.toUserDtoList(userDao.findAll()));
    }

    public Optional<UserDto> getUserById(Long id) {
        return inTransaction(() -> userDao.findByIdWithDetails(id).map(DtoMapper::toDto));
    }

    public void toggleUserActive(Long userId) {
        inTransactionVoid(() -> {
            userDao.findByIdWithDetails(userId).ifPresent(user -> {
                user.setActive(!user.isActive());
                userDao.update(user);
                logger.info("User {} active status toggled to {}", user.getLogin(), user.isActive());
            });
        });
    }

    public void setUserRole(Long userId, String roleName) {
        inTransactionVoid(() -> {
            Optional<User> userOpt = userDao.findByIdWithDetails(userId);
            if (userOpt.isEmpty()) throw new RuntimeException("User not found: " + userId);
            User user = userOpt.get();
            user.getRoles().clear();
            roleDao.findByName(roleName).ifPresent(r -> user.getRoles().add(r));
            userDao.update(user);
            logger.info("Role {} assigned to user {}", roleName, user.getLogin());
        });
    }

    public void addRoleToUser(Long userId, String roleName) {
        inTransactionVoid(() -> {
            Optional<User> userOpt = userDao.findByIdWithDetails(userId);
            if (userOpt.isEmpty()) throw new RuntimeException("User not found: " + userId);
            User user = userOpt.get();
            roleDao.findByName(roleName).ifPresent(r -> user.getRoles().add(r));
            userDao.update(user);
        });
    }

    public void deleteUser(Long userId) {
        inTransactionVoid(() -> userDao.deleteById(userId));
    }

    public void resetPassword(Long userId, String newPassword) {
        inTransactionVoid(() -> {
            userDao.findByIdWithDetails(userId).ifPresent(user -> {
                user.setPasswordHash(PasswordUtil.hash(newPassword));
                userDao.update(user);
            });
        });
    }

    public List<UserDto> getUsersByRole(String roleName) {
        return inTransaction(() -> DtoMapper.toUserDtoList(userDao.findByRole(roleName)));
    }
}
