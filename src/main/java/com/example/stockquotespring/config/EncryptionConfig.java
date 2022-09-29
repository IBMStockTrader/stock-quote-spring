package com.example.stockquotespring.config;

import com.example.stockquotespring.encrypt.*;
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
        return AESGSMEncryption.getInstance(environment);
    }

    @Bean
    @ConditionalOnProperty(name = "app.encryption.type", havingValue = "springSecurityBytesEncryptor")
    public Encryptor springSecurityBytesEncryptor() {
        return new SpringSecurityBytesEncryptor(environment);
    }

    @Bean
    @ConditionalOnProperty(name = "app.encryption.type", havingValue = "springSecurityTextEncryptor")
    public Encryptor springSecurityTextEncryptor() {
        return new SpringSecurityTextEncryptor(environment);
    }

    @Bean
    @ConditionalOnProperty(name = "app.encryption.type", havingValue = "noneEncryptor")
    public Encryptor noneEncryptor() {
        return new NoneEncryption();
    }
}
