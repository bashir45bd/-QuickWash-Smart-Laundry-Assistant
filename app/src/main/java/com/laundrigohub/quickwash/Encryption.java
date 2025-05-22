package com.laundrigohub.quickwash;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {

        public static String Mykey="";

        public static String EncryptData(String value){

            String plainText = value;
            byte[] plainTextBytes = null;
            try {
                plainTextBytes = plainText.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

            String password = "aB3$dEfGh1JkLmNo"; // 16-bit key for AES
            byte[] passwordBytes = null;
            try {
                passwordBytes = password.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

            SecretKeySpec secretKey = new SecretKeySpec(passwordBytes, "AES");
            Cipher cipher = null;
            try {
                cipher = Cipher.getInstance("AES");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (NoSuchPaddingException e) {
                throw new RuntimeException(e);
            }
            try {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            }
            byte[] securedBytes = null;
            try {
                securedBytes = cipher.doFinal(plainTextBytes);
            } catch (BadPaddingException e) {
                throw new RuntimeException(e);
            } catch (IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            }

            String encodedpass = Base64.encodeToString(securedBytes, Base64.DEFAULT);

            return encodedpass;
        }

        public static String DecryptData(String value){

            String encodedString = value;
            byte[] decodedBytes = Base64.decode(encodedString, Base64.DEFAULT);

            String password = "aB3$dEfGh1JkLmNo"; // 16-bit key for AES
            byte[] passwordBytes = null;
            try {
                passwordBytes = password.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

            SecretKeySpec secretKeySpec = new SecretKeySpec(passwordBytes, "AES");
            Cipher cipher = null;
            try {
                cipher = Cipher.getInstance("AES");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (NoSuchPaddingException e) {
                throw new RuntimeException(e);
            }
            try {
                cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            }
            byte[] decryptedBytes = null;
            try {
                decryptedBytes = cipher.doFinal(decodedBytes);
            } catch (BadPaddingException e) {
                throw new RuntimeException(e);
            } catch (IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            }

            String plainText = null;
            try {
                plainText = new String(decryptedBytes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

            return plainText;
        }

    }
