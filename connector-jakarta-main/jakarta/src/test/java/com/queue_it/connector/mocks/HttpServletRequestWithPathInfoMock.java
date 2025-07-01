package com.queue_it.connector.mocks;

import java.net.URISyntaxException;

import com.queue_it.connector.KnownUserTest.HttpServletRequestMock;

public class HttpServletRequestWithPathInfoMock extends HttpServletRequestMock {
    public HttpServletRequestWithPathInfoMock() throws URISyntaxException {
        super();
    }

    @Override
    public String getPathInfo() {
        return "/?event1=true&queueittoken=queueittokenvalue";
    }

    @Override
    public String getRemoteHost() {
        return "test.com";
    }
}