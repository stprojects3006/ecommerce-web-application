package com.queue_it.connector;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.queue_it.connector.helpers.ConnectorDiagnostics;
import com.queue_it.connector.helpers.Utils;
import com.queue_it.connector.integrationconfig.CustomerIntegration;
import com.queue_it.connector.integrationconfig.IntegrationConfigModel;
import com.queue_it.connector.integrationconfig.IntegrationEvaluator;
import com.queue_it.connector.models.ActionType;
import com.queue_it.connector.models.CancelEventConfig;
import com.queue_it.connector.models.QueueEventConfig;

public class KnownUser {

    private KnownUser() {
    }

    private static IUserInQueueService _userInQueueService;

    public static final String QUEUEIT_DEBUG_KEY = "queueitdebug";
    public static final String QUEUEIT_AJAX_HEADER_KEY = "x-queueit-ajaxpageurl";
    public static final String QUEUEIT_TOKEN_KEY = "queueittoken";

    private static IUserInQueueService getUserInQueueService(IConnectorContextProvider connectorContextProvider) {

        if (_userInQueueService == null) {
            return new UserInQueueService(connectorContextProvider,
                    new UserInQueueStateCookieRepository(connectorContextProvider));
        }
        return _userInQueueService;
    }

    /**
     * Use for supplying explicit mock for testing purpose.
     */
    public static void setUserInQueueService(IUserInQueueService userInQueueService) {
        _userInQueueService = userInQueueService;
    }

    public static RequestValidationResult validateRequestByIntegrationConfig(String currentUrlWithoutQueueITToken,
            String queueitToken, CustomerIntegration customerIntegrationInfo, String customerId, String secretKey,
            IConnectorContextProvider connectorContextProvider)
            throws Exception {

        Map<String, String> debugEntries = new HashMap<String, String>();

        ConnectorDiagnostics connectorDiagnostics = ConnectorDiagnostics.verify(customerId, secretKey, queueitToken,
                connectorContextProvider.getCryptoProvider());

        if (connectorDiagnostics.hasError) {
            return connectorDiagnostics.validationResult;
        }

        IHttpRequest httpRequest = connectorContextProvider.getHttpRequest();

        try {
            if (connectorDiagnostics.isEnabled) {

                debugEntries.put("SdkVersion", UserInQueueService.SDK_VERSION);
                debugEntries.put("Runtime", getRuntime());
                String conVer = (customerIntegrationInfo != null) ? Integer.toString(customerIntegrationInfo.Version)
                        : "NULL";
                debugEntries.put("ConfigVersion", conVer);
                debugEntries.put("PureUrl", currentUrlWithoutQueueITToken);
                debugEntries.put("QueueitToken", queueitToken);
                debugEntries.put("OriginalUrl", httpRequest.getUri().toString());

                logMoreRequestDetails(debugEntries, httpRequest);
            }

            if (Utils.isNullOrWhiteSpace(currentUrlWithoutQueueITToken)) {
                throw new Exception("currentUrlWithoutQueueITToken can not be null or empty.");
            }

            if (customerIntegrationInfo == null) {
                throw new KnownUserException("customerIntegrationInfo can not be null.");
            }

            IntegrationEvaluator configEvaluater = new IntegrationEvaluator();

            IntegrationConfigModel matchedConfig = configEvaluater.getMatchedIntegrationConfig(customerIntegrationInfo,
                    currentUrlWithoutQueueITToken, httpRequest);

            String matchedConfigName = (matchedConfig != null) ? matchedConfig.Name : "NULL";

            if (connectorDiagnostics.isEnabled) {
                debugEntries.put("MatchedConfig", matchedConfigName);
            }

            if (matchedConfig == null) {
                return new RequestValidationResult(null, null, null, null, null, null);
            }

            // unspecified or 'Queue' specified
            if (Utils.isNullOrWhiteSpace(matchedConfig.ActionType)
                    || ActionType.QUEUE_ACTION.equals(matchedConfig.ActionType)) {
                return handleQueueAction(matchedConfig, currentUrlWithoutQueueITToken, customerIntegrationInfo,
                        queueitToken, customerId, secretKey,
                        connectorContextProvider, debugEntries, connectorDiagnostics.isEnabled);

            } else if (ActionType.CANCEL_ACTION.equals(matchedConfig.ActionType)) {
                return handleCancelAction(matchedConfig, customerIntegrationInfo, currentUrlWithoutQueueITToken,
                        queueitToken, customerId, secretKey, connectorContextProvider, debugEntries,
                        connectorDiagnostics.isEnabled);
            } // for all unknown types default to 'Ignore'
            else {
                return handleIgnoreAction(matchedConfig.Name, connectorContextProvider);
            }
        } catch (Exception e) {
            if (connectorDiagnostics.isEnabled) {
                debugEntries.put("Exception", e.getMessage());
            }
            throw e;
        } finally {
            setDebugCookie(connectorContextProvider, debugEntries);
        }
    }

