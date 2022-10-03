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

package com.ibm.hybrid.cloud.sample.stocktrader.stockquote.config;

import com.ibm.hybrid.cloud.sample.stocktrader.stockquote.encrypt.*;
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
    public Encryptor aesGsmEncryptor() throws AESException {
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
        return new NoneEncryptor();
    }
}
