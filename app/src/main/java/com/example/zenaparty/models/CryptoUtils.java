package com.example.zenaparty.models;
import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class CryptoUtils {

    // Metodo per derivare la chiave usando password e salt
    private static SecretKeySpec generateKey(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 100000, 256);
        byte[] secretKey = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(secretKey, "AES");
    }

    public static String decrypt(String encryptedMessage, String password, byte[] salt) throws Exception {
        // Decodifica il messaggio crittografato
        byte[] decodedMessage = Base64.decode(encryptedMessage, Base64.DEFAULT);

        // Estrae l'IV dai primi 16 byte del messaggio crittografato
        byte[] iv = Arrays.copyOfRange(decodedMessage, 0, 16);
        byte[] cipherText = Arrays.copyOfRange(decodedMessage, 16, decodedMessage.length);

        SecretKeySpec secretKey = generateKey(password, salt);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

        byte[] original = cipher.doFinal(cipherText);
        return new String(original);
    }
}
