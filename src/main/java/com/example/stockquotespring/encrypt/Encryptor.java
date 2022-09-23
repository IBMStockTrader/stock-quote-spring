package com.example.stockquotespring.encrypt;

public interface Encryptor {

    String encrypt(String input) throws Exception;

    String decrypt(String cipherTextInBase64) throws Exception;

}