    public static RequestValidationResult cancelRequestByLocalConfig(String targetUrl, String queueitToken,
            CancelEventConfig cancelConfig, String customerId, String secretKey,
            IConnectorContextProvider connectorContextProvider)
            throws Exception {

        Map<String, String> debugEntries = new HashMap<String, String>();

        ConnectorDiagnostics connectorDiagnostics = ConnectorDiagnostics.verify(customerId, secretKey, queueitToken,
                connectorContextProvider.getCryptoProvider());

        if (connectorDiagnostics.hasError) {
            return connectorDiagnostics.validationResult;
        }

        try {
            return cancelRequestByLocalConfig(targetUrl, queueitToken, cancelConfig, customerId, secretKey,
                    debugEntries,
                    connectorContextProvider,
                    connectorDiagnostics.isEnabled);
        } catch (Exception e) {
            if (connectorDiagnostics.isEnabled) {
                debugEntries.put("Exception", e.getMessage());
            }
            throw e;
        } finally {
            setDebugCookie(connectorContextProvider, debugEntries);
        }
    }

    private static RequestValidationResult cancelRequestByLocalConfig(String targetUrl, String queueitToken,
            CancelEventConfig cancelConfig, String customerId, String secretKey, Map<String, String> debugEntries,
            IConnectorContextProvider connectorContextProvider,
            boolean isDebug)
            throws Exception {

        IHttpRequest httpRequest = connectorContextProvider.getHttpRequest();
        targetUrl = generateTargetUrl(targetUrl, httpRequest);

        if (isDebug) {
            debugEntries.put("SdkVersion", UserInQueueService.SDK_VERSION);
            debugEntries.put("Runtime", getRuntime());
            debugEntries.put("TargetUrl", targetUrl);
            debugEntries.put("QueueitToken", queueitToken);
            debugEntries.put("CancelConfig", (cancelConfig != null) ? cancelConfig.toString() : "NULL");
            debugEntries.put("OriginalUrl", httpRequest.getUri().toString());

            logMoreRequestDetails(debugEntries, httpRequest);
        }

        if (Utils.isNullOrWhiteSpace(targetUrl)) {
            throw new IllegalArgumentException("targetUrl can not be null or empty.");
        }
        if (Utils.isNullOrWhiteSpace(customerId)) {
            throw new IllegalArgumentException("customerId can not be null or empty.");
        }
        if (Utils.isNullOrWhiteSpace(secretKey)) {
            throw new IllegalArgumentException("secretKey can not be null or empty.");
        }
        if (cancelConfig == null) {
            throw new IllegalArgumentException("cancelConfig can not be null.");
        }
        if (Utils.isNullOrWhiteSpace(cancelConfig.getEventId())) {
            throw new IllegalArgumentException("EventId from cancelConfig can not be null or empty.");
        }
        if (Utils.isNullOrWhiteSpace(cancelConfig.getQueueDomain())) {
            throw new IllegalArgumentException("QueueDomain from cancelConfig can not be null or empty.");
        }

        IUserInQueueService userInQueueService = getUserInQueueService(connectorContextProvider);
        RequestValidationResult result = userInQueueService.validateCancelRequest(targetUrl, cancelConfig, customerId,
                secretKey);
        result.isAjaxResult = isQueueAjaxCall(httpRequest);

        return result;
    }

    public static RequestValidationResult resolveQueueRequestByLocalConfig(
            String targetUrl, String queueitToken,
            QueueEventConfig queueConfig, String customerId, String secretKey,
            IConnectorContextProvider connectorContextProvider) throws Exception {

        ConnectorDiagnostics connectorDiagnostics = ConnectorDiagnostics.verify(customerId, secretKey, queueitToken,
                connectorContextProvider.getCryptoProvider());

        if (connectorDiagnostics.hasError) {
            return connectorDiagnostics.validationResult;
        }

        Map<String, String> debugEntries = new HashMap<String, String>();

        try {
            targetUrl = generateTargetUrl(targetUrl, connectorContextProvider.getHttpRequest());

            return resolveQueueRequestByLocalConfig(targetUrl, queueitToken, queueConfig, customerId, secretKey,
                    connectorContextProvider,
                    debugEntries, connectorDiagnostics.isEnabled);
        } catch (Exception e) {
            if (connectorDiagnostics.isEnabled) {
                debugEntries.put("Exception", e.getMessage());
            }
            throw e;
        } finally {
            setDebugCookie(connectorContextProvider, debugEntries);
        }
    }

