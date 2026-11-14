package com.proftest.server.network;

import com.proftest.server.model.User;
import com.proftest.server.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Управляет активными сессиями пользователей (токен → пользователь).
 * Паттерн Singleton.
 */
public class SessionManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private static volatile SessionManager instance;
    private final Map<String, Long> tokenToUserId = new ConcurrentHashMap<>();
    private final Map<String, User> tokenToUser = new ConcurrentHashMap<>();

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    public String createSession(User user) {
        String token = PasswordUtil.generateToken();
        tokenToUserId.put(token, user.getUserId());
        tokenToUser.put(token, user);
        logger.info("Session created for user: {}", user.getLogin());
        return token;
    }

    public Optional<User> getUserByToken(String token) {
        if (token == null) return Optional.empty();
        return Optional.ofNullable(tokenToUser.get(token));
    }

    public void invalidateToken(String token) {
        if (token != null) {
            User user = tokenToUser.remove(token);
            tokenToUserId.remove(token);
            if (user != null) logger.info("Session invalidated for user: {}", user.getLogin());
        }
    }

    public boolean isAuthenticated(String token) {
        return token != null && tokenToUser.containsKey(token);
    }

    public int getActiveSessionCount() {
        return tokenToUser.size();
    }
}
