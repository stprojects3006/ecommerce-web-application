package com.queue_it.connector;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class CryptoProvider implements ICryptoProvider {

    @Override
    public String generateSHA256Hash(String secretKey, String plainString)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        byte[] secretKeyBytes = secretKey.getBytes("UTF-8");
        SecretKeySpec signingKey = new SecretKeySpec(secretKeyBytes, "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);
        byte[] bytes = plainString.getBytes("UTF-8");
        byte[] rawHmac = mac.doFinal(bytes);
        StringBuilder hashedString = new StringBuilder();
        for (byte b : rawHmac) {
            hashedString.append(String.format("%1$02x", b));
        }

        return hashedString.toString();
    }
}
