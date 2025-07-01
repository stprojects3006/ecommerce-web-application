package com.queue_it.connector;

import com.queue_it.connector.models.CancelEventConfig;
import com.queue_it.connector.models.QueueEventConfig;

public interface IUserInQueueService {

    RequestValidationResult validateQueueRequest(
            String targetUrl,
            String queueitToken,
            QueueEventConfig config,
            String customerId,
            String secretKey) throws Exception;

    RequestValidationResult validateCancelRequest(
            String targetUrl,
            CancelEventConfig config,
            String customerId,
            String secretKey) throws Exception;

    void extendQueueCookie(
            String eventId,
            int cookieValidityMinutes,
            String cookieDomain,
            Boolean isCookieHttpOnly,
            Boolean isCookieSecure,
            String secretKey);

    RequestValidationResult getIgnoreActionResult(String actionName);
}