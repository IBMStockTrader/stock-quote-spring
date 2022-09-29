package com.example.stockquotespring.encrypt;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoneEncryption implements Encryptor {

    public NoneEncryption() {
        log.info("Not using encryption");
    }

    @Override
    public String encrypt(String input) {
        return input;
    }

    @Override
    public String decrypt(String cipherTextInBase64) {
        return cipherTextInBase64;
    }
}
