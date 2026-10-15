package com.tasktier.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utility for password hashing and verification using PBKDF2WithHmacSHA256.
 * Adheres strictly to modern cryptographic standards using only Core Java libraries.
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 100_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_LENGTH_BITS = 256;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordHasher() {
        // Prevent instantiation
    }

    /**
     * Hashes a raw plaintext password using PBKDF2 with a randomly generated salt.
     *
     * @param plainPassword plaintext password
     * @return salt and hash encoded as salt:hash
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        byte[] salt = new byte[SALT_BYTES];
        SECURE_RANDOM.nextBytes(salt);

        byte[] hash = pbkdf2(plainPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);

        String saltEncoded = Base64.getEncoder().encodeToString(salt);
        String hashEncoded = Base64.getEncoder().encodeToString(hash);

        return saltEncoded + ":" + hashEncoded;
    }

    /**
     * Verifies a raw plaintext password against a stored salt:hash string.
     *
     * @param plainPassword candidate password
     * @param storedHash stored salt:hash string
     * @return true if password matches, false otherwise
     */
    public static boolean verify(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null || !storedHash.contains(":")) {
            return false;
        }

        String[] parts = storedHash.split(":", 2);
        if (parts.length != 2) {
            return false;
        }

        byte[] salt;
        byte[] expectedHash;
        try {
            salt = Base64.getDecoder().decode(parts[0]);
            expectedHash = Base64.getDecoder().decode(parts[1]);
        } catch (IllegalArgumentException exception) {
            AppLogger.warn(PasswordHasher.class, "Invalid Base64 string in stored password hash");
            return false;
        }

        byte[] actualHash = pbkdf2(plainPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);

        // Constant-time comparison to prevent timing attacks
        return slowEquals(expectedHash, actualHash);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
            AppLogger.error(PasswordHasher.class, "PBKDF2 hashing failed", exception);
            throw new IllegalStateException("Could not hash password", exception);
        }
    }

    private static boolean slowEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < a.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
