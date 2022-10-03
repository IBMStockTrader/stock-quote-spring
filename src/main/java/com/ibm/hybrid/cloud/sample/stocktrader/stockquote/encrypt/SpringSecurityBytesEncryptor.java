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
        var encodedIn64 = Base64.getEncoder().encodeToString(encryptor.encrypt(input.getBytes()));
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
