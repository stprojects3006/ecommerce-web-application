package com.queue_it.connector;

import java.net.URI;
import java.util.HashMap;

public interface IHttpRequest {

    String getUserAgent();

    HashMap<String, String> getHeaders();

    URI getUri();

    String getUserHostAddress();

    String getCookieValue(String cookieKey);

    abstract String getRequestBodyAsString();

    String getHeader(String headerValue);
}
