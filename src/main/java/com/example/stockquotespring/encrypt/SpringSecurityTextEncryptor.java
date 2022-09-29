package com.example.stockquotespring.encrypt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

import java.util.Base64;

@Slf4j
public class SpringSecurityTextEncryptor implements Encryptor {
    private final TextEncryptor encryptor;

    public SpringSecurityTextEncryptor(Environment env) {
        log.info("Using SpringSecurityTextEncryptor");
        var password = env.getProperty("app.encryption.password");
        assert password != null;
        encryptor = Encryptors.text(password, KeyGenerators.string().generateKey());
    }

    @Override
    public String encrypt(String input) {
        var encodedIn64 = Base64.getEncoder().encodeToString(encryptor.encrypt(input).getBytes());
        log.info("encrypting in base 64 - {}", encodedIn64);
        return encodedIn64;
    }

    @Override
    public String decrypt(String cipherTextInBase64) {
        log.info("decrypting base 64 - {}", cipherTextInBase64);
        return encryptor.decrypt(
                new String(
                        Base64.getDecoder().decode(cipherTextInBase64.getBytes())
                )
        );
    }
}
