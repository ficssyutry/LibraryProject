package com.example.library.security;

import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class Id39586EncryptionService {

    public String encrypt(String data) {

        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    public String decrypt(String encrypted) {

        return new String(Base64.getDecoder().decode(encrypted));
    }
}