package com.queue_it.connector;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import com.queue_it.connector.helpers.Utils;

public class HttpResponse implements IHttpResponse {

    HttpServletResponse _HttpServletResponse;

    public HttpResponse(HttpServletResponse httpServletResponse) {
        _HttpServletResponse = httpServletResponse;
    }

    @Override
    public void setCookie(String name, String value, Integer expiration, String domain,
            Boolean isCookieHttpOnly, Boolean isCookieSecure) {
        Cookie cookie = new Cookie(name, value);

        if (value == null) {
            value = "";
        }

        try {
            cookie.setValue(URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(HttpResponse.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (expiration != null) {
            cookie.setMaxAge(expiration);
        }
        cookie.setPath("/");
        if (!Utils.isNullOrWhiteSpace(domain)) {
            cookie.setDomain(domain);
        }

        cookie.setHttpOnly(Boolean.TRUE.equals(isCookieHttpOnly));
        cookie.setSecure(Boolean.TRUE.equals(isCookieSecure));

        _HttpServletResponse.addCookie(cookie);
    }
}
