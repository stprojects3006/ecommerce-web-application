package com.queue_it.connector;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.queue_it.connector.models.CustomData;
import com.queue_it.queuetoken.EnqueueTokenGenerator;
import com.queue_it.queuetoken.EnqueueTokenPayloadGenerator;
import com.queue_it.queuetoken.IEnqueueToken;
import com.queue_it.queuetoken.Payload;
import com.queue_it.queuetoken.Token;

interface ICustomData {
    public HashMap<String, String> customData = null;
}

public class EnqueueTokenProvider implements IEnqueueTokenProvider {

    private final String customerId;
    private final String secretKey;
    private final String clientIp;
    private final int validityTime;
    private final Boolean withKey;
    private final String xForwardedFor;
    private EnqueueTokenGenerator tokenGenerator;
    private EnqueueTokenPayloadGenerator payLoadGenerator;

    public EnqueueTokenProvider(
            String customerId,
            String secretKey,
            int validityTime,
            String clientIp,
            boolean withKey,
            EnqueueTokenGenerator tokenGenerator,
            EnqueueTokenPayloadGenerator payLoadGenerator,
            String xForwardedFor) {

        this.customerId = customerId;
        this.secretKey = secretKey;
        this.validityTime = validityTime;
        this.clientIp = clientIp;
        this.withKey = withKey;
        this.tokenGenerator = tokenGenerator == null ? Token.enqueue(customerId) : tokenGenerator;
        this.payLoadGenerator = payLoadGenerator == null ? Payload.enqueue() : payLoadGenerator;
        this.xForwardedFor = xForwardedFor;
    }

    @Override
    public String getEnqueueToken(String waitingRoomId) throws Exception {

        List<CustomData> customData = getEnqueueTokenCustomData(waitingRoomId);
        if (customData != null && !customData.isEmpty()) {
            for (CustomData data : customData) {
                this.payLoadGenerator = this.payLoadGenerator.withCustomData(data.getKey(), data.getValue());
            }
        }

        if (this.withKey) {
            payLoadGenerator = payLoadGenerator.withKey(getEnqueueTokenKey(waitingRoomId));
        }

        IEnqueueToken enqueueToken = Token.enqueue(customerId)
                .withPayload(payLoadGenerator.generate())
                .withEventId(waitingRoomId)
                .withIpAddress(clientIp, xForwardedFor)
                .withValidity(validityTime)
                .generate(secretKey);

        return enqueueToken.getToken();
    }

    @Override
    public List<CustomData> getEnqueueTokenCustomData(String waitingRoom) throws Exception {
        return Collections.emptyList();
    }

    @Override
    public String getEnqueueTokenKey(String waitingRoomId) throws Exception {
        return UUID.randomUUID().toString();
    }
}