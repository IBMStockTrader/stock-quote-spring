package com.example.stockquotespring.encrypt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.keygen.KeyGenerators;

import java.util.Base64;

@Slf4j
public class SpringSecurityBytesEncryptor implements Encryptor {
    private final BytesEncryptor encryptor;

    public SpringSecurityBytesEncryptor(Environment env) {
        log.info("Using SpringSecurityBytesEncryptor");
        var password = env.getProperty("app.encryption.password");
        assert password != null;
        encryptor = Encryptors.standard(password, KeyGenerators.string().generateKey());
    }

    @Override
    public String encrypt(String input) {
        var cipherText = encryptor.encrypt(input.getBytes());
        var encodedIn64 = Base64.getEncoder().encodeToString(cipherText);
        log.info("encrypting in base 64 - {}", encodedIn64);
        return encodedIn64;

    }

    @Override
    public String decrypt(String cipherTextInBase64) {
        log.info("decrypting base 64 - {}", cipherTextInBase64);
        return new String(
                encryptor.decrypt(
                        Base64.getDecoder().decode(cipherTextInBase64.getBytes())
                )
        );
    }
}
