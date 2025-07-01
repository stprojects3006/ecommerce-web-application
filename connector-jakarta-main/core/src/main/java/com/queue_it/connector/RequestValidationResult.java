package com.queue_it.connector;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.queue_it.connector.helpers.Utils;

public class RequestValidationResult {

    private String actionName;
    private String actionType;
    private String eventId;
    public boolean isAjaxResult;
    private String queueId;
    private String redirectType;
    private String redirectUrl;

    public RequestValidationResult(String actionType, String eventId, String queueId, String redirectUrl,
            String redirectType, String actionName) {
        this.actionName = actionName;
        this.actionType = actionType;
        this.eventId = eventId;
        this.queueId = queueId;
        this.redirectType = redirectType;
        this.redirectUrl = redirectUrl;
    }

    public String getActionType() {
        return actionType;
    }

    public String getEventId() {
        return eventId;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public boolean doRedirect() {
        return !Utils.isNullOrWhiteSpace(redirectUrl);
    }

    public String getQueueId() {
        return this.queueId;
    }

    public String getRedirectType() {
        return this.redirectType;
    }

    public String getAjaxQueueRedirectHeaderKey() {
        return "x-queueit-redirect";
    }

    public String getAjaxRedirectUrl() {
        try {
            if (!Utils.isNullOrWhiteSpace(redirectUrl)) {
                return URLEncoder.encode(redirectUrl, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
        }
        return "";
    }

    public String getActionName() {
        return this.actionName;
    }
}
