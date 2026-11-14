package com.proftest.server.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {}

    public static String hash(String password) {
        try {
            byte[] salt = new byte[16];
            RANDOM.nextBytes(salt);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            String saltStr = Base64.getEncoder().encodeToString(salt);
            String hashStr = Base64.getEncoder().encodeToString(hashedBytes);
            return saltStr + ":" + hashStr;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    public static boolean verify(String password, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 2) return false;
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] actualHash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }

    public static String generateToken() {
        byte[] tokenBytes = new byte[32];
        RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
