package com.queue_it.connector;

import static org.junit.Assert.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.Test;

import com.queue_it.connector.EnqueueTokenProvider;
import com.queue_it.connector.helpers.HashHelper;
import com.queue_it.queuetoken.EnqueueTokenGenerator;
import com.queue_it.queuetoken.EnqueueTokenPayloadGenerator;
import com.queue_it.queuetoken.IEnqueueToken;

public class DefaultEnqueueTokenProviderTest {
    private final String customerId = "cusotmerId";
    private final String secretKey = "secretKey";
    private final String waitingRoomId = "waitingRoom1";
    private final String mockedClientIP = "127.0.0.2";
    private final int validityTime = 240000;
    private final boolean withKey = false;
    private EnqueueTokenGenerator tokenGenerator;
    private EnqueueTokenPayloadGenerator payLoadGenerator;

    private static IEnqueueToken Parse(String tokenString, String secretKey) throws Exception {
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new Exception("Invalid secret key.");
        }

        if (tokenString == null || tokenString.trim().isEmpty()) {
            throw new Exception("Invalid token.");
        }

        String[] tokenParts = tokenString.split(".");
        String headerPart = tokenParts[0];
        String payloadPart = tokenParts[1];
        String hashPart = tokenParts[2];

        if (headerPart == null || headerPart.trim().isEmpty())
            throw new IllegalArgumentException("Invalid token");
        if (hashPart == null || hashPart.trim().isEmpty())
            throw new IllegalArgumentException("Invalid token");

        String token = headerPart + "." + payloadPart;

        String expectedHash = Base64.getUrlEncoder()
                .encodeToString(HashHelper.generateSHA256Hash(secretKey, token)
                        .getBytes("UTF-8"));

        if (expectedHash != hashPart)
            throw new Exception();
        return null;
    }

    // @Test
    // public void Test_GetEnqueueToken_Given_WithoutCustomKeyAndCustomData_Returns_EnqueueToken() throws Exception {
    //     EnqueueTokenProvider enqueueTokenProvider = new EnqueueTokenProvider(
    //             customerId,
    //             secretKey,
    //             validityTime,
    //             mockedClientIP,
    //             withKey,
    //             tokenGenerator,
    //             payLoadGenerator,
    //             null);

    //     String enqueueToken = enqueueTokenProvider.getEnqueueToken(waitingRoomId);
    //     assertNotNull(enqueueToken);
    // }
}
