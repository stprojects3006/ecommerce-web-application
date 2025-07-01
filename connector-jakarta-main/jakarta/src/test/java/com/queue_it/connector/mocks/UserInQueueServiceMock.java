package com.queue_it.connector.mocks;

import java.util.ArrayList;

import com.queue_it.connector.KnownUserTest;
import com.queue_it.connector.IUserInQueueService;
import com.queue_it.connector.RequestValidationResult;
import com.queue_it.connector.models.CancelEventConfig;
import com.queue_it.connector.models.QueueEventConfig;

public class UserInQueueServiceMock implements IUserInQueueService {

    public ArrayList<ArrayList<String>> validateQueueRequestCalls = new ArrayList<ArrayList<String>>();
    public boolean validateQueueRequestRaiseException = false;
    public ArrayList<ArrayList<String>> validateCancelRequestCalls = new ArrayList<ArrayList<String>>();
    public boolean validateCancelRequestRaiseException = false;
    public ArrayList<ArrayList<String>> extendQueueCookieCalls = new ArrayList<ArrayList<String>>();
    public ArrayList<ArrayList<String>> getIgnoreActionResultCalls = new ArrayList<ArrayList<String>>();

    public UserInQueueServiceMock(ConnectorContextProviderMock connectorContextProviderMock) {
        KnownUserTest.connectorContextProviderMock = connectorContextProviderMock;
    }

    @Override
    public RequestValidationResult validateQueueRequest(String targetUrl, String queueitToken,
            QueueEventConfig config, String customerId, String secretKey) throws Exception {
        ArrayList<String> args = new ArrayList<String>();
        args.add(targetUrl);
        args.add(queueitToken);
        args.add(config.getCookieDomain() + ":" + config.getLayoutName() + ":" + config.getCulture() + ":"
                + config.getEventId() + ":" + config.getQueueDomain() + ":" + config.getExtendCookieValidity() + ":"
                + config.getCookieValidityMinute() + ":" + config.getVersion() + ":" + config.getActionName());
        args.add(customerId);
        args.add(secretKey);
        validateQueueRequestCalls.add(args);

        if (this.validateQueueRequestRaiseException) {
            throw new Exception("exception");
        } else {
            return new RequestValidationResult("Queue", "", "", "", "", "");
        }
    }

    @Override
    public RequestValidationResult validateCancelRequest(String targetUrl, CancelEventConfig config,
            String customerId, String secretKey) throws Exception {

        ArrayList<String> args = new ArrayList<String>();
        args.add(targetUrl);
        args.add(config.getCookieDomain() + ":" + config.getEventId() + ":" + config.getQueueDomain() + ":"
                + config.getVersion() + ":" + config.getActionName());
        args.add(customerId);
        args.add(secretKey);
        validateCancelRequestCalls.add(args);

        if (this.validateCancelRequestRaiseException) {
            throw new Exception("exception");
        } else {
            return new RequestValidationResult("Cancel", "", "", "", "", "");
        }
    }

    @Override
    public void extendQueueCookie(String eventId, int cookieValidityMinute, String cookieDomain,
            Boolean isCookieHttpOnly, Boolean isCookieSecure, String secretKey) {
        ArrayList<String> args = new ArrayList<String>();
        args.add(eventId);
        args.add(Integer.toString(cookieValidityMinute));
        args.add(cookieDomain);
        args.add(secretKey);
        args.add(Boolean.toString(isCookieHttpOnly));
        args.add(Boolean.toString(isCookieSecure));
        extendQueueCookieCalls.add(args);
    }

    @Override
    public RequestValidationResult getIgnoreActionResult(String actionName) {
        ArrayList<String> args = new ArrayList<String>();
        args.add(actionName);
        getIgnoreActionResultCalls.add(args);
        return new RequestValidationResult("Ignore", "", "", "", "", "");
    }
}