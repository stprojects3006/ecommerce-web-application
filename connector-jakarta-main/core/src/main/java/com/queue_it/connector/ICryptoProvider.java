package com.queue_it.connector;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface ICryptoProvider {
    String generateSHA256Hash(String secretKey, String stringToHash)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException;
}