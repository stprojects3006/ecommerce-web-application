package com.queue_it.connector;

import java.net.URISyntaxException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import com.queue_it.connector.models.ConnectorSettings;
import com.queue_it.queuetoken.EnqueueTokenGenerator;
import com.queue_it.queuetoken.EnqueueTokenPayloadGenerator;

public class ConnectorContextProvider implements IConnectorContextProvider {

    private IHttpRequest httpRequest;
    private IHttpResponse httpResponse;
    private ICryptoProvider cryptoProvider;
    private IEnqueueTokenProvider enqueueTokenProvider;

    /**
     * @param connectorSettings
     * @param httpServletRequest
     * @param httpServletResponse
     * @throws URISyntaxException
     */
    public ConnectorContextProvider(ConnectorSettings connectorSettings,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws URISyntaxException {

        this(connectorSettings, new HttpRequest(httpServletRequest), httpServletResponse, null);
    }

    /**
     * @param connectorSettings
     * @param httpServletRequestWrapper
     * @param httpServletResponse
     * @param enqueueTokenProvider
     * @throws URISyntaxException
     */
    public ConnectorContextProvider(ConnectorSettings connectorSettings,
            HttpServletRequestWrapper httpServletRequestWrapper,
            HttpServletResponse httpServletResponse,
            IEnqueueTokenProvider enqueueTokenProvider) throws URISyntaxException {

        this(connectorSettings, new HttpRequest(httpServletRequestWrapper), httpServletResponse, enqueueTokenProvider);
    }

    /**
     * @param connectorSettings
     * @param httpRequest
     * @param httpServletResponse
     * @param enqueueTokenProvider
     * @throws URISyntaxException
     */
    public ConnectorContextProvider(ConnectorSettings connectorSettings,
            IHttpRequest httpRequest,
            HttpServletResponse httpServletResponse,
            IEnqueueTokenProvider enqueueTokenProvider) throws URISyntaxException {

        final String cannotBeNullMessage = " cannot be null";

        if (connectorSettings == null) {
            throw new IllegalArgumentException(ConnectorSettings.class.getName() + cannotBeNullMessage);
        }

        if (httpRequest == null) {
            throw new IllegalArgumentException(HttpRequest.class.getName() + cannotBeNullMessage);
        }

        if (httpServletResponse == null) {
            throw new IllegalArgumentException(HttpServletResponse.class.getName() + cannotBeNullMessage);
        }

        this.httpRequest = httpRequest;
        this.httpResponse = new HttpResponse(httpServletResponse);
        this.cryptoProvider = new CryptoProvider();

        EnqueueTokenGenerator tokenGenerator = new EnqueueTokenGenerator(connectorSettings.getCustomerId());

        EnqueueTokenPayloadGenerator tokenPayloadGenerator = new EnqueueTokenPayloadGenerator();

        if (Boolean.TRUE.equals(connectorSettings.getEnqueueTokenEnabled())) {
            this.enqueueTokenProvider = enqueueTokenProvider != null ? enqueueTokenProvider
                    : new EnqueueTokenProvider(
                            connectorSettings.getCustomerId(),
                            connectorSettings.getSecretKey(),
                            connectorSettings.getDefaultEnqueueTokenValidityTime(),
                            httpRequest.getUserHostAddress(),
                            connectorSettings.getEnqueueTokenKeyEnabled(),
                            tokenGenerator, tokenPayloadGenerator, httpRequest.getHeader("X-FORWARDED-FOR"));
        }
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
        return cryptoProvider;
    }

    @Override
    public IEnqueueTokenProvider getEnqueueTokenProvider() {
        return enqueueTokenProvider;
    }
}
