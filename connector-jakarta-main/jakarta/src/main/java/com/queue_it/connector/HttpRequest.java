package com.queue_it.connector;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import com.queue_it.connector.helpers.Utils;

public class HttpRequest implements IHttpRequest {

    public final HttpServletRequest _httpServletRequest;
    public final String ClientIpHeader = "x-queueit-clientip";
    
    public String UserAgent;
    public String UserHostAddress;
    public HashMap<String, String> Headers;
    public URI Uri;

    /**
     * @param httpServletRequest
     * @throws URISyntaxException
     */
    public HttpRequest(HttpServletRequest httpServletRequest) throws URISyntaxException {
        _httpServletRequest = httpServletRequest;
        Headers = new HashMap<String, String>();

        final Enumeration headerNames = _httpServletRequest.getHeaderNames();

        UserAgent = _httpServletRequest.getHeader("User-Agent");
        UserHostAddress = _httpServletRequest.getRemoteAddr();

        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = _httpServletRequest.getHeader(key);
            Headers.put(key, value);
        }

        StringBuilder requestFullURLStringBuilder = new StringBuilder(_httpServletRequest.getRequestURL().toString());

        if (!Utils.isNullOrWhiteSpace(_httpServletRequest.getQueryString())) {
            requestFullURLStringBuilder.append('?');
            requestFullURLStringBuilder.append(_httpServletRequest.getQueryString());
        }
        Uri = new URI(requestFullURLStringBuilder.toString());
    }

    @Override
    public String getUserAgent() {
        return UserAgent;
    }

    @Override
    public HashMap<String, String> getHeaders() {
        return Headers;
    }

    @Override
    public URI getUri() {
        return Uri;
    }

    @Override
    public String getUserHostAddress() {
        return UserHostAddress;
    }

    @Override
    public String getCookieValue(String cookieKey) {
        Cookie[] cookies = _httpServletRequest.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieKey)) {
                try {
                    return URLDecoder.decode(cookie.getValue(), "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                }
            }
        }
        return null;
    }

    @Override
    public String getRequestBodyAsString() {
        return "";
    }

    @Override
    public String getHeader(String headerValue) {
        if (headerValue.equals(ClientIpHeader)) {
            return this.getUserHostAddress();
        } else {
            return Headers.get(headerValue.toLowerCase());
        }
    }
}