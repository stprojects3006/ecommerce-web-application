package com.queue_it.connector.mocks;

import jakarta.servlet.http.HttpServletRequest;

import com.queue_it.connector.KnownUserRequestWrapper;


public class KnownUserRequestWrapperMock extends KnownUserRequestWrapper {

    String Body = "";
    public KnownUserRequestWrapperMock(HttpServletRequest request) {
        super(request);
    }

    public void SetRequestBodyAsString(String body){
        this.Body = body;
    }

    public String GetRequestBodyAsString(){
        return this.Body;
    }
}