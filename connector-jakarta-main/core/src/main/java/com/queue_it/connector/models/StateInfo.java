package com.queue_it.connector.models;

import com.queue_it.connector.helpers.Utils;

public class StateInfo {

    private final String queueId;
    private final String fixedCookieValidityMinutes;
    private final String redirectType;
    private final boolean isFound;
    private final boolean isValid;
    private final String clientIP;

    public StateInfo(boolean isFound, boolean isValid, String queueId, String fixedCookieValidityMinutes,
            String redirectType, String clientIP) {
        this.isFound = isFound;
        this.isValid = isValid;
        this.queueId = queueId;
        this.fixedCookieValidityMinutes = fixedCookieValidityMinutes;
        this.redirectType = redirectType;
        this.clientIP = clientIP;
    }

    public String getQueueId() {
        return this.queueId;
    }

    public boolean isFound() {
        return this.isFound;
    }

    public boolean isValid() {
        return this.isValid;
    }

    public String getRedirectType() {
        return this.redirectType;
    }

    public boolean isStateExtendable() {
        return this.isValid && Utils.isNullOrWhiteSpace(this.fixedCookieValidityMinutes);
    }

    public String getClientIP() {
        return this.clientIP;
    }
}