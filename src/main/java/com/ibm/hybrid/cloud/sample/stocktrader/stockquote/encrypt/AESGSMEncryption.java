/*
       Copyright 2022 Kyndryl, All Rights Reserved
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.ibm.hybrid.cloud.sample.stocktrader.stockquote.encrypt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.util.EncodingUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.util.Base64;

@Slf4j
public class AESGSMEncryption implements Encryptor {

    private static final int IV_BYTES_LENGTH = 96;
    private static final int SALT_BYTES_LENGTH = 8;
    private static AESGSMEncryption encryption;
    private static final String GSM_ALGORITHM = "AES/GCM/NoPadding";
    private final Key key;

    public static AESGSMEncryption getInstance(Environment env) throws AESException {
        if (encryption == null)
            encryption = new AESGSMEncryption(env);
        return encryption;
    }

    private AESGSMEncryption(Environment env) throws AESException {
        log.info("Using AESGSMEncryption");
        String password = env.getProperty("app.encryption.password");
        assert password != null;
        if (env.getProperty("app.encryption.saltBytes") != null) {
            log.info("using with salt bytes from env");
            key = getKeyFromPassword(password, Hex.decode(env.getProperty("app.encryption.saltBytes")));
        } else {
            log.info("using default constructor");
            key = getKeyFromPassword(password, generateSecureRandomBytes(SALT_BYTES_LENGTH));
        }
    }

    public String encrypt(String input) throws AESException {
        try {
            var ivBytes = generateSecureRandomBytes(IV_BYTES_LENGTH);
            Cipher cipher = Cipher.getInstance(GSM_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, getGcmParamSpecification(ivBytes));
            byte[] cipherText = cipher.doFinal(input.getBytes());
            var cipherTextWithIvBytes = EncodingUtils.concatenate(ivBytes, cipherText);
            var encodedIn64 = Base64.getEncoder().encodeToString(cipherTextWithIvBytes);
            log.info("encrypting in base 64 - {} ...", encodedIn64.substring(0, 30));
            return encodedIn64;
        } catch (Exception e) {
            throw new AESException(e.getMessage());
        }
    }

    public String decrypt(String cipherTextInBase64) throws AESException {
        log.info("decrypting base 64 - {} ...", cipherTextInBase64.substring(0, 30));
        try {
            var cipherBytesWithIv = Base64.getDecoder().decode(cipherTextInBase64.getBytes());
            var ivBytes = getIvBytes(cipherBytesWithIv);
            Cipher cipher = Cipher.getInstance(GSM_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, getGcmParamSpecification(ivBytes));
            byte[] plainText = cipher.doFinal(getCipherText(cipherBytesWithIv));
            return new String(plainText);
        } catch (Exception e) {
            throw new AESException(e.getMessage());
        }
    }

    private byte[] getCipherText(byte[] cipherTextWithIvBytes) {
        return EncodingUtils.subArray(cipherTextWithIvBytes, IV_BYTES_LENGTH, cipherTextWithIvBytes.length);
    }

    private byte[] getIvBytes(byte[] cipherTextWithIvBytes) {
        return EncodingUtils.subArray(cipherTextWithIvBytes, 0, IV_BYTES_LENGTH);
    }

    private AlgorithmParameterSpec getGcmParamSpecification(byte[] ivBytes) {
        return new GCMParameterSpec(128, ivBytes);
    }

    private Key getKeyFromPassword(String password, byte[] salt) throws AESException {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 1024, 256);
            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        } catch (Exception e) {
            throw new AESException(e.getMessage());
        }
    }

    private byte[] generateSecureRandomBytes(int byteNum) {
        byte[] bytes = new byte[byteNum];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
}