package com.example.stockquotespring.encrypt;

public class NoneEncryption implements Encryptor {

    @Override
    public String encrypt(String input) {
        return input;
    }

    @Override
    public String decrypt(String cipherTextInBase64) {
        return cipherTextInBase64;
    }
}
