package com.queue_it.connector;

public interface IConnectorContextProvider {
    IHttpRequest getHttpRequest();

    IHttpResponse getHttpResponse();

    ICryptoProvider getCryptoProvider();

    IEnqueueTokenProvider getEnqueueTokenProvider();
}