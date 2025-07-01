package com.queue_it.connector.helpers;

public class QueueUrlParams {

    final static String EmptyString = "";

    private Integer cookieValidityMinutes;
    private String eventId;
    private boolean extendableCookie;
    private String hashCode;
    private String HashedIp;
    private String queueId;
    private String queueITToken;
    private String queueITTokenWithoutHash;
    private String redirectType;
    private long timeStamp;

    public QueueUrlParams() {
        this.cookieValidityMinutes = null;
        this.eventId = EmptyString;
        this.extendableCookie = false;
        this.hashCode = EmptyString;
        this.HashedIp = EmptyString;
        this.queueId = EmptyString;
        this.queueITToken = EmptyString;
        this.queueITTokenWithoutHash = EmptyString;
        this.timeStamp = 0;
    }

    public String getEventId() {
        return this.eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getHashCode() {
        return this.hashCode;
    }

    public void setHashCode(String hashCode) {
        this.hashCode = hashCode;
    }

    public boolean getExtendableCookie() {
        return this.extendableCookie;
    }

    public void setExtendableCookie(boolean extendableCookie) {
        this.extendableCookie = extendableCookie;
    }

    public Integer getCookieValidityMinutes() {
        return this.cookieValidityMinutes;
    }

    public void setCookieValidityMinutes(Integer cookieValidityMinute) {
        this.cookieValidityMinutes = cookieValidityMinute;
    }

    public String getQueueITToken() {
        return this.queueITToken;
    }

    public void setQueueITToken(String queueITToken) {
        this.queueITToken = queueITToken;
    }

    public String getQueueITTokenWithoutHash() {
        return this.queueITTokenWithoutHash;
    }

    public void setQueueITTokenWithoutHash(String queueITTokenWithoutHash) {
        this.queueITTokenWithoutHash = queueITTokenWithoutHash;
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }

    public String getQueueId() {
        return this.queueId;
    }

    public void setRedirectType(String redirectType) {
        this.redirectType = redirectType;
    }

    public String getRedirectType() {
        return this.redirectType;
    }

    public String getHashedIp() {
        return HashedIp;
    }

    public void setHashedIp(String hashedIp) {
        HashedIp = hashedIp;
    }
}