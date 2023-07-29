package com.example.petshop.pelengkap;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Base64;

import androidx.annotation.RequiresApi;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class KeyStoreHelper {
    private static final String CIPHER_TRANSFORMATION = "AES";
    private static Cipher cipher;

    //    https://stackoverflow.com/questions/10303767/encrypt-and-decrypt-in-java
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static SecretKey getOrCreateSecretKey() throws Exception {
        /*
         create key
         If we need to generate a new key use a KeyGenerator
         If we have existing plaintext key use a SecretKeyFactory
        */
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128); // block size is 128bits

        return keyGenerator.generateKey();
    }

    public static String encodeKeyToString(SecretKey secretKey) {
        return Base64.encodeToString(secretKey.getEncoded(), Base64.NO_WRAP);
    }

    public static SecretKey decodeKeyFromString(String encodedKey) {
        byte[] keyBytes = Base64.decode(encodedKey, Base64.NO_WRAP);
        return new SecretKeySpec(keyBytes, "AES");
    }

    @SuppressLint("GetInstance")
    public static String encryptionOrDecryptionAES(String input, SecretKey secretKey,
                                                   boolean isEncrypt) throws Exception {
        if (cipher == null) {
            /*
              Cipher Info
              Algorithm : for the encryption of electronic data
              mode of operation : to avoid repeated blocks encrypt to the same values.
              padding: ensuring messages are the proper length necessary for certain ciphers
              mode/padding are not used with stream cyphers.
            */
            cipher = Cipher.getInstance(CIPHER_TRANSFORMATION); //SunJCE provider AES algorithm, mode(optional) and padding schema(optional)
        }

        if (isEncrypt) {
            return encrypt(input, secretKey);
        } else {
            return decrypt(input, secretKey);
        }
    }

    private static String encrypt(String plainText, SecretKey secretKey) throws Exception {
        byte[] plainTextByte = plainText.getBytes();
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedByte = cipher.doFinal(plainTextByte);
        return Base64.encodeToString(encryptedByte, Base64.NO_WRAP);
    }

    private static String decrypt(String encryptedText, SecretKey secretKey) throws Exception {
        byte[] encryptedTextByte = Base64.decode(encryptedText, Base64.NO_WRAP);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
        return new String(decryptedByte);
    }
}