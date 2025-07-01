package com.queue_it.connector.models;

public class ConnectorSettings {

    private static final int DEFAULT_ENQUEUETOKEN_VALIDITY_TIME = 240000;
    private static final int MINIMUM_ENQUEUETOKEN_VALIDITY_TIME = 30000;

    public ConnectorSettings(Boolean enqueueTokenEnabled, String customerId, String secretKey, String apiKey,
            int enqueueTokenValidityTime, Boolean enqueueTokenKeyEnabled) {
        this.customerId = customerId;
        this.secretKey = secretKey;
        this.apiKey = apiKey;
        this.enqueueTokenEnabled = enqueueTokenEnabled;
        this.enqueueTokenValidityTime = enqueueTokenValidityTime < MINIMUM_ENQUEUETOKEN_VALIDITY_TIME
                ? DEFAULT_ENQUEUETOKEN_VALIDITY_TIME
                : enqueueTokenValidityTime;
        this.enqueueTokenKeyEnabled = enqueueTokenKeyEnabled;
    }

    public ConnectorSettings(String customerId, String secretKey, String apiKey) {
        this.customerId = customerId;
        this.secretKey = secretKey;
        this.apiKey = apiKey;
        this.enqueueTokenEnabled = true;
        this.enqueueTokenValidityTime = DEFAULT_ENQUEUETOKEN_VALIDITY_TIME;
        this.enqueueTokenKeyEnabled = false;
    }

    private final String customerId;

    public String getCustomerId() {
        return customerId;
    }

    private final String secretKey;

    public String getSecretKey() {
        return secretKey;
    }

    private final String apiKey;

    public String getApiKey() {
        return apiKey;
    }

    private final Boolean enqueueTokenEnabled;

    public Boolean getEnqueueTokenEnabled() {
        return enqueueTokenEnabled;
    }

    private final int enqueueTokenValidityTime;

    public int getEnqueueTokenValidityTime() {
        return enqueueTokenValidityTime;
    }

    private final Boolean enqueueTokenKeyEnabled;

    public Boolean getEnqueueTokenKeyEnabled() {
        return enqueueTokenKeyEnabled;
    }

    public int getDefaultEnqueueTokenValidityTime() {
        return DEFAULT_ENQUEUETOKEN_VALIDITY_TIME;
    }
}