    private static RequestValidationResult resolveQueueRequestByLocalConfig(String targetUrl, String queueitToken,
            QueueEventConfig queueConfig, String customerId, String secretKey,
            IConnectorContextProvider connectorContextProvider,
            Map<String, String> debugEntries, boolean isDebug) throws Exception {

        IHttpRequest httpRequest = connectorContextProvider.getHttpRequest();

        if (isDebug) {
            debugEntries.put("SdkVersion", UserInQueueService.SDK_VERSION);
            debugEntries.put("Runtime", getRuntime());
            debugEntries.put("TargetUrl", targetUrl);
            debugEntries.put("QueueitToken", queueitToken);
            debugEntries.put("QueueConfig", (queueConfig != null) ? queueConfig.toString() : "NULL");
            debugEntries.put("OriginalUrl", httpRequest.getUri().toString());

            logMoreRequestDetails(debugEntries, httpRequest);
        }

        if (Utils.isNullOrWhiteSpace(customerId)) {
            throw new IllegalArgumentException("customerId can not be null or empty.");
        }
        if (Utils.isNullOrWhiteSpace(secretKey)) {
            throw new IllegalArgumentException("secretKey can not be null or empty.");
        }
        if (queueConfig == null) {
            throw new IllegalArgumentException("eventConfig can not be null.");
        }
        if (Utils.isNullOrWhiteSpace(queueConfig.getEventId())) {
            throw new IllegalArgumentException("EventId from queueConfig can not be null or empty.");
        }
        if (Utils.isNullOrWhiteSpace(queueConfig.getQueueDomain())) {
            throw new IllegalArgumentException("QueueDomain from queueConfig can not be null or empty.");
        }
        if (queueConfig.getCookieValidityMinute() <= 0) {
            throw new IllegalArgumentException("cookieValidityMinute from queueConfig should be greater than 0.");
        }
        if (queueitToken == null) {
            queueitToken = "";
        }

        IUserInQueueService userInQueueService = getUserInQueueService(connectorContextProvider);
        RequestValidationResult result = userInQueueService.validateQueueRequest(targetUrl, queueitToken, queueConfig,
                customerId, secretKey);
        result.isAjaxResult = isQueueAjaxCall(httpRequest);

        return result;
    }

    public static void extendQueueCookie(String eventId,
            int cookieValidityMinute,
            String cookieDomain,
            Boolean isCookieHttpOnly,
            Boolean isCookieSecure,
            HttpServletRequest request,
            HttpServletResponse response,
            String secretKey,
            IConnectorContextProvider connectorContextProvider) throws Exception {

        if (Utils.isNullOrWhiteSpace(eventId)) {
            throw new IllegalArgumentException("eventId can not be null or empty.");
        }
        if (cookieValidityMinute <= 0) {
            throw new IllegalArgumentException("cookieValidityMinute should be greater than 0.");
        }
        if (Utils.isNullOrWhiteSpace(secretKey)) {
            throw new IllegalArgumentException("secretKey can not be null or empty.");
        }

        _userInQueueService.extendQueueCookie(eventId, cookieValidityMinute, cookieDomain, isCookieHttpOnly,
                isCookieSecure, secretKey);
    }

    private static void setDebugCookie(IConnectorContextProvider connectorContextProvider,
            Map<String, String> debugEntries)
            throws UnsupportedEncodingException {

        if (debugEntries.isEmpty()) {
            return;
        }

        String cookieValue = "";
        for (Map.Entry<String, String> entry : debugEntries.entrySet()) {
            cookieValue += (entry.getKey() + "=" + entry.getValue() + "|");
        }
        if (!"".equals(cookieValue)) {
            cookieValue = cookieValue.substring(0, cookieValue.length() - 1); // remove trailing char
        }

        connectorContextProvider.getHttpResponse().setCookie(QUEUEIT_DEBUG_KEY, cookieValue, 1200, null, false,
                false);
    }

    private static void logMoreRequestDetails(Map<String, String> debugEntries, IHttpRequest request) {

        TimeZone tz = TimeZone.getTimeZone("UTC");
        // Quoted "Z" to indicate UTC, no timezone offset
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());

