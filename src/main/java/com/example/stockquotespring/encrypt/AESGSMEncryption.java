package com.example.stockquotespring.encrypt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

@Component("aesGsmEncryptor")
public class AESGSMEncryption implements Encryptor {

    private static final String GSM_ALGORITHM = "AES/GCM/NoPadding";
    private Key key;
    private IvParameterSpec initialVector;
    private String password;

    @Autowired
    public AESGSMEncryption(Environment env) {
        password = env.getProperty("app.encryption.password");
        assert password != null;
    }

    public String encrypt(String input) throws AESException {
        try {
            Cipher cipher = Cipher.getInstance(GSM_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getKey(), getGcmParamSpecification());
            byte[] cipherText = cipher.doFinal(input.getBytes());
            return new String(Base64.getEncoder().encode(cipherText));
        } catch (Exception e) {
            throw new AESException(e.getMessage());
        }
    }

    public String decrypt(String cipherTextInBase64) throws AESException {
        try {
            Cipher cipher = Cipher.getInstance(GSM_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getKey(), getGcmParamSpecification());
            byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherTextInBase64.getBytes()));
            return new String(plainText);
        } catch (Exception e) {
            throw new AESException(e.getMessage());
        }
    }

    private AlgorithmParameterSpec getGcmParamSpecification() {
        return new GCMParameterSpec(128, getInitialVector().getIV());
    }

    private Key getKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (key == null)
            key = getKeyFromPassword(password, generateSecureRandomBytes(8));
        return key;
    }

    private IvParameterSpec getInitialVector() {
        if (initialVector == null)
            initialVector = generateInitialVector(96);
        return initialVector;
    }

    private IvParameterSpec generateInitialVector(int byteNum) {
        return new IvParameterSpec(generateSecureRandomBytes(byteNum));
    }

    private Key getKeyFromPassword(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    private byte[] generateSecureRandomBytes(int byteNum) {
        byte[] bytes = new byte[byteNum];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
}