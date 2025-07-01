package com.queue_it.connector;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class KnownUserRequestWrapper extends HttpServletRequestWrapper {

    public KnownUserRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    public String GetRequestBodyAsString(){
        return "";
    }
}