package com.example.stockquotespring.encrypt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Slf4j
@Component("springSecurityEncryptor")
public class SpringSecurityEncryptor implements Encryptor {
    private final BytesEncryptor encryptor;

    @Autowired
    public SpringSecurityEncryptor(Environment env) {
        var password = env.getProperty("app.encryption.password");
        assert password != null;
        encryptor = Encryptors.standard(password, KeyGenerators.string().generateKey());
    }

    @Override
    public String encrypt(String input) {
        return new String(
                Base64.getEncoder().encode(encryptor.encrypt(input.getBytes()))
        );
    }

    @Override
    public String decrypt(String cipherTextInBase64) {
        return new String(
                encryptor.decrypt(
                        Base64.getDecoder().decode(cipherTextInBase64.getBytes())
                )
        );
    }
}
