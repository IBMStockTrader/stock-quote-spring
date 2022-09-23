package com.example.stockquotespring.config;

import com.example.stockquotespring.encrypt.AESGSMEncryption;
import com.example.stockquotespring.encrypt.Encryptor;
import com.example.stockquotespring.encrypt.SpringSecurityEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class EncryptionConfig {

    private final Environment environment;

    @Autowired
    public EncryptionConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    @ConditionalOnProperty(name = "app.encryption.type", havingValue = "aesGsmEncryptor")
    public Encryptor aesGsmEncryptor() {
        return new AESGSMEncryption(environment);
    }

    @Bean
    @ConditionalOnProperty(name = "app.encryption.type", havingValue = "springSecurityEncryptor")
    public Encryptor springSecurityEncryptor() {
        return new SpringSecurityEncryptor(environment);
    }
}
