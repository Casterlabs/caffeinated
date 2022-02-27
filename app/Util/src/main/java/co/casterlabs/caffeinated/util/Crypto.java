package co.casterlabs.caffeinated.util;

import java.security.SecureRandom;

public class Crypto {
    private static final char[] SAFE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private static final int KEY_SIZE = 64;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static char[] generateSecureRandomKey() {
        char[] result = new char[KEY_SIZE];

        for (int i = 0; i < KEY_SIZE; i++) {
            result[i] = SAFE_CHARS[SECURE_RANDOM.nextInt(SAFE_CHARS.length)];
        }

        return result;
    }

}
