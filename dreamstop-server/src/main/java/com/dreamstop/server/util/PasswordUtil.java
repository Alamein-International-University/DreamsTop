package com.dreamstop.server.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for hashing and verifying passwords securely using SHA-256.
 *
 * @author Omar ElSharkawy (@omarehab544)
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    /**
     * Hashes the given plain-text password using SHA-256.
     *
     * @param plainPassword the plain text password
     * @return lowercase hexadecimal string representation of the hash
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in JVM", e);
        }
    }

    /**
     * Verifies whether a plain-text password matches a stored hash.
     * Uses constant-time comparison to prevent timing attacks.
     *
     * @param plainPassword the plain text candidate password
     * @param storedHash the expected hexadecimal hash string
     * @return true if candidate matches stored hash, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }

        String candidateHash = hashPassword(plainPassword);
        return MessageDigest.isEqual(
                candidateHash.getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8)
        );
    }
}
