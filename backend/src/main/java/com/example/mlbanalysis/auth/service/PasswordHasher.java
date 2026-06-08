package com.example.mlbanalysis.auth.service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasher {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 185_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_BYTES = 16;
    private final SecureRandom secureRandom = new SecureRandom();

    public String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        secureRandom.nextBytes(salt);
        byte[] derived = derive(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        return String.join("$", "pbkdf2-sha256", String.valueOf(ITERATIONS), Base64.getEncoder().encodeToString(salt), Base64.getEncoder().encodeToString(derived));
    }

    public boolean matches(String password, String encoded) {
        String[] parts = encoded.split("\\$");
        if (parts.length != 4 || !"pbkdf2-sha256".equals(parts[0])) return false;
        int iterations = Integer.parseInt(parts[1]);
        byte[] salt = Base64.getDecoder().decode(parts[2]);
        byte[] expected = Base64.getDecoder().decode(parts[3]);
        byte[] actual = derive(password.toCharArray(), salt, iterations, expected.length * 8);
        return constantTimeEquals(expected, actual);
    }

    private byte[] derive(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(new PBEKeySpec(password, salt, iterations, keyLength)).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
            throw new IllegalStateException("Password hashing is unavailable", exception);
        }
    }

    private boolean constantTimeEquals(byte[] expected, byte[] actual) {
        if (expected.length != actual.length) return false;
        int result = 0;
        for (int i = 0; i < expected.length; i++) result |= expected[i] ^ actual[i];
        return result == 0;
    }
}
