package com.queue_it.connector;

import com.queue_it.connector.models.StateInfo;

public interface IUserInQueueStateRepository {

    void store(
            String eventId,
            String queueId,
            Integer fixedCookieValidityMinutes,
            String cookieDomain,
            Boolean isCookieHttpOnly,
            Boolean isCookieSecure,
            String redirectType,
            String hashedIP,
            String secretKey) throws Exception;

    StateInfo getState(String eventId,
            int cookieValidityMinutes,
            String secretKey,
            boolean validateTime);

    void cancelQueueCookie(
            String eventId,
            String cookieDomain,
            Boolean isCookieHttpOnly,
            Boolean isCookieSecure);

    void reissueQueueCookie(
            String eventId,
            int cookieValidityMinutes,
            String cookieDomain,
            Boolean isCookieHttpOnly,
            Boolean isCookieSecure,
            String secretKey);
}
