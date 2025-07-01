package com.queue_it.connector;

public interface IHttpResponse {
    void setCookie(String cookieName, String cookieValue, Integer expiration, String domain, Boolean isCookieHttpOnly,
            Boolean isCookieSecure);
}