package com.example.linktosync.utils;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {

    // Secret key for encryption/decryption (32 bytes for AES-256)
    private static final String SECRET_KEY = "3c2a5f27ebf6a0b0d48799d4b15f3cfa"; // 32 bytes (16 characters in hex)

    // AES encryption
    public static String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec secretKey = new SecretKeySpec(getKeyBytes(SECRET_KEY), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // AES decryption
    public static String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec secretKey = new SecretKeySpec(getKeyBytes(SECRET_KEY), "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }

    // Decode JWT
    public static void decodeJWT(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token");
        }

        // Decode header
        String header = new String(Base64.getUrlDecoder().decode(parts[0]));
        System.out.println("Header: " + header);

        // Decode payload
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
        System.out.println("Payload: " + payload);

        // Signature is not used here for decoding, but you can validate it as needed
        String signature = parts[2];
        System.out.println("Signature: " + signature);
    }

    // Convert hex string to byte array
    private static byte[] getKeyBytes(String key) {
        int len = key.length();
        byte[] result = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            result[i / 2] = (byte) ((Character.digit(key.charAt(i), 16) << 4)
                                 + Character.digit(key.charAt(i + 1), 16));
        }
        return result;
    }
}
