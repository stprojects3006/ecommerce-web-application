package com.queue_it.connector.helpers;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HashHelper {

    public static String generateSHA256Hash(String secretKey, String stringToHash) {
        try {
            byte[] secretKeyBytes = secretKey.getBytes("UTF-8");
            SecretKeySpec signingKey = new SecretKeySpec(secretKeyBytes, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] bytes = stringToHash.getBytes("UTF-8");
            byte[] rawHmac = mac.doFinal(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : rawHmac) {
                sb.append(String.format("%1$02x", b));
            }

            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}