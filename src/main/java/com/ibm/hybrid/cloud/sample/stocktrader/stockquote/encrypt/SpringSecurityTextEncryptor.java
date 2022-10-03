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
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

@Slf4j
public class SpringSecurityTextEncryptor implements Encryptor {
    private final TextEncryptor encryptor;

    public SpringSecurityTextEncryptor(Environment env) {
        log.info("Using SpringSecurityTextEncryptor");
        var password = env.getProperty("app.encryption.password");
        assert password != null;
        if (env.getProperty("app.encryption.saltBytes") != null) {
            log.info("using with salt bytes from env");
            encryptor = Encryptors.text(password, env.getProperty("app.encryption.saltBytes"));
        } else {
            encryptor = Encryptors.text(password, KeyGenerators.string().generateKey());
        }
    }

    @Override
    public String encrypt(String input) {
        var cipherInput = encryptor.encrypt(input);
        log.info("ciphered input {}", cipherInput);
        return cipherInput;
    }

    @Override
    public String decrypt(String cipherText) {
        log.info("decrypting ciphered - {}", cipherText);
        return encryptor.decrypt(cipherText);
    }
}
