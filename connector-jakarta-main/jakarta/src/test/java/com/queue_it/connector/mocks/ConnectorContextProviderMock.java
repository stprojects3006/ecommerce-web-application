package com.queue_it.connector.mocks;

import java.net.URISyntaxException;

import com.queue_it.connector.KnownUserTest.HttpServletRequestMock;
import com.queue_it.connector.KnownUserTest.HttpServletResponseMock;
import com.queue_it.connector.EnqueueTokenProvider;
import com.queue_it.connector.IConnectorContextProvider;
import com.queue_it.connector.ICryptoProvider;
import com.queue_it.connector.IEnqueueTokenProvider;
import com.queue_it.connector.IHttpRequest;
import com.queue_it.connector.IHttpResponse;
import com.queue_it.connector.CryptoProvider;
import com.queue_it.connector.HttpRequest;
import com.queue_it.connector.HttpResponse;

public class ConnectorContextProviderMock implements IConnectorContextProvider {

    public KnownUserRequestWrapperMock knownUserRequestWrapperMock;
    public HttpServletRequestMock httpServletRequestMock;
    public IHttpRequest httpRequest;
    public HttpServletResponseMock httpServletResponseMock;
    public IHttpResponse httpResponse;

    public ConnectorContextProviderMock() throws URISyntaxException {
        httpServletRequestMock = new HttpServletRequestMock();
        knownUserRequestWrapperMock = new KnownUserRequestWrapperMock(httpServletRequestMock);
        httpRequest = new HttpRequest(knownUserRequestWrapperMock);
        httpServletResponseMock = new HttpServletResponseMock();
        httpResponse = new HttpResponse(httpServletResponseMock);
    }

    @Override
    public IHttpRequest getHttpRequest() {
        return httpRequest;
    }

    @Override
    public IHttpResponse getHttpResponse() {
        return httpResponse;
    }

    @Override
    public ICryptoProvider getCryptoProvider() {
        return new CryptoProvider();
    }

    @Override
    public IEnqueueTokenProvider getEnqueueTokenProvider() {
        return new EnqueueTokenProvider("customerId", "secretKey", 0, "127.0.0.1",
                false, null, null, "127.0.0.1");
    }
}
