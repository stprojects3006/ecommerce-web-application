package com.queue_it.connector;

import java.util.ArrayList;

import com.queue_it.connector.helpers.HashHelper;
import com.queue_it.connector.helpers.QueueParameterHelper;
import com.queue_it.connector.helpers.QueueUrlParams;
import com.queue_it.connector.helpers.Utils;
import com.queue_it.connector.models.ActionType;
import com.queue_it.connector.models.CancelEventConfig;
import com.queue_it.connector.models.QueueEventConfig;
import com.queue_it.connector.models.StateInfo;

public class UserInQueueService implements IUserInQueueService {

    public static final String SDK_VERSION = "java-4.0.6";

    private static String generateRedirectUrl(String queueDomain, String uriPath, String query) throws Exception {
        if (!queueDomain.endsWith("/")) {
            queueDomain = queueDomain + "/";
        }
        return "https://" + queueDomain + uriPath + "?" + query;
    }

    public final IConnectorContextProvider connectorContextProvider;

    public final IUserInQueueStateRepository userInQueueStateRepository;

    public UserInQueueService(IConnectorContextProvider connectorContextProvider,
            IUserInQueueStateRepository queueStateRepository) {
        this.connectorContextProvider = connectorContextProvider;
        this.userInQueueStateRepository = queueStateRepository;
    }

    @Override
    public RequestValidationResult validateQueueRequest(
            String targetUrl,
            String queueitToken,
            QueueEventConfig config,
            String customerId,
            String secretKey) throws Exception {

        StateInfo stateInfo = this.userInQueueStateRepository.getState(config.getEventId(),
                config.getCookieValidityMinute(), secretKey, true);

        if (stateInfo.isValid()) {
            if (stateInfo.isStateExtendable() && config.getExtendCookieValidity()) {
                this.userInQueueStateRepository.store(
                        config.getEventId(),
                        stateInfo.getQueueId(),
                        null,
                        config.getCookieDomain(),
                        config.getIsCookieHttpOnly(),
                        config.getIsCookieSecure(),
                        stateInfo.getRedirectType(),
                        "",
                        secretKey);
            }
            return new RequestValidationResult(ActionType.QUEUE_ACTION, config.getEventId(), stateInfo.getQueueId(),
                    null, stateInfo.getRedirectType(), config.getActionName());
        }

        QueueUrlParams queueParams = QueueParameterHelper.extractQueueParams(queueitToken);
        RequestValidationResult requestValidationResult;
        boolean isTokenValid = false;

        if (queueParams != null) {
            TokenValidationResult tokenValidationResult = validateToken(config, queueParams, secretKey);
            isTokenValid = tokenValidationResult.isValid();

            if (isTokenValid) {
                requestValidationResult = getValidTokenResult(config, queueParams, secretKey);
            } else {
                requestValidationResult = getErrorResult(customerId, targetUrl, config, queueParams,
                        tokenValidationResult.getErrorCode());
            }
        } else {
            requestValidationResult = getQueueResult(targetUrl, config, customerId);
        }

        if (stateInfo.isFound() && !isTokenValid) {
            this.userInQueueStateRepository.cancelQueueCookie(config.getEventId(), config.getCookieDomain(),
                    config.getIsCookieHttpOnly(), config.getIsCookieSecure());
        }
        return requestValidationResult;
    }

    @Override
    public void extendQueueCookie(
            String eventId,
            int cookieValidityMinute,
            String cookieDomain,
            Boolean isCookieHttpOnly,
            Boolean isCookieSecure,
            String secretKey) {
        this.userInQueueStateRepository.reissueQueueCookie(eventId, cookieValidityMinute, cookieDomain,
                isCookieHttpOnly, isCookieSecure, secretKey);
    }

    @Override
    public RequestValidationResult validateCancelRequest(
            String targetUrl,
            CancelEventConfig config,
            String customerId,
            String secretKey) throws Exception {
        final int cookieValidityMinutes = -1;

        StateInfo state = userInQueueStateRepository.getState(config.getEventId(), cookieValidityMinutes, secretKey,
                false);

        if (state.isValid()) {
            this.userInQueueStateRepository.cancelQueueCookie(config.getEventId(), config.getCookieDomain(),
                    config.getIsCookieHttpOnly(), config.getIsCookieSecure());

            StringBuilder uriPathBuilder = new StringBuilder("cancel/");
            uriPathBuilder.append(customerId);
            uriPathBuilder.append("/");
            uriPathBuilder.append(config.getEventId());

            String queueId = state.getQueueId();

            if (!Utils.isNullOrWhiteSpace(queueId)) {
                uriPathBuilder.append("/");
                uriPathBuilder.append(queueId);
            }

            String uriPath = uriPathBuilder.toString();

            String query = getQueryString(customerId, config.getEventId(), config.getVersion(),
                    config.getActionName(),
                    null, null, null);

            if (!Utils.isNullOrWhiteSpace(targetUrl)) {
                query += "&r=" + Utils.encodeUrl(targetUrl);
            }

            String redirectUrl = generateRedirectUrl(config.getQueueDomain(), uriPath, query);

            return new RequestValidationResult(ActionType.CANCEL_ACTION, config.getEventId(), state.getQueueId(),
                    redirectUrl, state.getRedirectType(), config.getActionName());
        } else {
            return new RequestValidationResult(ActionType.CANCEL_ACTION, config.getEventId(), null, null, null,
                    config.getActionName());
        }
    }