        debugEntries.put("ServerUtcTime", nowAsISO);
        debugEntries.put("RequestIP", request.getUserHostAddress());
        debugEntries.put("RequestHttpHeader_Via",
                request.getHeader("via") != null ? request.getHeader("via") : "");
        debugEntries.put("RequestHttpHeader_Forwarded",
                request.getHeader("forwarded") != null ? request.getHeader("forwarded") : "");
        debugEntries.put("RequestHttpHeader_XForwardedFor",
                request.getHeader("x-forwarded-for") != null ? request.getHeader("x-forwarded-for") : "");
        debugEntries.put("RequestHttpHeader_XForwardedHost",
                request.getHeader("x-forwarded-host") != null ? request.getHeader("x-forwarded-host") : "");
        debugEntries.put("RequestHttpHeader_XForwardedProto",
                request.getHeader("x-forwarded-proto") != null ? request.getHeader("x-forwarded-proto") : "");
    }

    private static RequestValidationResult handleQueueAction(IntegrationConfigModel matchedConfig,
            String currentUrlWithoutQueueITToken,
            CustomerIntegration customerIntegrationInfo, String queueitToken, String customerId, String secretKey,
            IConnectorContextProvider connectorContextProvider, Map<String, String> debugEntries, boolean isDebug)
            throws Exception {
        String targetUrl;

        IHttpRequest httpRequest = connectorContextProvider.getHttpRequest();

        if ("ForecedTargetUrl".equals(matchedConfig.RedirectLogic)
                || // support for typo
                "ForcedTargetUrl".equals(matchedConfig.RedirectLogic)) {
            targetUrl = matchedConfig.ForcedTargetUrl;
        } else if ("EventTargetUrl".equals(matchedConfig.RedirectLogic)) {
            targetUrl = "";
        } else {

            targetUrl = generateTargetUrl(currentUrlWithoutQueueITToken, httpRequest);
        }

        QueueEventConfig queueConfig = new QueueEventConfig();
        queueConfig.setQueueDomain(matchedConfig.QueueDomain);
        queueConfig.setCulture(matchedConfig.Culture);
        queueConfig.setEventId(matchedConfig.EventId);
        queueConfig.setExtendCookieValidity(matchedConfig.ExtendCookieValidity);
        queueConfig.setLayoutName(matchedConfig.LayoutName);
        queueConfig.setCookieValidityMinute(matchedConfig.CookieValidityMinute);
        queueConfig.setCookieDomain(matchedConfig.CookieDomain);
        queueConfig.setIsCookieHttpOnly(matchedConfig.IsCookieHttpOnly);
        queueConfig.setIsCookieSecure(matchedConfig.IsCookieSecure);
        queueConfig.setVersion(customerIntegrationInfo.Version);
        queueConfig.setActionName(matchedConfig.Name);

        return resolveQueueRequestByLocalConfig(targetUrl, queueitToken, queueConfig, customerId, secretKey,
                connectorContextProvider,
                debugEntries, isDebug);
    }

    private static RequestValidationResult handleCancelAction(IntegrationConfigModel matchedConfig,
            CustomerIntegration customerIntegrationInfo, String currentUrlWithoutQueueITToken, String queueitToken,
            String customerId, String secretKey, IConnectorContextProvider connectorContextProvider,
            Map<String, String> debugEntries, boolean isDebug) throws Exception {

        CancelEventConfig cancelConfig = new CancelEventConfig();
        cancelConfig.setQueueDomain(matchedConfig.QueueDomain);
        cancelConfig.setEventId(matchedConfig.EventId);
        cancelConfig.setCookieDomain(matchedConfig.CookieDomain);
        cancelConfig.setIsCookieHttpOnly(matchedConfig.IsCookieHttpOnly);
        cancelConfig.setIsCookieSecure(matchedConfig.IsCookieSecure);
        cancelConfig.setVersion(customerIntegrationInfo.Version);
        cancelConfig.setActionName(matchedConfig.Name);

        String targetUrl = generateTargetUrl(currentUrlWithoutQueueITToken, connectorContextProvider.getHttpRequest());

        return cancelRequestByLocalConfig(targetUrl, queueitToken, cancelConfig, customerId,
                secretKey, debugEntries, connectorContextProvider, isDebug);
    }

    private static RequestValidationResult handleIgnoreAction(String actionName,
            IConnectorContextProvider connectorContextProvider) {

        IUserInQueueService userInQueueService = getUserInQueueService(connectorContextProvider);
        RequestValidationResult result = userInQueueService.getIgnoreActionResult(actionName);
        result.isAjaxResult = isQueueAjaxCall(connectorContextProvider.getHttpRequest());
        return result;
    }

    private static String generateTargetUrl(String originalTargetUrl, IHttpRequest request) {
        try {
            return !isQueueAjaxCall(request) ? originalTargetUrl
                    : URLDecoder.decode(request.getHeader(QUEUEIT_AJAX_HEADER_KEY), "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        return "";
    }

    private static boolean isQueueAjaxCall(IHttpRequest request) {
        return !Utils.isNullOrWhiteSpace(request.getHeader(QUEUEIT_AJAX_HEADER_KEY));
    }

    public static String getRuntime() {
        try {
            return URLEncoder.encode(System.getProperty("java.runtime.version"), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return "unknown";
        }
    }
}