    @Override
    public RequestValidationResult getIgnoreActionResult(String actionName) {
        return new RequestValidationResult(ActionType.IGNORE_ACTION, null, null, null, null, actionName);
    }

    private RequestValidationResult getValidTokenResult(
            QueueEventConfig config,
            QueueUrlParams queueParams,
            String secretKey) throws Exception {

        this.userInQueueStateRepository.store(
                config.getEventId(),
                queueParams.getQueueId(),
                queueParams.getCookieValidityMinutes(),
                config.getCookieDomain(),
                config.getIsCookieHttpOnly(),
                config.getIsCookieSecure(),
                queueParams.getRedirectType(),
                queueParams.getHashedIp(),
                secretKey);

        return new RequestValidationResult(
                ActionType.QUEUE_ACTION,
                config.getEventId(),
                queueParams.getQueueId(),
                null,
                queueParams.getRedirectType(),
                config.getActionName());
    }

    private RequestValidationResult getErrorResult(
            String customerId,
            String targetUrl,
            QueueEventConfig config,
            QueueUrlParams qParams,
            String errorCode) throws Exception {

        String query = getQueryString(customerId, config.getEventId(), config.getVersion(), config.getActionName(),
                config.getCulture(), config.getLayoutName(), null)
                + "&queueittoken=" + qParams.getQueueITToken() + "&ts=" + System.currentTimeMillis() / 1000L;

        if (!Utils.isNullOrWhiteSpace(targetUrl)) {
            query += "&t=" + Utils.encodeUrl(targetUrl);
        }

        String redirectUrl = generateRedirectUrl(config.getQueueDomain(), "error/" + errorCode + "/", query);

        return new RequestValidationResult(ActionType.QUEUE_ACTION, config.getEventId(), null, redirectUrl, null,
                config.getActionName());
    }

    private RequestValidationResult getQueueResult(
            String targetUrl,
            QueueEventConfig config,
            String customerId) throws Exception {
        String enqueueToken = "";
        IEnqueueTokenProvider enqueueTokenProvider = connectorContextProvider.getEnqueueTokenProvider();
        if (enqueueTokenProvider != null) {
            enqueueToken = enqueueTokenProvider.getEnqueueToken(config.getEventId());
        }

        String query = getQueryString(customerId, config.getEventId(), config.getVersion(), config.getActionName(),
                config.getCulture(), config.getLayoutName(), enqueueToken);

        if (!Utils.isNullOrWhiteSpace(targetUrl)) {
            query += "&t=" + Utils.encodeUrl(targetUrl);

        }
        String redirectUrl = generateRedirectUrl(config.getQueueDomain(), "", query);

        return new RequestValidationResult(ActionType.QUEUE_ACTION, config.getEventId(), null, redirectUrl, null,
                config.getActionName());
    }

    private String getQueryString(String customerId, String eventId, int configVersion, String actionName,
            String culture, String layoutName, String enqueueToken) throws Exception {

        ArrayList<String> queryStringList = new ArrayList<String>();
        queryStringList.add("c=" + Utils.encodeUrl(customerId));
        queryStringList.add("e=" + Utils.encodeUrl(eventId));
        queryStringList.add("ver=" + Utils.encodeUrl(SDK_VERSION));
        queryStringList.add("cver=" + Utils.encodeUrl(String.valueOf(configVersion)));
        queryStringList.add("man=" + Utils.encodeUrl(actionName));

        if (!Utils.isNullOrWhiteSpace(culture)) {
            queryStringList.add("cid=" + Utils.encodeUrl(culture));
        }

        if (!Utils.isNullOrWhiteSpace(layoutName)) {
            queryStringList.add("l=" + Utils.encodeUrl(layoutName));
        }

        if (!Utils.isNullOrWhiteSpace(enqueueToken)) {
            queryStringList.add("enqueuetoken=" + Utils.encodeUrl(enqueueToken));
        }

        return Utils.join("&", queryStringList);
    }

    private TokenValidationResult validateToken(QueueEventConfig config, QueueUrlParams queueParams, String secretKey)
            throws Exception {

        String calculatedHash = HashHelper.generateSHA256Hash(secretKey, queueParams.getQueueITTokenWithoutHash());
        if (!calculatedHash.equalsIgnoreCase(queueParams.getHashCode())) {
            return new TokenValidationResult(false, "hash");
        }

        if (!config.getEventId().equalsIgnoreCase(queueParams.getEventId())) {
            return new TokenValidationResult(false, "eventid");
        }

        if (queueParams.getTimeStamp() < System.currentTimeMillis() / 1000L) {
            return new TokenValidationResult(false, "timestamp");
        }

        // Validate IP Binding
        String clientIp = connectorContextProvider.getHttpRequest().getUserHostAddress();

        if (!Utils.isNullOrWhiteSpace(queueParams.getHashedIp()) && !Utils.isNullOrWhiteSpace(clientIp)) {
            String hashedClientIp = connectorContextProvider.getCryptoProvider().generateSHA256Hash(secretKey,
                    clientIp);
            if (!queueParams.getHashedIp().equalsIgnoreCase(hashedClientIp)) {
                return new TokenValidationResult(false, "ip");
            }
        }
        return new TokenValidationResult(true, null);
    }

    private class TokenValidationResult {

        private final boolean isValid;
        private final String errorCode;

        public TokenValidationResult(boolean isValid, String errorCode) {
            this.isValid = isValid;
            this.errorCode = errorCode;
        }

        public boolean isValid() {
            return this.isValid;
        }

        public String getErrorCode() {
            return this.errorCode;
        }
    }
}
