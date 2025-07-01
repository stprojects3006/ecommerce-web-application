package com.queue_it.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;

import org.junit.Before;
import org.junit.Test;

import com.queue_it.connector.IHttpRequest;
import com.queue_it.connector.RequestValidationResult;
import com.queue_it.connector.UserInQueueService;
import com.queue_it.connector.integrationconfig.CustomerIntegration;
import com.queue_it.connector.integrationconfig.IntegrationConfigModel;
import com.queue_it.connector.integrationconfig.TriggerModel;
import com.queue_it.connector.integrationconfig.TriggerPart;
import com.queue_it.connector.HttpRequest;
import com.queue_it.connector.HttpResponse;
import com.queue_it.connector.KnownUser;
import com.queue_it.connector.KnownUserRequestWrapper;
import com.queue_it.connector.models.ActionType;
import com.queue_it.connector.models.CancelEventConfig;
import com.queue_it.connector.models.QueueEventConfig;
import com.queue_it.connector.mocks.ConnectorContextProviderMock;
import com.queue_it.connector.mocks.HttpServletRequestWithPathInfoMock;
import com.queue_it.connector.mocks.UserInQueueServiceMock;

public class KnownUserTest {

    public static ConnectorContextProviderMock connectorContextProviderMock;
    public static UserInQueueServiceMock userInQueueServiceMock;

    @Before
    public void setup() throws URISyntaxException {
        connectorContextProviderMock = new ConnectorContextProviderMock();
        userInQueueServiceMock = new UserInQueueServiceMock(connectorContextProviderMock);
        KnownUser.setUserInQueueService(userInQueueServiceMock);
    }

    @Test
    public void cancelRequestByLocalConfigTest() throws Exception {
        // Arrange
        CancelEventConfig cancelEventConfig = new CancelEventConfig();
        cancelEventConfig.setCookieDomain("cookiedomain");
        cancelEventConfig.setEventId("eventid");
        cancelEventConfig.setQueueDomain("queuedomain");
        cancelEventConfig.setVersion(1);
        cancelEventConfig.setActionName("cancelAction");

        // Act
        RequestValidationResult result = KnownUser.cancelRequestByLocalConfig("url", "queueitToken", cancelEventConfig,
                "customerid", "secretkey", connectorContextProviderMock);

        // Assert
        assertEquals("url", userInQueueServiceMock.validateCancelRequestCalls.get(0).get(0));
        assertEquals("cookiedomain:eventid:queuedomain:1:cancelAction",
                userInQueueServiceMock.validateCancelRequestCalls.get(0).get(1));
        assertEquals("customerid", userInQueueServiceMock.validateCancelRequestCalls.get(0).get(2));
        assertEquals("secretkey", userInQueueServiceMock.validateCancelRequestCalls.get(0).get(3));
        assertFalse(result.isAjaxResult);
    }

    @Test
    public void CancelRequestByLocalConfig_AjaxCall_Test() throws Exception {

        // Arrange

        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.Headers.put("x-queueit-ajaxpageurl", "http%3A%2F%2Furl");
        KnownUserRequestWrapperMock knownUserRequestWrapperMock = new KnownUserRequestWrapperMock(requestMock);
        IHttpRequest httpRequestMock = new HttpRequest(knownUserRequestWrapperMock);
        connectorContextProviderMock.httpRequest = httpRequestMock;

        CancelEventConfig cancelEventConfig = new CancelEventConfig();
        cancelEventConfig.setCookieDomain("cookiedomain");
        cancelEventConfig.setEventId("eventid");
        cancelEventConfig.setQueueDomain("queuedomain");
        cancelEventConfig.setVersion(1);
        cancelEventConfig.setActionName("cancelAction");

        // Act
        RequestValidationResult result = KnownUser.cancelRequestByLocalConfig("url", "queueitToken", cancelEventConfig,
                "customerid", "secretkey", connectorContextProviderMock);

        // Assert
        assertEquals("http://url", userInQueueServiceMock.validateCancelRequestCalls.get(0).get(0));
        assertEquals("cookiedomain:eventid:queuedomain:1:cancelAction",
                userInQueueServiceMock.validateCancelRequestCalls.get(0).get(1));
        assertEquals("customerid", userInQueueServiceMock.validateCancelRequestCalls.get(0).get(2));
        assertEquals("secretkey", userInQueueServiceMock.validateCancelRequestCalls.get(0).get(3));
        assertTrue(result.isAjaxResult);
    }

    @Test
    public void cancelRequestByLocalConfigDebugCookieLoggingTest() throws Exception {

        // Arrange
        CancelEventConfig cancelEventConfig = new CancelEventConfig();
        cancelEventConfig.setCookieDomain("cookiedomain");
        cancelEventConfig.setEventId("eventid");
        cancelEventConfig.setQueueDomain("queuedomain");
        cancelEventConfig.setVersion(1);
        cancelEventConfig.setActionName("cancelAction");

        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.RequestURL = "requestUrl";
        requestMock.RemoteAddr = "80.35.35.34";
        requestMock.Headers.put("via", "1.1 example.com");
        requestMock.Headers.put("forwarded", "for=192.0.2.60;proto=http;by=203.0.113.43");
        requestMock.Headers.put("x-forwarded-for", "129.78.138.66, 129.78.64.103");
        requestMock.Headers.put("x-forwarded-host", "en.wikipedia.org:8080");
        requestMock.Headers.put("x-forwarded-proto", "https");

        HttpServletResponseMock responseMock = new HttpServletResponseMock();

        KnownUserRequestWrapperMock knownUserRequestWrapperMock = new KnownUserRequestWrapperMock(requestMock);
        connectorContextProviderMock.httpRequest = new HttpRequest(knownUserRequestWrapperMock);
        connectorContextProviderMock.httpResponse = new HttpResponse(responseMock);

        // Act
        String secretKey = "secretkey";
        Date date = new Date();
        date = addDays(date, 1);
        String queueittoken = UserInQueueServiceTest.QueueITTokenGenerator.generateToken(date, "eventId", false,
                10, secretKey, "debug", "");

        KnownUser.cancelRequestByLocalConfig("url", queueittoken, cancelEventConfig, "customerId", secretKey,
                connectorContextProviderMock);

        // Assert
        assertEquals(1, responseMock.addedCookies.size());
        assertEquals(KnownUser.QUEUEIT_DEBUG_KEY, responseMock.addedCookies.get(0).getName());
        String decodedCookieValue = URLDecoder.decode(responseMock.addedCookies.get(0).getValue(),
                "UTF-8");
        assertTrue(decodedCookieValue.contains("OriginalUrl=requestUrl"));
        assertTrue(decodedCookieValue.contains("CancelConfig=EventId:eventid"));
        assertTrue(decodedCookieValue.contains("&Version:1"));
        assertTrue(decodedCookieValue.contains("&QueueDomain:queuedomain"));
        assertTrue(decodedCookieValue.contains("&CookieDomain:cookiedomain"));
        assertTrue(decodedCookieValue.contains("QueueitToken=" + queueittoken));
        assertTrue(decodedCookieValue.contains("TargetUrl=url"));
        assertTrue(decodedCookieValue.contains("RequestIP=80.35.35.34"));
        assertTrue(decodedCookieValue.contains("RequestHttpHeader_Via=1.1 example.com"));
        assertTrue(
                decodedCookieValue.contains("RequestHttpHeader_Forwarded=for=192.0.2.60;proto=http;by=203.0.113.43"));
        assertTrue(decodedCookieValue.contains("RequestHttpHeader_XForwardedFor=129.78.138.66, 129.78.64.103"));
        assertTrue(decodedCookieValue.contains("RequestHttpHeader_XForwardedHost=en.wikipedia.org:8080"));
        assertTrue(decodedCookieValue.contains("RequestHttpHeader_XForwardedProto=https"));
        assertTrue(decodedCookieValue.contains("&ActionName:cancelAction"));
    }

    @Test
    public void cancelRequestByLocalConfigNullQueueDomainTest() throws URISyntaxException {

        // Arrange
        boolean exceptionWasThrown = false;
        CancelEventConfig cancelEventConfig = new CancelEventConfig();
        cancelEventConfig.setEventId("eventid");
        cancelEventConfig.setCookieDomain("cookieDomain");
        cancelEventConfig.setVersion(12);

        // Act
        try {
            KnownUser.cancelRequestByLocalConfig("targetUrl", "queueitToken", cancelEventConfig, "customerId",
                    "secretKey", connectorContextProviderMock);
        } catch (Exception ex) {
            exceptionWasThrown = "QueueDomain from cancelConfig can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(userInQueueServiceMock.validateCancelRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void cancelRequestByLocalConfigEventIdNullTest() throws URISyntaxException {

        // Arrange
        boolean exceptionWasThrown = false;
        CancelEventConfig cancelEventConfig = new CancelEventConfig();
        cancelEventConfig.setCookieDomain("domain");
        cancelEventConfig.setVersion(12);

        // Act
        try {
            KnownUser.cancelRequestByLocalConfig("targetUrl", "queueitToken", cancelEventConfig, "customerId",
                    "secretKey", connectorContextProviderMock);

        } catch (Exception ex) {
            exceptionWasThrown = "EventId from cancelConfig can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(userInQueueServiceMock.validateCancelRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void cancelRequestByLocalConfigCancelEventConfigNullTest() throws URISyntaxException {

        // Arrange
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.cancelRequestByLocalConfig("targetUrl", "queueitToken", null, "customerId", "secretKey",
                    connectorContextProviderMock);
        } catch (Exception ex) {
            exceptionWasThrown = "cancelConfig can not be null.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(userInQueueServiceMock.validateCancelRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void cancelRequestByLocalConfigCustomerIdNullTest() throws URISyntaxException {

        // Arrange
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.cancelRequestByLocalConfig("targetUrl", "queueitToken", new CancelEventConfig(), null,
                    "secretKey", connectorContextProviderMock);
        } catch (Exception ex) {
            exceptionWasThrown = "customerId can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(userInQueueServiceMock.validateCancelRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void cancelRequestByLocalConfigSecretKeyNullTest() throws URISyntaxException {
        // Arrange
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.cancelRequestByLocalConfig("targetUrl", "queueitToken", new CancelEventConfig(), "customerId",
                    null, connectorContextProviderMock);
        } catch (Exception ex) {
            exceptionWasThrown = "secretKey can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(userInQueueServiceMock.validateCancelRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void CancelRequestByLocalConfigTargetUrlNullTest() throws URISyntaxException {
        // Arrange
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.cancelRequestByLocalConfig(null, "queueitToken", new CancelEventConfig(), "customerId",
                    "secretKey", connectorContextProviderMock);
        } catch (Exception ex) {
            exceptionWasThrown = "targetUrl can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(userInQueueServiceMock.validateCancelRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void extendQueueCookieNullEventIdTest() throws URISyntaxException {
        // Arrange
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.extendQueueCookie(null, 0, null, null, null, null, null, null, null);
        } catch (Exception ex) {
            exceptionWasThrown = "eventId can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(userInQueueServiceMock.extendQueueCookieCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void extendQueueCookieInvalidCookieValidityMinutesTest() throws URISyntaxException {
        // Arrange
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.extendQueueCookie("eventId", 0, null, null, null, null, null, null, null);
        } catch (Exception ex) {
            exceptionWasThrown = "cookieValidityMinute should be greater than 0.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(userInQueueServiceMock.extendQueueCookieCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void extendQueueCookieNullSecretKeyTest() throws URISyntaxException {
        // Arrange
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.extendQueueCookie("eventId", 20, null, null, null, null, null, null, null);
        } catch (Exception ex) {
            exceptionWasThrown = "secretKey can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(userInQueueServiceMock.extendQueueCookieCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void extendQueueCookieTest() throws Exception {
        // Arrange

        // Act
        KnownUser.extendQueueCookie("eventId", 20, "cookieDomain", true, false, null, null, "secretKey", null);

        // Assert
        assertEquals("eventId", userInQueueServiceMock.extendQueueCookieCalls.get(0).get(0));
        assertEquals("20", userInQueueServiceMock.extendQueueCookieCalls.get(0).get(1));
        assertEquals("cookieDomain", userInQueueServiceMock.extendQueueCookieCalls.get(0).get(2));
        assertEquals("secretKey", userInQueueServiceMock.extendQueueCookieCalls.get(0).get(3));
        assertTrue(Boolean.parseBoolean(userInQueueServiceMock.extendQueueCookieCalls.get(0).get(4)));
        assertFalse(Boolean.parseBoolean(userInQueueServiceMock.extendQueueCookieCalls.get(0).get(5)));
    }

    @Test
    public void resolveQueueRequestByLocalConfigNullCustomerIdTest() throws URISyntaxException {

        // Arrange
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.resolveQueueRequestByLocalConfig("targetUrl", "queueitToken", null, null, "secretKey",
                    connectorContextProviderMock);
        } catch (Exception ex) {
            exceptionWasThrown = "customerId can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(userInQueueServiceMock.validateQueueRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void resolveQueueRequestByLocalConfigNullSecretKeyTest() throws URISyntaxException {
        // Arrange
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.resolveQueueRequestByLocalConfig("targetUrl", "queueitToken", null, "customerId", null,
                    connectorContextProviderMock);
        } catch (Exception ex) {
            exceptionWasThrown = "secretKey can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(userInQueueServiceMock.validateQueueRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void resolveQueueRequestByLocalConfigNullEventConfigTest() throws URISyntaxException {
        // Arrange
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.resolveQueueRequestByLocalConfig("targetUrl", "queueitToken", null, "customerId",
                    "secretKey", connectorContextProviderMock);
        } catch (Exception ex) {
            exceptionWasThrown = "eventConfig can not be null.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(userInQueueServiceMock.validateQueueRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void resolveQueueRequestByLocalConfigNullEventIdTest() throws URISyntaxException {

        // Arrange
        boolean exceptionWasThrown = false;

        QueueEventConfig eventConfig = new QueueEventConfig();
        eventConfig.setCookieDomain("cookieDomain");
        eventConfig.setLayoutName("layoutName");
        eventConfig.setCulture("culture");
        eventConfig.setQueueDomain("queueDomain");
        eventConfig.setExtendCookieValidity(true);
        eventConfig.setCookieValidityMinute(10);
        eventConfig.setVersion(12);

        // Act
        try {
            KnownUser.resolveQueueRequestByLocalConfig("targetUrl", "queueitToken", eventConfig, "customerId",
                    "secretKey", connectorContextProviderMock);
        } catch (Exception ex) {
            exceptionWasThrown = "EventId from queueConfig can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(userInQueueServiceMock.validateQueueRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void resolveQueueRequestByLocalConfigNullQueueDomainTest() throws URISyntaxException {
        // Arrange
        boolean exceptionWasThrown = false;

        QueueEventConfig eventConfig = new QueueEventConfig();
        eventConfig.setCookieDomain("cookieDomain");
        eventConfig.setLayoutName("layoutName");
        eventConfig.setCulture("culture");
        eventConfig.setEventId("eventId");
        eventConfig.setExtendCookieValidity(true);
        eventConfig.setCookieValidityMinute(10);
        eventConfig.setVersion(12);
        eventConfig.setActionName("queueAction");

        // Act
        try {
            KnownUser.resolveQueueRequestByLocalConfig("targetUrl", "queueitToken", eventConfig, "customerId",
                    "secretKey", connectorContextProviderMock);
        } catch (Exception ex) {
            exceptionWasThrown = "QueueDomain from queueConfig can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(userInQueueServiceMock.validateQueueRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void resolveQueueRequestByLocalConfigInvalidCookieValidityMinuteTest() throws URISyntaxException {
        // Arrange
        boolean exceptionWasThrown = false;

        QueueEventConfig eventConfig = new QueueEventConfig();
        eventConfig.setCookieDomain("cookieDomain");
        eventConfig.setLayoutName("layoutName");
        eventConfig.setCulture("culture");
        eventConfig.setEventId("eventId");
        eventConfig.setQueueDomain("queueDomain");
        eventConfig.setExtendCookieValidity(true);
        eventConfig.setVersion(12);

        // Act
        try {
            KnownUser.resolveQueueRequestByLocalConfig("targetUrl", "queueitToken", eventConfig, "customerId",
                    "secretKey", connectorContextProviderMock);
        } catch (Exception ex) {
            exceptionWasThrown = "cookieValidityMinute from queueConfig should be greater than 0."
                    .equals(ex.getMessage());
        }

        // Assert
        assertTrue(userInQueueServiceMock.validateQueueRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void resolveQueueRequestByLocalConfigTest() throws Exception {

        // Arrange
        QueueEventConfig eventConfig = new QueueEventConfig();
        eventConfig.setCookieDomain("cookieDomain");
        eventConfig.setLayoutName("layoutName");
        eventConfig.setCulture("culture");
        eventConfig.setEventId("eventId");
        eventConfig.setQueueDomain("queueDomain");
        eventConfig.setExtendCookieValidity(true);
        eventConfig.setCookieValidityMinute(10);
        eventConfig.setVersion(12);
        eventConfig.setActionName("queueAction");

        // Act
        RequestValidationResult result = KnownUser.resolveQueueRequestByLocalConfig("targetUrl", "queueitToken",
                eventConfig, "customerId", "secretKey", connectorContextProviderMock);

        // Assert
        assertEquals("targetUrl", userInQueueServiceMock.validateQueueRequestCalls.get(0).get(0));
        assertEquals("queueitToken", userInQueueServiceMock.validateQueueRequestCalls.get(0).get(1));
        assertEquals("cookieDomain:layoutName:culture:eventId:queueDomain:true:10:12:queueAction",
                userInQueueServiceMock.validateQueueRequestCalls.get(0).get(2));
        assertEquals("customerId", userInQueueServiceMock.validateQueueRequestCalls.get(0).get(3));
        assertEquals("secretKey", userInQueueServiceMock.validateQueueRequestCalls.get(0).get(4));
        assertFalse(result.isAjaxResult);
    }

    @Test
    public void resolveQueueRequestByLocalConfigAjaxCallTest() throws Exception {
        // Arrange
        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.Headers.put("x-queueit-ajaxpageurl", "http%3A%2F%2Furl");

        connectorContextProviderMock.httpServletRequestMock = requestMock;
        KnownUserRequestWrapperMock knownUserRequestWrapperMock = new KnownUserRequestWrapperMock(requestMock);
        connectorContextProviderMock.httpRequest = new HttpRequest(knownUserRequestWrapperMock);

        QueueEventConfig eventConfig = new QueueEventConfig();
        eventConfig.setCookieDomain("cookieDomain");
        eventConfig.setLayoutName("layoutName");
        eventConfig.setCulture("culture");
        eventConfig.setEventId("eventId");
        eventConfig.setQueueDomain("queueDomain");
        eventConfig.setExtendCookieValidity(true);
        eventConfig.setCookieValidityMinute(10);
        eventConfig.setVersion(12);
        eventConfig.setActionName("queueAction");

        // Act
        RequestValidationResult result = KnownUser.resolveQueueRequestByLocalConfig("targetUrl", "queueitToken",
                eventConfig, "customerId", "secretKey", connectorContextProviderMock);

        // Assert
        assertEquals("http://url", userInQueueServiceMock.validateQueueRequestCalls.get(0).get(0));
        assertEquals("queueitToken", userInQueueServiceMock.validateQueueRequestCalls.get(0).get(1));
        assertEquals("cookieDomain:layoutName:culture:eventId:queueDomain:true:10:12:queueAction",
                userInQueueServiceMock.validateQueueRequestCalls.get(0).get(2));
        assertEquals("customerId", userInQueueServiceMock.validateQueueRequestCalls.get(0).get(3));
        assertEquals("secretKey", userInQueueServiceMock.validateQueueRequestCalls.get(0).get(4));
        assertTrue(result.isAjaxResult);
    }

    @Test
    public void resolveQueueRequestByLocalConfigDebugCookieLoggingTest() throws Exception {

        // Arrange
        QueueEventConfig queueConfig = new QueueEventConfig();
        queueConfig.setCookieDomain("cookieDomain");
        queueConfig.setLayoutName("layoutName");
        queueConfig.setCulture("culture");
        queueConfig.setEventId("eventId");
        queueConfig.setQueueDomain("queueDomain");
        queueConfig.setExtendCookieValidity(true);
        queueConfig.setCookieValidityMinute(10);
        queueConfig.setVersion(12);
        queueConfig.setActionName("queueAction");

        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.RequestURL = "http://test.tesdomain.com/test";
        requestMock.RemoteAddr = "80.35.35.34";
        requestMock.Headers.put("via", "1.1 example.com");
        requestMock.Headers.put("forwarded", "for=192.0.2.60;proto=http;by=203.0.113.43");
        requestMock.Headers.put("x-forwarded-for", "129.78.138.66, 129.78.64.103");
        requestMock.Headers.put("x-forwarded-host", "en.wikipedia.org:8080");
        requestMock.Headers.put("x-forwarded-proto", "https");

        HttpServletResponseMock responseMock = new HttpServletResponseMock();
        KnownUserRequestWrapperMock knownUserRequestWrapperMock = new KnownUserRequestWrapperMock(requestMock);
        connectorContextProviderMock.httpRequest = new HttpRequest(knownUserRequestWrapperMock);
        connectorContextProviderMock.httpResponse = new HttpResponse(responseMock);

        // Act
        String secretKey = "secretkey";
        Date date = new Date();
        date = addDays(date, 1);
        String queueittoken = UserInQueueServiceTest.QueueITTokenGenerator.generateToken(date, "eventId", false,
                null, secretKey, "debug", "");

        KnownUser.resolveQueueRequestByLocalConfig("targetUrl", queueittoken, queueConfig, "customerId", secretKey,
                connectorContextProviderMock);

        // Assert
        assertEquals(1, responseMock.addedCookies.size());
        assertEquals(KnownUser.QUEUEIT_DEBUG_KEY, responseMock.addedCookies.get(0).getName());
        String decodedCookieValue = URLDecoder.decode(responseMock.addedCookies.get(0).getValue(),
                "UTF-8");
        assertTrue(decodedCookieValue.contains("OriginalUrl=http://test.tesdomain.com/test?q=2#"));
        assertTrue(decodedCookieValue.contains("QueueConfig=EventId:eventId"));
        assertTrue(decodedCookieValue.contains("&Version:12"));
        assertTrue(decodedCookieValue.contains("&QueueDomain:queueDomain"));
        assertTrue(decodedCookieValue.contains("&CookieDomain:cookieDomain"));
        assertTrue(decodedCookieValue.contains("&ExtendCookieValidity:true"));
        assertTrue(decodedCookieValue.contains("&CookieValidityMinute:10"));
        assertTrue(decodedCookieValue.contains("&LayoutName:layoutName"));
        assertTrue(decodedCookieValue.contains("&Culture:culture"));
        assertTrue(decodedCookieValue.contains("QueueitToken=" + queueittoken));
        assertTrue(decodedCookieValue.contains("TargetUrl=targetUrl"));
        assertTrue(decodedCookieValue.contains("RequestIP=80.35.35.34"));
        assertTrue(decodedCookieValue.contains("RequestHttpHeader_Via=1.1 example.com"));
        assertTrue(
                decodedCookieValue.contains("RequestHttpHeader_Forwarded=for=192.0.2.60;proto=http;by=203.0.113.43"));
        assertTrue(decodedCookieValue.contains("RequestHttpHeader_XForwardedFor=129.78.138.66, 129.78.64.103"));
        assertTrue(decodedCookieValue.contains("RequestHttpHeader_XForwardedHost=en.wikipedia.org:8080"));
        assertTrue(decodedCookieValue.contains("RequestHttpHeader_XForwardedProto=https"));
        assertTrue(decodedCookieValue.contains("&ActionName:" + queueConfig.getActionName()));
    }

    @Test
    public void validateRequestByIntegrationConfigEmptyCurrentUrlTest() throws URISyntaxException {

        // Arrange
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.validateRequestByIntegrationConfig("", null, null, null, null, connectorContextProviderMock);
        } catch (Exception ex) {
            exceptionWasThrown = "currentUrlWithoutQueueITToken can not be null or empty.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(userInQueueServiceMock.validateQueueRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void validateRequestByIntegrationConfigEmptyIntegrationsConfigTest() throws URISyntaxException {

        // Arrange
        boolean exceptionWasThrown = false;

        // Act
        try {
            KnownUser.validateRequestByIntegrationConfig("currentUrl", "queueitToken", null, null,
                    null, connectorContextProviderMock);
        } catch (Exception ex) {
            exceptionWasThrown = "customerIntegrationInfo can not be null.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(userInQueueServiceMock.validateQueueRequestCalls.isEmpty());
        assertTrue(exceptionWasThrown);
    }

    @Test
    public void validateRequestByIntegrationConfigQueueActionTest() throws Exception {
        // Arrange
        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.Operator = "Contains";
        triggerPart1.ValueToCompare = "event1";
        triggerPart1.UrlPart = "PageUrl";
        triggerPart1.ValidatorType = "UrlValidator";
        triggerPart1.IsNegative = false;
        triggerPart1.IsIgnoreCase = true;

        TriggerPart triggerPart2 = new TriggerPart();
        triggerPart2.Operator = "Contains";
        triggerPart2.ValueToCompare = "googlebot";
        triggerPart2.ValidatorType = "UserAgentValidator";
        triggerPart2.IsNegative = false;
        triggerPart2.IsIgnoreCase = false;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[] { triggerPart1, triggerPart2 };

        IntegrationConfigModel config = new IntegrationConfigModel();
        config.Name = "event1action";
        config.EventId = "event1";
        config.CookieDomain = ".test.com";
        config.LayoutName = "Christmas Layout by Queue-it";
        config.Culture = "da-DK";
        config.ExtendCookieValidity = true;
        config.CookieValidityMinute = 20;
        config.Triggers = new TriggerModel[] { trigger };
        config.QueueDomain = "knownusertest.queue-it.net";
        config.RedirectLogic = "AllowTParameter";
        config.ForcedTargetUrl = "";
        config.ActionType = ActionType.QUEUE_ACTION;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[] { config };
        customerIntegration.Version = 3;

        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.UserAgent = "googlebot";
        requestMock.RequestURL = "http://test.com?event1=true";

        HttpServletResponseMock responseMock = new HttpServletResponseMock();
        KnownUserRequestWrapperMock knownUserRequestWrapperMock = new KnownUserRequestWrapperMock(requestMock);
        connectorContextProviderMock.httpRequest = new HttpRequest(knownUserRequestWrapperMock);
        connectorContextProviderMock.httpResponse = new HttpResponse(responseMock);

        // Act
        RequestValidationResult result = KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true",
                "queueitToken", customerIntegration, "customerId", "secretKey", connectorContextProviderMock);

        // Assert
        assertEquals(1, userInQueueServiceMock.validateQueueRequestCalls.size());
        assertEquals("http://test.com?event1=true", userInQueueServiceMock.validateQueueRequestCalls.get(0).get(0));
        assertEquals("queueitToken", userInQueueServiceMock.validateQueueRequestCalls.get(0).get(1));
        assertEquals(
                ".test.com:Christmas Layout by Queue-it:da-DK:event1:knownusertest.queue-it.net:true:20:3:event1action",
                userInQueueServiceMock.validateQueueRequestCalls.get(0).get(2));
        assertEquals("customerId", userInQueueServiceMock.validateQueueRequestCalls.get(0).get(3));
        assertEquals("secretKey", userInQueueServiceMock.validateQueueRequestCalls.get(0).get(4));
        assertFalse(result.isAjaxResult);
    }

    @Test
    public void validateRequestByIntegrationConfigQueueActionAjaxCallTest() throws Exception {

        // Arrange
        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.Operator = "Contains";
        triggerPart1.ValueToCompare = "event1";
        triggerPart1.UrlPart = "PageUrl";
        triggerPart1.ValidatorType = "UrlValidator";
        triggerPart1.IsNegative = false;
        triggerPart1.IsIgnoreCase = true;

        TriggerPart triggerPart2 = new TriggerPart();
        triggerPart2.Operator = "Contains";
        triggerPart2.ValueToCompare = "googlebot";
        triggerPart2.ValidatorType = "UserAgentValidator";
        triggerPart2.IsNegative = false;
        triggerPart2.IsIgnoreCase = false;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[] { triggerPart1, triggerPart2 };

        IntegrationConfigModel config = new IntegrationConfigModel();
        config.Name = "event1action";
        config.EventId = "event1";
        config.CookieDomain = ".test.com";
        config.LayoutName = "Christmas Layout by Queue-it";
        config.Culture = "da-DK";
        config.ExtendCookieValidity = true;
        config.CookieValidityMinute = 20;
        config.Triggers = new TriggerModel[] { trigger };
        config.QueueDomain = "knownusertest.queue-it.net";
        config.RedirectLogic = "AllowTParameter";
        config.ForcedTargetUrl = "";
        config.ActionType = ActionType.QUEUE_ACTION;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[] { config };
        customerIntegration.Version = 3;

        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.UserAgent = "googlebot";
        requestMock.Headers.put("x-queueit-ajaxpageurl", "http%3A%2F%2Furl");
        KnownUserRequestWrapperMock knownUserRequestWrapperMock = new KnownUserRequestWrapperMock(requestMock);
        connectorContextProviderMock.httpRequest = new HttpRequest(knownUserRequestWrapperMock);

        // Act
        RequestValidationResult result = KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true",
                "queueitToken", customerIntegration, "customerId", "secretKey", connectorContextProviderMock);

        // Assert
        assertEquals(1, userInQueueServiceMock.validateQueueRequestCalls.size());
        assertEquals("http://url", userInQueueServiceMock.validateQueueRequestCalls.get(0).get(0));
        assertEquals("queueitToken", userInQueueServiceMock.validateQueueRequestCalls.get(0).get(1));
        assertEquals(
                ".test.com:Christmas Layout by Queue-it:da-DK:event1:knownusertest.queue-it.net:true:20:3:event1action",
                userInQueueServiceMock.validateQueueRequestCalls.get(0).get(2));
        assertEquals("customerId", userInQueueServiceMock.validateQueueRequestCalls.get(0).get(3));
        assertEquals("secretKey", userInQueueServiceMock.validateQueueRequestCalls.get(0).get(4));
        assertTrue(result.isAjaxResult);
    }

    @Test
    public void validateRequestByIntegrationConfigDebugCookieLoggingTest() throws Exception {
        // Arrange
        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.Operator = "Contains";
        triggerPart1.ValueToCompare = "event1";
        triggerPart1.UrlPart = "PageUrl";
        triggerPart1.ValidatorType = "UrlValidator";
        triggerPart1.IsNegative = false;
        triggerPart1.IsIgnoreCase = true;

        TriggerPart triggerPart2 = new TriggerPart();
        triggerPart2.Operator = "Contains";
        triggerPart2.ValueToCompare = "googlebot";
        triggerPart2.ValidatorType = "UserAgentValidator";
        triggerPart2.IsNegative = false;
        triggerPart2.IsIgnoreCase = false;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[] { triggerPart1, triggerPart2 };

        IntegrationConfigModel config = new IntegrationConfigModel();
        config.Name = "event1action";
        config.EventId = "event1";
        config.CookieDomain = ".test.com";
        config.LayoutName = "Christmas Layout by Queue-it";
        config.Culture = "da-DK";
        config.ExtendCookieValidity = true;
        config.CookieValidityMinute = 20;
        config.Triggers = new TriggerModel[] { trigger };
        config.QueueDomain = "knownusertest.queue-it.net";
        config.RedirectLogic = "AllowTParameter";
        config.ForcedTargetUrl = "";
        config.ActionType = ActionType.QUEUE_ACTION;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[] { config };
        customerIntegration.Version = 3;

        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.UserAgent = "googlebot";
        requestMock.RequestURL = "http://test.tesdomain.com/test";

        HttpServletResponseMock responseMock = new HttpServletResponseMock();
        KnownUserRequestWrapperMock knownUserRequestWrapperMock = new KnownUserRequestWrapperMock(requestMock);
        connectorContextProviderMock.httpRequest = new HttpRequest(knownUserRequestWrapperMock);
        connectorContextProviderMock.httpResponse = new HttpResponse(responseMock);

        // Act
        String secretKey = "secretkey";
        Date date = new Date();
        date = addDays(date, 1);
        String queueittoken = UserInQueueServiceTest.QueueITTokenGenerator.generateToken(date, "eventId", true,
                20, secretKey, "debug", "");

        KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true",
                queueittoken, customerIntegration, "customerId", secretKey, connectorContextProviderMock);

        // Assert
        assertEquals(1, responseMock.addedCookies.size());
        assertEquals(KnownUser.QUEUEIT_DEBUG_KEY, responseMock.addedCookies.get(0).getName());
        String decodedCookieValue = URLDecoder.decode(responseMock.addedCookies.get(0).getValue(),
                "UTF-8");
        assertTrue(decodedCookieValue.contains("OriginalUrl=http://test.tesdomain.com/test?q=2#"));
        assertTrue(decodedCookieValue.contains("PureUrl=http://test.com?event1=true"));
        assertTrue(decodedCookieValue.contains("ConfigVersion=3"));
        assertTrue(decodedCookieValue.contains("QueueConfig=EventId:event1"));
        assertTrue(decodedCookieValue.contains("&Version:3"));
        assertTrue(decodedCookieValue.contains("&QueueDomain:knownusertest.queue-it.net"));
        assertTrue(decodedCookieValue.contains("&CookieDomain:.test.com"));
        assertTrue(decodedCookieValue.contains("&ExtendCookieValidity:true"));
        assertTrue(decodedCookieValue.contains("&CookieValidityMinute:20"));
        assertTrue(decodedCookieValue.contains("&LayoutName:Christmas Layout by Queue-it"));
        assertTrue(decodedCookieValue.contains("&Culture:da-DK"));
        assertTrue(decodedCookieValue.contains("QueueitToken=" + queueittoken));
        assertTrue(decodedCookieValue.contains("TargetUrl=http://test.com?event1=true"));
        assertTrue(decodedCookieValue.contains("MatchedConfig=event1action"));
        assertTrue(decodedCookieValue.contains("&ActionName:" + config.Name));
    }

    @Test
    public void validateRequestByIntegrationConfigNotMatchTest() throws Exception {
        // Arrange
        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[0];
        customerIntegration.Version = 3;

        // Act
        RequestValidationResult result = KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true",
                "queueitToken", customerIntegration, "customerId", "secretKey",
                connectorContextProviderMock);

        // Assert
        assertTrue(userInQueueServiceMock.validateQueueRequestCalls.isEmpty());
        assertFalse(result.doRedirect());
    }

    @Test
    public void validateRequestByIntegrationConfigNotMatchDebugCookieLoggingTest() throws Exception {
        // Arrange
        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[0];
        customerIntegration.Version = 3;

        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.RequestURL = "http://test.tesdomain.com";
        requestMock.RemoteAddr = "80.35.35.34";
        requestMock.Headers.put("via", "1.1 example.com");
        requestMock.Headers.put("forwarded", "for=192.0.2.60;proto=http;by=203.0.113.43");
        requestMock.Headers.put("x-forwarded-for", "129.78.138.66, 129.78.64.103");
        requestMock.Headers.put("x-forwarded-host", "en.wikipedia.org:8080");
        requestMock.Headers.put("x-forwarded-proto", "https");

        HttpServletResponseMock responseMock = new HttpServletResponseMock();
        KnownUserRequestWrapperMock knownUserRequestWrapperMock = new KnownUserRequestWrapperMock(requestMock);
        connectorContextProviderMock.httpRequest = new HttpRequest(knownUserRequestWrapperMock);
        connectorContextProviderMock.httpResponse = new HttpResponse(responseMock);

        // Act
        String secretKey = "secretkey";
        Date date = new Date();
        date = addDays(date, 1);
        String queueittoken = UserInQueueServiceTest.QueueITTokenGenerator.generateToken(date, "eventId", true,
                20, secretKey, "debug", "");

        KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true", queueittoken, customerIntegration,
                "customerId", secretKey, connectorContextProviderMock);

        // Assert
        assertEquals(1, responseMock.addedCookies.size());
        assertEquals(KnownUser.QUEUEIT_DEBUG_KEY, responseMock.addedCookies.get(0).getName());
        String decodedCookieValue = URLDecoder.decode(responseMock.addedCookies.get(0).getValue(),
                "UTF-8");
        assertTrue(decodedCookieValue.contains("OriginalUrl=http://test.tesdomain.com"));
        assertTrue(decodedCookieValue.contains("PureUrl=http://test.com?event1=true"));
        assertTrue(decodedCookieValue.contains("ConfigVersion=3"));
        assertTrue(decodedCookieValue.contains("QueueitToken=" + queueittoken));
        assertTrue(decodedCookieValue.contains("RequestIP=80.35.35.34"));
        assertTrue(decodedCookieValue.contains("RequestHttpHeader_Via=1.1 example.com"));
        assertTrue(
                decodedCookieValue.contains("RequestHttpHeader_Forwarded=for=192.0.2.60;proto=http;by=203.0.113.43"));
        assertTrue(decodedCookieValue.contains("RequestHttpHeader_XForwardedFor=129.78.138.66, 129.78.64.103"));
        assertTrue(decodedCookieValue.contains("RequestHttpHeader_XForwardedHost=en.wikipedia.org:8080"));
        assertTrue(decodedCookieValue.contains("RequestHttpHeader_XForwardedProto=https"));
    }

    @Test
    public void validateRequestByIntegrationConfigForcedTargeturlTest() throws Exception {
        // Arrange
        TriggerPart triggerPart = new TriggerPart();
        triggerPart.Operator = "Contains";
        triggerPart.ValueToCompare = "event1";
        triggerPart.UrlPart = "PageUrl";
        triggerPart.ValidatorType = "UrlValidator";
        triggerPart.IsNegative = false;
        triggerPart.IsIgnoreCase = true;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[] { triggerPart };

        IntegrationConfigModel config = new IntegrationConfigModel();
        config.Name = "event1action";
        config.EventId = "event1";
        config.CookieDomain = ".test.com";
        config.LayoutName = "Christmas Layout by Queue-it";
        config.Culture = "da-DK";
        config.ExtendCookieValidity = true;
        config.CookieValidityMinute = 20;
        config.Triggers = new TriggerModel[] { trigger };
        config.QueueDomain = "knownusertest.queue-it.net";
        config.RedirectLogic = "ForcedTargetUrl";
        config.ForcedTargetUrl = "http://forcedtargeturl.com";
        config.ActionType = ActionType.QUEUE_ACTION;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[] { config };
        customerIntegration.Version = 3;

        // Act
        KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true", "queueitToken", customerIntegration,
                "customerId", "secretKey", connectorContextProviderMock);

        // Assert
        assertEquals(1, userInQueueServiceMock.validateQueueRequestCalls.size());
        assertEquals("http://forcedtargeturl.com", userInQueueServiceMock.validateQueueRequestCalls.get(0).get(0));
    }

    @Test
    public void validateRequestByIntegrationConfigForecedTargeturlTest() throws Exception {
        // Arrange
        TriggerPart triggerPart = new TriggerPart();
        triggerPart.Operator = "Contains";
        triggerPart.ValueToCompare = "event1";
        triggerPart.UrlPart = "PageUrl";
        triggerPart.ValidatorType = "UrlValidator";
        triggerPart.IsNegative = false;
        triggerPart.IsIgnoreCase = true;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[] { triggerPart };

        IntegrationConfigModel config = new IntegrationConfigModel();
        config.Name = "event1action";
        config.EventId = "event1";
        config.CookieDomain = ".test.com";
        config.LayoutName = "Christmas Layout by Queue-it";
        config.Culture = "da-DK";
        config.ExtendCookieValidity = true;
        config.CookieValidityMinute = 20;
        config.Triggers = new TriggerModel[] { trigger };
        config.QueueDomain = "knownusertest.queue-it.net";
        config.RedirectLogic = "ForecedTargetUrl";
        config.ForcedTargetUrl = "http://forcedtargeturl.com";
        config.ActionType = ActionType.QUEUE_ACTION;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[] { config };
        customerIntegration.Version = 3;

        // Act
        KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true", "queueitToken", customerIntegration,
                "customerId", "secretKey", connectorContextProviderMock);

        // Assert
        assertEquals(1, userInQueueServiceMock.validateQueueRequestCalls.size());
        assertEquals("http://forcedtargeturl.com", userInQueueServiceMock.validateQueueRequestCalls.get(0).get(0));
    }

    @Test
    public void validateRequestByIntegrationConfigEventTargetUrl() throws Exception {
        // Arrange
        TriggerPart triggerPart = new TriggerPart();
        triggerPart.Operator = "Contains";
        triggerPart.ValueToCompare = "event1";
        triggerPart.UrlPart = "PageUrl";
        triggerPart.ValidatorType = "UrlValidator";
        triggerPart.IsNegative = false;
        triggerPart.IsIgnoreCase = true;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[] { triggerPart };

        IntegrationConfigModel config = new IntegrationConfigModel();
        config.Name = "event1action";
        config.EventId = "event1";
        config.CookieDomain = ".test.com";
        config.LayoutName = "Christmas Layout by Queue-it";
        config.Culture = "da-DK";
        config.ExtendCookieValidity = true;
        config.CookieValidityMinute = 20;
        config.Triggers = new TriggerModel[] { trigger };
        config.QueueDomain = "knownusertest.queue-it.net";
        config.RedirectLogic = "EventTargetUrl";
        config.ActionType = ActionType.QUEUE_ACTION;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[] { config };
        customerIntegration.Version = 3;

        // Act
        KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true", "queueitToken", customerIntegration,
                "customerId", "secretKey", connectorContextProviderMock);

        // Assert
        assertEquals(1, userInQueueServiceMock.validateQueueRequestCalls.size());
        assertEquals("", userInQueueServiceMock.validateQueueRequestCalls.get(0).get(0));
    }

    @Test
    public void validateRequestByIntegrationConfigIgnoreAction() throws Exception {
        // Arrange
        TriggerPart triggerPart = new TriggerPart();
        triggerPart.Operator = "Contains";
        triggerPart.ValueToCompare = "event1";
        triggerPart.UrlPart = "PageUrl";
        triggerPart.ValidatorType = "UrlValidator";
        triggerPart.IsNegative = false;
        triggerPart.IsIgnoreCase = true;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[] { triggerPart };

        IntegrationConfigModel config = new IntegrationConfigModel();
        config.Name = "event1action";
        config.EventId = "event1";
        config.CookieDomain = "cookiedomain";
        config.Triggers = new TriggerModel[] { trigger };
        config.QueueDomain = "queuedomain";
        config.ActionType = ActionType.IGNORE_ACTION;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[] { config };
        customerIntegration.Version = 3;

        // Act
        RequestValidationResult result = KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true",
                "queueitToken", customerIntegration, "customerId", "secretKey",
                connectorContextProviderMock);

        // Assert
        assertEquals(1, userInQueueServiceMock.getIgnoreActionResultCalls.size());
        assertFalse(result.isAjaxResult);
        assertSame(userInQueueServiceMock.getIgnoreActionResultCalls.get(0).get(0), config.Name);
    }

    @Test
    public void validateRequestByIntegrationConfigAjaxCallIgnoreAction() throws Exception {
        // Arrange
        TriggerPart triggerPart = new TriggerPart();
        triggerPart.Operator = "Contains";
        triggerPart.ValueToCompare = "event1";
        triggerPart.UrlPart = "PageUrl";
        triggerPart.ValidatorType = "UrlValidator";
        triggerPart.IsNegative = false;
        triggerPart.IsIgnoreCase = true;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[] { triggerPart };

        IntegrationConfigModel config = new IntegrationConfigModel();
        config.Name = "event1action";
        config.EventId = "event1";
        config.CookieDomain = "cookiedomain";
        config.Triggers = new TriggerModel[] { trigger };
        config.QueueDomain = "queuedomain";
        config.ActionType = ActionType.IGNORE_ACTION;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[] { config };
        customerIntegration.Version = 3;

        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.Headers.put("x-queueit-ajaxpageurl", "url");
        KnownUserRequestWrapperMock knownUserRequestWrapperMock = new KnownUserRequestWrapperMock(requestMock);
        connectorContextProviderMock.httpRequest = new HttpRequest(knownUserRequestWrapperMock);

        // Act
        RequestValidationResult result = KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true",
                "queueitToken", customerIntegration, "customerId", "secretKey", connectorContextProviderMock);

        // Assert
        assertEquals(1, userInQueueServiceMock.getIgnoreActionResultCalls.size());
        assertTrue(result.isAjaxResult);
        assertSame(userInQueueServiceMock.getIgnoreActionResultCalls.get(0).get(0), config.Name);
    }

    @Test
    public void validateRequestByIntegrationConfigCancelAction() throws Exception {
        // Arrange
        TriggerPart triggerPart = new TriggerPart();
        triggerPart.Operator = "Contains";
        triggerPart.ValueToCompare = "event1";
        triggerPart.UrlPart = "PageUrl";
        triggerPart.ValidatorType = "UrlValidator";
        triggerPart.IsNegative = false;
        triggerPart.IsIgnoreCase = true;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[] { triggerPart };

        IntegrationConfigModel config = new IntegrationConfigModel();
        config.Name = "event1action";
        config.EventId = "event1";
        config.CookieDomain = "cookiedomain";
        config.Triggers = new TriggerModel[] { trigger };
        config.QueueDomain = "queuedomain";
        config.ActionType = ActionType.CANCEL_ACTION;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[] { config };
        customerIntegration.Version = 3;

        // Act
        RequestValidationResult result = KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true",
                "queueitToken", customerIntegration, "customerId", "secretKey",
                connectorContextProviderMock);

        // Assert
        assertEquals("http://test.com?event1=true", userInQueueServiceMock.validateCancelRequestCalls.get(0).get(0));
        assertEquals("cookiedomain:event1:queuedomain:3:event1action",
                userInQueueServiceMock.validateCancelRequestCalls.get(0).get(1));
        assertEquals("customerId", userInQueueServiceMock.validateCancelRequestCalls.get(0).get(2));
        assertEquals("secretKey", userInQueueServiceMock.validateCancelRequestCalls.get(0).get(3));
        assertFalse(result.isAjaxResult);
    }

    @Test
    public void validateRequestByIntegrationConfigAjaxCallCancelAction() throws Exception {
        // Arrange
        TriggerPart triggerPart = new TriggerPart();
        triggerPart.Operator = "Contains";
        triggerPart.ValueToCompare = "event1";
        triggerPart.UrlPart = "PageUrl";
        triggerPart.ValidatorType = "UrlValidator";
        triggerPart.IsNegative = false;
        triggerPart.IsIgnoreCase = true;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[] { triggerPart };

        IntegrationConfigModel config = new IntegrationConfigModel();
        config.Name = "event1action";
        config.EventId = "event1";
        config.CookieDomain = "cookiedomain";
        config.Triggers = new TriggerModel[] { trigger };
        config.QueueDomain = "queuedomain";
        config.ActionType = ActionType.CANCEL_ACTION;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[] { config };
        customerIntegration.Version = 3;

        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.Headers.put("x-queueit-ajaxpageurl", "http%3A%2F%2Furl");
        KnownUserRequestWrapperMock knownUserRequestWrapperMock = new KnownUserRequestWrapperMock(requestMock);
        connectorContextProviderMock.httpRequest = new HttpRequest(knownUserRequestWrapperMock);

        // Act
        RequestValidationResult result = KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true",
                "queueitToken", customerIntegration, "customerId", "secretKey", connectorContextProviderMock);

        // Assert
        assertEquals("http://url", userInQueueServiceMock.validateCancelRequestCalls.get(0).get(0));
        assertEquals("cookiedomain:event1:queuedomain:3:event1action",
                userInQueueServiceMock.validateCancelRequestCalls.get(0).get(1));
        assertEquals("customerId", userInQueueServiceMock.validateCancelRequestCalls.get(0).get(2));
        assertEquals("secretKey", userInQueueServiceMock.validateCancelRequestCalls.get(0).get(3));
        assertTrue(result.isAjaxResult);
    }

    @Test
    public void validateRequestByIntegrationConfig_Debug() throws Exception {
        // Arrange
        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.Operator = "Contains";
        triggerPart1.ValueToCompare = "event1";
        triggerPart1.UrlPart = "PageUrl";
        triggerPart1.ValidatorType = "UrlValidator";
        triggerPart1.IsNegative = false;
        triggerPart1.IsIgnoreCase = true;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[] { triggerPart1 };

        IntegrationConfigModel config = new IntegrationConfigModel();
        config.Name = "event1action";
        config.EventId = "event1";
        config.CookieDomain = ".test.com";
        config.LayoutName = "Christmas Layout by Queue-it";
        config.Culture = "da-DK";
        config.ExtendCookieValidity = true;
        config.CookieValidityMinute = 20;
        config.Triggers = new TriggerModel[] { trigger };
        config.QueueDomain = "knownusertest.queue-it.net";
        config.RedirectLogic = "AllowTParameter";
        config.ForcedTargetUrl = "";

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[] { config };
        customerIntegration.Version = 3;

        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.UserAgent = "googlebot";
        requestMock.RequestURL = "http://test.tesdomain.com/test";
        requestMock.RemoteAddr = "80.35.35.34";
        requestMock.Headers.put("via", "1.1 example.com");
        requestMock.Headers.put("forwarded", "for=192.0.2.60;proto=http;by=203.0.113.43");
        requestMock.Headers.put("x-forwarded-for", "129.78.138.66, 129.78.64.103");
        requestMock.Headers.put("x-forwarded-host", "en.wikipedia.org:8080");
        requestMock.Headers.put("x-forwarded-proto", "https");

        HttpServletResponseMock responseMock = new HttpServletResponseMock();
        KnownUserRequestWrapperMock knownUserRequestWrapperMock = new KnownUserRequestWrapperMock(requestMock);
        connectorContextProviderMock.httpRequest = new HttpRequest(knownUserRequestWrapperMock);
        connectorContextProviderMock.httpResponse = new HttpResponse(responseMock);

        // Act
        String secretKey = "secretkey";
        Date date = new Date();
        date = addDays(date, 1);
        String queueittoken = UserInQueueServiceTest.QueueITTokenGenerator.generateToken(date, "eventId", true,
                20, secretKey, "debug", "");

        KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true",
                queueittoken, customerIntegration, "customerId", secretKey, connectorContextProviderMock);

        // Assert
        assertEquals(1, responseMock.addedCookies.size());
        assertEquals(KnownUser.QUEUEIT_DEBUG_KEY, responseMock.addedCookies.get(0).getName());
        String decodedCookieValue = URLDecoder.decode(responseMock.addedCookies.get(0).getValue(),
                "UTF-8");
        assertTrue(decodedCookieValue.contains("PureUrl=http://test.com?event1=true"));
        assertTrue(decodedCookieValue.contains("ConfigVersion=3"));
        assertTrue(decodedCookieValue.contains("MatchedConfig=event1action"));
        assertTrue(decodedCookieValue.contains("QueueitToken=" + queueittoken));
        assertTrue(decodedCookieValue.contains("OriginalUrl=http://test.tesdomain.com/test?q=2#"));
        assertTrue(decodedCookieValue.contains("TargetUrl=http://test.com?event1=true"));
        assertTrue(decodedCookieValue.contains("QueueConfig=EventId:event1"));
        assertTrue(decodedCookieValue.contains("&Version:3"));
        assertTrue(decodedCookieValue.contains("&QueueDomain:knownusertest.queue-it.net"));
        assertTrue(decodedCookieValue.contains("&CookieDomain:.test.com"));
        assertTrue(decodedCookieValue.contains("&ExtendCookieValidity:true"));
        assertTrue(decodedCookieValue.contains("&CookieValidityMinute:20"));
        assertTrue(decodedCookieValue.contains("&LayoutName:Christmas Layout by Queue-it"));
        assertTrue(decodedCookieValue.contains("&Culture:da-DK"));
        assertTrue(decodedCookieValue.contains("&ActionName:" + config.Name));
        assertTrue(decodedCookieValue.contains("SdkVersion=" + UserInQueueService.SDK_VERSION));
        assertTrue(decodedCookieValue.contains("Runtime=" + GetRuntimeVersion()));
    }

    @Test
    public void ValidateRequestByIntegrationConfig_Debug_WithoutMatch() throws Exception {

        // Arrange
        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[] {};
        customerIntegration.Version = 10;

        HttpServletRequestWithPathInfoMock requestMock = new HttpServletRequestWithPathInfoMock();
        requestMock.RequestURL = "http://test.com/?event1=true&queueittoken=queueittokenvalue";
        requestMock.RemoteAddr = "80.35.35.34";
        requestMock.Headers.put("via", "1.1 example.com");
        requestMock.Headers.put("forwarded", "for=192.0.2.60;proto=http;by=203.0.113.43");
        requestMock.Headers.put("x-forwarded-for", "129.78.138.66, 129.78.64.103");
        requestMock.Headers.put("x-forwarded-host", "en.wikipedia.org:8080");
        requestMock.Headers.put("x-forwarded-proto", "https");

        HttpServletResponseMock responseMock = new HttpServletResponseMock();
        KnownUserRequestWrapperMock knownUserRequestWrapperMock = new KnownUserRequestWrapperMock(requestMock);
        connectorContextProviderMock.httpRequest = new HttpRequest(knownUserRequestWrapperMock);
        connectorContextProviderMock.httpResponse = new HttpResponse(responseMock);

        // Act
        String secretKey = "secretkey";
        Date date = new Date();
        date = addDays(date, 1);
        String queueittoken = UserInQueueServiceTest.QueueITTokenGenerator.generateToken(date, "event1", true,
                null, secretKey, "debug", "");

        KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true",
                queueittoken, customerIntegration, "customerId", secretKey, connectorContextProviderMock);

        // Assert
        assertEquals(1, responseMock.addedCookies.size());
        String decodedCookieValue = URLDecoder.decode(responseMock.addedCookies.get(0).getValue(),
                "UTF-8");
        assertTrue(decodedCookieValue.contains("PureUrl=http://test.com?event1=true"));
        assertTrue(decodedCookieValue.contains("QueueitToken=" + queueittoken));
        assertTrue(decodedCookieValue.contains("ConfigVersion=10"));
        assertTrue(
                decodedCookieValue.contains("OriginalUrl=http://test.com/?event1=true&queueittoken=queueittokenvalue"));
        assertTrue(decodedCookieValue.contains("MatchedConfig=NULL"));
        assertTrue(decodedCookieValue.contains("SdkVersion=" + UserInQueueService.SDK_VERSION));
        assertTrue(decodedCookieValue.contains("Runtime=" + GetRuntimeVersion()));
    }

    @Test
    public void ValidateRequestByIntegrationConfig_Exception_NoDebugToken_NoDebugCookie() throws URISyntaxException {

        // Arrange
        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.Operator = "Contains";
        triggerPart1.ValueToCompare = "event1";
        triggerPart1.UrlPart = "PageUrl";
        triggerPart1.ValidatorType = "UrlValidator";
        triggerPart1.IsNegative = false;
        triggerPart1.IsIgnoreCase = true;

        TriggerPart triggerPart2 = new TriggerPart();
        triggerPart2.Operator = "Contains";
        triggerPart2.ValueToCompare = "googlebot";
        triggerPart2.ValidatorType = "UserAgentValidator";
        triggerPart2.IsNegative = false;
        triggerPart2.IsIgnoreCase = false;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[] { triggerPart1, triggerPart2 };

        IntegrationConfigModel config = new IntegrationConfigModel();
        config.Name = "event1action";
        config.EventId = "event1";
        config.CookieDomain = ".test.com";
        config.LayoutName = "Christmas Layout by Queue-it";
        config.Culture = "da-DK";
        config.ExtendCookieValidity = true;
        config.CookieValidityMinute = 20;
        config.Triggers = new TriggerModel[] { trigger };
        config.QueueDomain = "knownusertest.queue-it.net";
        config.RedirectLogic = "AllowTParameter";
        config.ForcedTargetUrl = "";
        config.ActionType = ActionType.QUEUE_ACTION;

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[] { config };
        customerIntegration.Version = 3;

        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.UserAgent = "googlebot";

        HttpServletResponseMock responseMock = new HttpServletResponseMock();
        userInQueueServiceMock.validateQueueRequestRaiseException = true;
        KnownUserRequestWrapperMock knownUserRequestWrapperMock = new KnownUserRequestWrapperMock(requestMock);
        connectorContextProviderMock.httpRequest = new HttpRequest(knownUserRequestWrapperMock);
        connectorContextProviderMock.httpResponse = new HttpResponse(responseMock);

        // Act
        try {
            KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true",
                    "queueitToken", customerIntegration, "customerId", "secretKey", connectorContextProviderMock);
        } catch (Exception ex) {
            assertEquals("exception", ex.getMessage());
        }

        // Assert
        assertEquals(0, responseMock.addedCookies.size());
        assertTrue(userInQueueServiceMock.validateQueueRequestCalls.size() > 0);
    }

    @Test
    public void ResolveQueueRequestByLocalConfigTest_Exception_NoDebugToken_NoDebugCookie() throws URISyntaxException {

        // Arrange
        HttpServletRequestMock httpContextMock = new HttpServletRequestMock();
        httpContextMock.UserAgent = "googlebot";

        HttpServletResponseMock responseMock = new HttpServletResponseMock();

        QueueEventConfig eventConfig = new QueueEventConfig();
        eventConfig.setCookieDomain("cookieDomain");
        eventConfig.setLayoutName("layoutName");
        eventConfig.setCulture("culture");
        eventConfig.setEventId("eventId");
        eventConfig.setQueueDomain("queueDomain");
        eventConfig.setExtendCookieValidity(true);
        eventConfig.setCookieValidityMinute(10);
        eventConfig.setVersion(12);
        eventConfig.setActionName("queueAction");

        userInQueueServiceMock.validateQueueRequestRaiseException = true;

        // Act
        try {
            KnownUser.resolveQueueRequestByLocalConfig("targetUrl", "queueitToken", eventConfig, "customerId",
                    "secretKey", connectorContextProviderMock);
        } catch (Exception ex) {
            assertEquals("exception", ex.getMessage());
        }

        // Assert
        assertTrue(userInQueueServiceMock.validateQueueRequestCalls.size() > 0);
        assertEquals(0, responseMock.addedCookies.size());
    }

    @Test
    public void CancelRequestByLocalConfig_Exception_NoDebugToken_NoDebugCookie() throws URISyntaxException {
        // Arrange
        HttpServletRequestMock httpContextMock = new HttpServletRequestMock();
        httpContextMock.UserAgent = "googlebot";

        HttpServletResponseMock responseMock = new HttpServletResponseMock();

        CancelEventConfig cancelEventConfig = new CancelEventConfig();
        cancelEventConfig.setCookieDomain("cookiedomain");
        cancelEventConfig.setEventId("eventid");
        cancelEventConfig.setQueueDomain("queuedomain");
        cancelEventConfig.setVersion(1);
        cancelEventConfig.setActionName("cancelAction");

        userInQueueServiceMock.validateQueueRequestRaiseException = true;

        // Act
        try {
            KnownUser.cancelRequestByLocalConfig("targetUrl", "queueitToken", cancelEventConfig, "customerid",
                    "secretkey", connectorContextProviderMock);
        } catch (Exception ex) {
            assertEquals("exception", ex.getMessage());
        }

        // Assert
        assertTrue(userInQueueServiceMock.validateCancelRequestCalls.size() > 0);
        assertEquals(0, responseMock.addedCookies.size());
    }

    @Test
    public void validateRequestByIntegrationConfig_Debug_NullConfig() throws Exception {
        // Arrange
        boolean exceptionWasThrown = false;

        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.Operator = "Contains";
        triggerPart1.ValueToCompare = "event1";
        triggerPart1.UrlPart = "PageUrl";
        triggerPart1.ValidatorType = "UrlValidator";
        triggerPart1.IsNegative = false;
        triggerPart1.IsIgnoreCase = true;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[] { triggerPart1 };

        HttpServletRequestWithPathInfoMock requestMock = new HttpServletRequestWithPathInfoMock();
        requestMock.RequestURL = "http://test.com";
        requestMock.RemoteAddr = "80.35.35.34";
        requestMock.Headers.put("via", "1.1 example.com");
        requestMock.Headers.put("forwarded", "for=192.0.2.60;proto=http;by=203.0.113.43");
        requestMock.Headers.put("x-forwarded-for", "129.78.138.66, 129.78.64.103");
        requestMock.Headers.put("x-forwarded-host", "en.wikipedia.org:8080");
        requestMock.Headers.put("x-forwarded-proto", "https");

        HttpServletResponseMock responseMock = new HttpServletResponseMock();
        KnownUserRequestWrapperMock knownUserRequestWrapperMock = new KnownUserRequestWrapperMock(requestMock);
        connectorContextProviderMock.httpRequest = new HttpRequest(knownUserRequestWrapperMock);
        connectorContextProviderMock.httpResponse = new HttpResponse(responseMock);

        // Act
        String secretKey = "secretkey";
        Date date = new Date();
        date = addDays(date, 1);
        String queueittoken = UserInQueueServiceTest.QueueITTokenGenerator.generateToken(date, "eventId", true,
                20, secretKey, "debug", "");

        try {
            KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true", queueittoken, null,
                    "customerId", secretKey, connectorContextProviderMock);
        } catch (Exception ex) {
            exceptionWasThrown = "customerIntegrationInfo can not be null.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(exceptionWasThrown);
        assertEquals(1, responseMock.addedCookies.size());
        assertEquals(KnownUser.QUEUEIT_DEBUG_KEY, responseMock.addedCookies.get(0).getName());
        String decodedCookieValue = URLDecoder.decode(responseMock.addedCookies.get(0).getValue(),
                "UTF-8");
        assertTrue(decodedCookieValue.contains("SdkVersion=" + UserInQueueService.SDK_VERSION));
        assertTrue(decodedCookieValue.contains("PureUrl=http://test.com?event1=true"));
        assertTrue(decodedCookieValue.contains("ConfigVersion=NULL"));
        assertTrue(decodedCookieValue.contains("QueueitToken=" + queueittoken));
        assertTrue(decodedCookieValue.contains("OriginalUrl=http://test.com"));
        assertTrue(decodedCookieValue.contains("SdkVersion=" + UserInQueueService.SDK_VERSION));
        assertTrue(decodedCookieValue.contains("Runtime=" + GetRuntimeVersion()));
        assertTrue(decodedCookieValue.contains("Exception=customerIntegrationInfo can not be null."));
    }

    @Test
    public void ValidateRequestByIntegrationConfig_Debug_Missing_CustomerId() throws Exception {

        // Arrange
        HttpServletResponseMock responseMock = new HttpServletResponseMock();
        CustomerIntegration customerIntegration = new CustomerIntegration();

        // Act
        String secretKey = "secretKey";
        Date date = new Date();
        date = addDays(date, 1);

        String queueittoken = UserInQueueServiceTest.QueueITTokenGenerator.generateToken(date, "event1", true,
                null, secretKey, "debug", "");

        RequestValidationResult result = KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true",
                queueittoken, customerIntegration, null, secretKey, connectorContextProviderMock);

        // Assert
        assertEquals("https://api2.queue-it.net/diagnostics/connector/error/?code=setup", result.getRedirectUrl());
        assertTrue(responseMock.addedCookies.isEmpty());
    }

    @Test
    public void ValidateRequestByIntegrationConfig_Debug_Missing_Secretkey() throws Exception {

        // Arrange
        HttpServletResponseMock responseMock = new HttpServletResponseMock();
        CustomerIntegration customerIntegration = new CustomerIntegration();

        // Act
        String secretKey = "secretKey";
        Date date = new Date();
        date = addDays(date, 1);

        String queueittoken = UserInQueueServiceTest.QueueITTokenGenerator.generateToken(date, "event1", true,
                null, secretKey, "debug", "");

        RequestValidationResult result = KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true",
                queueittoken, customerIntegration, "customerId", null, connectorContextProviderMock);

        // Assert
        assertEquals("https://api2.queue-it.net/diagnostics/connector/error/?code=setup", result.getRedirectUrl());
        assertTrue(responseMock.addedCookies.isEmpty());

    }

    @Test
    public void ValidateRequestByIntegrationConfig_Debug_ExpiredToken() throws Exception {

        // Arrange
        HttpServletResponseMock responseMock = new HttpServletResponseMock();
        CustomerIntegration customerIntegration = new CustomerIntegration();

        // Act
        String secretKey = "secretKey";
        Date date = new Date();
        date = addDays(date, -1);

        String queueittoken = UserInQueueServiceTest.QueueITTokenGenerator.generateToken(date, "event1", true,
                null, secretKey, "debug", "");

        RequestValidationResult result = KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true",
                queueittoken, customerIntegration, "customerId", secretKey, connectorContextProviderMock);

        // Assert
        assertEquals("https://customerId.api2.queue-it.net/customerId/diagnostics/connector/error/?code=timestamp",
                result.getRedirectUrl());
        assertTrue(responseMock.addedCookies.isEmpty());
    }

    @Test
    public void ValidateRequestByIntegrationConfig_Debug_ModifiedToken() throws Exception {

        // Arrange
        HttpServletResponseMock responseMock = new HttpServletResponseMock();
        CustomerIntegration customerIntegration = new CustomerIntegration();

        // Act
        String secretKey = "secretKey";
        Date date = new Date();
        date = addDays(date, 1);

        String queueittoken = UserInQueueServiceTest.QueueITTokenGenerator.generateToken(date, "event1", true,
                null, secretKey, "debug", "") + "invalid-hash";

        RequestValidationResult result = KnownUser.validateRequestByIntegrationConfig("http://test.com?event1=true",
                queueittoken, customerIntegration, "customerId", secretKey, connectorContextProviderMock);

        // Assert
        assertEquals("https://customerId.api2.queue-it.net/customerId/diagnostics/connector/error/?code=hash",
                result.getRedirectUrl());
        assertTrue(responseMock.addedCookies.isEmpty());
    }

    @Test
    public void ResolveQueueRequestByLocalConfig_Debug() throws Exception {

        // Arrange
        HttpServletResponseMock responseMock = new HttpServletResponseMock();
        HttpServletRequestWithPathInfoMock requestMock = new HttpServletRequestWithPathInfoMock();

        requestMock.RequestURL = "http://test.com/?event1=true&queueittoken=queueittokenvalue";
        requestMock.RemoteAddr = "80.35.35.34";
        requestMock.Headers.put("via", "1.1 example.com");
        requestMock.Headers.put("forwarded", "for=192.0.2.60;proto=http;by=203.0.113.43");
        requestMock.Headers.put("x-forwarded-for", "129.78.138.66, 129.78.64.103");
        requestMock.Headers.put("x-forwarded-host", "en.wikipedia.org:8080");
        requestMock.Headers.put("x-forwarded-proto", "https");

        KnownUserRequestWrapperMock knownUserRequestWrapperMock = new KnownUserRequestWrapperMock(requestMock);
        connectorContextProviderMock.httpRequest = new HttpRequest(knownUserRequestWrapperMock);
        connectorContextProviderMock.httpResponse = new HttpResponse(responseMock);

        QueueEventConfig eventConfig = new QueueEventConfig();
        eventConfig.setCookieDomain("cookieDomain");
        eventConfig.setLayoutName("layoutName");
        eventConfig.setCulture("culture");
        eventConfig.setEventId("eventId");
        eventConfig.setQueueDomain("queueDomain");
        eventConfig.setIsCookieHttpOnly(true);
        eventConfig.setIsCookieSecure(false);
        eventConfig.setExtendCookieValidity(true);
        eventConfig.setCookieValidityMinute(10);
        eventConfig.setVersion(12);
        eventConfig.setActionName("QueueAction");

        // Act
        String secretKey = "secretKey";
        Date date = new Date();
        date = addDays(date, 1);

        String queueittoken = UserInQueueServiceTest.QueueITTokenGenerator.generateToken(date, "event1", true,
                null, secretKey, "debug", "");

        KnownUser.resolveQueueRequestByLocalConfig("http://test.com?event1=true",
                queueittoken, eventConfig, "customerId", secretKey, connectorContextProviderMock);

        // Assert
        String decodedCookieValue = URLDecoder.decode(responseMock.addedCookies.get(0).getValue(),
                "UTF-8");
        assertTrue(decodedCookieValue.contains("QueueitToken=" + queueittoken));
        assertTrue(
                decodedCookieValue.contains("OriginalUrl=http://test.com/?event1=true&queueittoken=queueittokenvalue"));
        assertTrue(decodedCookieValue.contains("TargetUrl=http://test.com?event1=true"));
        assertTrue(decodedCookieValue.contains(
                "QueueConfig=EventId:eventId&Version:12&QueueDomain:queueDomain&CookieDomain:cookieDomain&IsCookieHttpOnly:true&IsCookieSecure:false&ExtendCookieValidity:true&CookieValidityMinute:10&LayoutName:layoutName&Culture:culture&ActionName:"
                        + eventConfig.getActionName()));
        assertTrue(decodedCookieValue.contains("SdkVersion=" + UserInQueueService.SDK_VERSION));
        assertTrue(decodedCookieValue.contains("Runtime=" + GetRuntimeVersion()));
    }

    @Test
    public void ResolveQueueRequestByLocalConfig_Debug_NullConfig() throws Exception {

        // Arrange
        boolean exceptionWasThrown = false;

        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.Operator = "Contains";
        triggerPart1.ValueToCompare = "event1";
        triggerPart1.UrlPart = "PageUrl";
        triggerPart1.ValidatorType = "UrlValidator";
        triggerPart1.IsNegative = false;
        triggerPart1.IsIgnoreCase = true;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[] { triggerPart1 };

        HttpServletRequestWithPathInfoMock requestMock = new HttpServletRequestWithPathInfoMock();
        requestMock.RequestURL = "http://test.com/?event1=true&queueittoken=queueittokenvalue";
        requestMock.RemoteAddr = "80.35.35.34";
        requestMock.Headers.put("via", "1.1 example.com");
        requestMock.Headers.put("forwarded", "for=192.0.2.60;proto=http;by=203.0.113.43");
        requestMock.Headers.put("x-forwarded-for", "129.78.138.66, 129.78.64.103");
        requestMock.Headers.put("x-forwarded-host", "en.wikipedia.org:8080");
        requestMock.Headers.put("x-forwarded-proto", "https");

        HttpServletResponseMock responseMock = new HttpServletResponseMock();

        KnownUserRequestWrapperMock knownUserRequestWrapperMock = new KnownUserRequestWrapperMock(requestMock);
        connectorContextProviderMock.httpRequest = new HttpRequest(knownUserRequestWrapperMock);
        connectorContextProviderMock.httpResponse = new HttpResponse(responseMock);

        // Act
        String secretKey = "secretkey";
        Date date = new Date();
        date = addDays(date, 1);

        String queueittoken = UserInQueueServiceTest.QueueITTokenGenerator.generateToken(date, "event1", true,
                null, secretKey, "debug", "");

        try {
            KnownUser.resolveQueueRequestByLocalConfig("http://test.com?event1=true", queueittoken, null, "customerId",
                    secretKey, connectorContextProviderMock);
        } catch (Exception ex) {
            exceptionWasThrown = "eventConfig can not be null.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(exceptionWasThrown);
        String decodedCookieValue = URLDecoder.decode(responseMock.addedCookies.get(0).getValue(),
                "UTF-8");
        assertTrue(decodedCookieValue.contains("QueueitToken=" + queueittoken));
        assertTrue(
                decodedCookieValue.contains("OriginalUrl=http://test.com/?event1=true&queueittoken=queueittokenvalue"));
        assertTrue(decodedCookieValue.contains("QueueConfig=NULL"));
        assertTrue(decodedCookieValue.contains("Exception=eventConfig can not be null."));
    }

    @Test
    public void ResolveQueueRequestByLocalConfig_Debug_Missing_CustomerId() throws Exception {

        // Arrange
        HttpServletResponseMock responseMock = new HttpServletResponseMock();
        QueueEventConfig eventConfig = new QueueEventConfig();

        eventConfig.setEventId("event1");

        // Act
        String secretKey = "secretkey";
        Date date = new Date();
        date = addDays(date, -1);

        String queueittoken = UserInQueueServiceTest.QueueITTokenGenerator.generateToken(date, "event1", true,
                null, secretKey, "debug", "");

        RequestValidationResult result = KnownUser.resolveQueueRequestByLocalConfig("http://test.com?event1=true",
                queueittoken, eventConfig, null, secretKey, connectorContextProviderMock);

        // Assert
        assertEquals("https://api2.queue-it.net/diagnostics/connector/error/?code=setup", result.getRedirectUrl());
        assertTrue(responseMock.addedCookies.isEmpty());
    }

    @Test
    public void ResolveQueueRequestByLocalConfig_Debug_Missing_SecretKey() throws Exception {

        // Arrange
        HttpServletResponseMock responseMock = new HttpServletResponseMock();
        QueueEventConfig eventConfig = new QueueEventConfig();

        eventConfig.setEventId("event1");

        // Act
        String secretKey = "secretkey";
        Date date = new Date();
        date = addDays(date, -1);
        String queueittoken = UserInQueueServiceTest.QueueITTokenGenerator.generateToken(date, "event1", true,
                null, secretKey, "debug", "");

        RequestValidationResult result = KnownUser.resolveQueueRequestByLocalConfig("http://test.com?event1=true",
                queueittoken, eventConfig, "customerId", null, connectorContextProviderMock);

        // Assert
        assertEquals("https://api2.queue-it.net/diagnostics/connector/error/?code=setup", result.getRedirectUrl());
        assertTrue(responseMock.addedCookies.isEmpty());
    }

    @Test
    public void ResolveQueueRequestByLocalConfig_Debug_ExpiredToken() throws Exception {

        // Arrange
        HttpServletResponseMock responseMock = new HttpServletResponseMock();
        QueueEventConfig eventConfig = new QueueEventConfig();

        eventConfig.setEventId("event1");

        // Act
        String secretKey = "secretkey";
        Date date = new Date();
        date = addDays(date, -1);

        String queueittoken = UserInQueueServiceTest.QueueITTokenGenerator.generateToken(date, "event1", true,
                null, secretKey, "debug", "");

        RequestValidationResult result = KnownUser.resolveQueueRequestByLocalConfig("http://test.com?event1=true",
                queueittoken, eventConfig, "customerId", secretKey, connectorContextProviderMock);

        // Assert
        assertEquals("https://customerId.api2.queue-it.net/customerId/diagnostics/connector/error/?code=timestamp",
                result.getRedirectUrl());
        assertTrue(responseMock.addedCookies.isEmpty());
    }

    @Test
    public void ResolveQueueRequestByLocalConfig_Debug_ModifiedToken() throws Exception {

        // Arrange
        HttpServletResponseMock responseMock = new HttpServletResponseMock();

        QueueEventConfig eventConfig = new QueueEventConfig();
        eventConfig.setCookieDomain("cookieDomain");
        eventConfig.setLayoutName("layoutName");
        eventConfig.setCulture("culture");
        eventConfig.setQueueDomain("queueDomain");
        eventConfig.setExtendCookieValidity(true);
        eventConfig.setCookieValidityMinute(10);
        eventConfig.setVersion(12);

        // Act
        String secretKey = "secretkey";
        Date date = new Date();
        date = addDays(date, 1);

        String queueittoken = UserInQueueServiceTest.QueueITTokenGenerator.generateToken(date, "event1", true,
                null, secretKey, "debug", "") + "invalid-hash";

        RequestValidationResult result = KnownUser.resolveQueueRequestByLocalConfig("http://test.com?event1=true",
                queueittoken, eventConfig, "customerId", secretKey, connectorContextProviderMock);

        // Assert
        assertEquals("https://customerId.api2.queue-it.net/customerId/diagnostics/connector/error/?code=hash",
                result.getRedirectUrl());
        assertTrue(responseMock.addedCookies.isEmpty());
    }

    @Test
    public void CancelRequestByLocalConfig_Debug() throws Exception {

        // Arrange
        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.Operator = "Contains";
        triggerPart1.ValueToCompare = "event1";
        triggerPart1.UrlPart = "PageUrl";
        triggerPart1.ValidatorType = "UrlValidator";
        triggerPart1.IsNegative = false;
        triggerPart1.IsIgnoreCase = true;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[] { triggerPart1 };

        IntegrationConfigModel config = new IntegrationConfigModel();
        config.Name = "event1action";
        config.EventId = "event1";
        config.CookieDomain = ".test.com";
        config.LayoutName = "Christmas Layout by Queue-it";
        config.Culture = "da-DK";
        config.ExtendCookieValidity = true;
        config.CookieValidityMinute = 20;
        config.Triggers = new TriggerModel[] { trigger };
        config.QueueDomain = "knownusertest.queue-it.net";
        config.RedirectLogic = "AllowTParameter";
        config.ForcedTargetUrl = "";

        CustomerIntegration customerIntegration = new CustomerIntegration();
        customerIntegration.Integrations = new IntegrationConfigModel[] { config };
        customerIntegration.Version = 3;

        HttpServletRequestWithPathInfoMock requestMock = new HttpServletRequestWithPathInfoMock();
        requestMock.RequestURL = "http://test.com/?event1=true&queueittoken=queueittokenvalue";
        requestMock.RemoteAddr = "80.35.35.34";
        requestMock.Headers.put("via", "1.1 example.com");
        requestMock.Headers.put("forwarded", "for=192.0.2.60;proto=http;by=203.0.113.43");
        requestMock.Headers.put("x-forwarded-for", "129.78.138.66, 129.78.64.103");
        requestMock.Headers.put("x-forwarded-host", "en.wikipedia.org:8080");
        requestMock.Headers.put("x-forwarded-proto", "https");

        HttpServletResponseMock responseMock = new HttpServletResponseMock();

        KnownUserRequestWrapperMock knownUserRequestWrapperMock = new KnownUserRequestWrapperMock(requestMock);
        connectorContextProviderMock.httpRequest = new HttpRequest(knownUserRequestWrapperMock);
        connectorContextProviderMock.httpResponse = new HttpResponse(responseMock);

        CancelEventConfig cancelEventConfig = new CancelEventConfig();
        cancelEventConfig.setCookieDomain("cookiedomain");
        cancelEventConfig.setEventId("eventId");
        cancelEventConfig.setQueueDomain("queuedomain");
        cancelEventConfig.setVersion(12);
        cancelEventConfig.setActionName("cancelAction");
        cancelEventConfig.setIsCookieHttpOnly(true);
        cancelEventConfig.setIsCookieSecure(false);

        // Act
        String secretKey = "secretkey";
        Date date = new Date();
        date = addDays(date, 1);
        String queueittoken = UserInQueueServiceTest.QueueITTokenGenerator.generateToken(date, "eventId", true,
                null, secretKey, "debug", "");

        KnownUser.cancelRequestByLocalConfig("http://test.com?event1=true", queueittoken, cancelEventConfig,
                "customerId", secretKey, connectorContextProviderMock);

        // Assert
        assertEquals(1, responseMock.addedCookies.size());
        String decodedCookieValue = URLDecoder.decode(responseMock.addedCookies.get(0).getValue(),
                "UTF-8");
        assertTrue(decodedCookieValue.contains("QueueitToken=" + queueittoken));
        assertTrue(
                decodedCookieValue.contains("OriginalUrl=http://test.com/?event1=true&queueittoken=queueittokenvalue"));
        assertTrue(decodedCookieValue.contains("TargetUrl=http://test.com?event1=true"));

        String configvalues = "CancelConfig=EventId:eventId&Version:12&QueueDomain:queuedomain&CookieDomain:cookiedomain&IsCookieHttpOnly:true&IsCookieSecure:false&ActionName:"
                + cancelEventConfig.getActionName();
        assertTrue(decodedCookieValue.contains(configvalues));
    }

    @Test
    public void CancelRequestByLocalConfig_Debug_NullConfig() throws Exception {
        // Arrange
        boolean exceptionWasThrown = false;

        TriggerPart triggerPart1 = new TriggerPart();
        triggerPart1.Operator = "Contains";
        triggerPart1.ValueToCompare = "event1";
        triggerPart1.UrlPart = "PageUrl";
        triggerPart1.ValidatorType = "UrlValidator";
        triggerPart1.IsNegative = false;
        triggerPart1.IsIgnoreCase = true;

        TriggerModel trigger = new TriggerModel();
        trigger.LogicalOperator = "And";
        trigger.TriggerParts = new TriggerPart[] { triggerPart1 };

        HttpServletRequestWithPathInfoMock requestMock = new HttpServletRequestWithPathInfoMock();
        requestMock.RequestURL = "http://test.com/?event1=true&queueittoken=queueittokenvalue";
        requestMock.RemoteAddr = "80.35.35.34";
        requestMock.Headers.put("via", "1.1 example.com");
        requestMock.Headers.put("forwarded", "for=192.0.2.60;proto=http;by=203.0.113.43");
        requestMock.Headers.put("x-forwarded-for", "129.78.138.66, 129.78.64.103");
        requestMock.Headers.put("x-forwarded-host", "en.wikipedia.org:8080");
        requestMock.Headers.put("x-forwarded-proto", "https");

        HttpServletResponseMock responseMock = new HttpServletResponseMock();

        KnownUserRequestWrapperMock knownUserRequestWrapperMock = new KnownUserRequestWrapperMock(requestMock);
        connectorContextProviderMock.httpRequest = new HttpRequest(knownUserRequestWrapperMock);
        connectorContextProviderMock.httpResponse = new HttpResponse(responseMock);

        // Act
        String secretKey = "secretkey";
        Date date = new Date();
        date = addDays(date, 1);
        String queueittoken = UserInQueueServiceTest.QueueITTokenGenerator.generateToken(date, "eventId", true,
                null, secretKey, "debug", "");

        try {
            KnownUser.cancelRequestByLocalConfig("http://test.com?event1=true", queueittoken, null, "customerId",
                    secretKey, connectorContextProviderMock);
        } catch (Exception ex) {
            exceptionWasThrown = "cancelConfig can not be null.".equals(ex.getMessage());
        }

        // Assert
        assertTrue(exceptionWasThrown);
        assertEquals(1, responseMock.addedCookies.size());
        String decodedCookieValue = URLDecoder.decode(responseMock.addedCookies.get(0).getValue(),
                "UTF-8");
        assertTrue(decodedCookieValue.contains("QueueitToken=" + queueittoken));
        assertTrue(
                decodedCookieValue.contains("OriginalUrl=http://test.com/?event1=true&queueittoken=queueittokenvalue"));
        assertTrue(decodedCookieValue.contains("CancelConfig=NULL"));
        assertTrue(decodedCookieValue.contains("SdkVersion=" + UserInQueueService.SDK_VERSION));
        assertTrue(decodedCookieValue.contains("Runtime=" + GetRuntimeVersion()));
        assertTrue(decodedCookieValue.contains("Exception=cancelConfig can not be null."));
    }

    @Test
    public void CancelRequestByLocalConfig_Debug_Missing_CustomerId() throws Exception {

        // Arrange
        HttpServletResponseMock responseMock = new HttpServletResponseMock();

        CancelEventConfig cancelEventConfig = new CancelEventConfig();
        cancelEventConfig.setCookieDomain("cookiedomain");
        cancelEventConfig.setEventId("event1");
        cancelEventConfig.setQueueDomain("queuedomain");
        cancelEventConfig.setVersion(1);
        cancelEventConfig.setActionName("cancelAction");

        // Act
        String secretKey = "secretkey";
        Date date = new Date();
        date = addDays(date, 1);

        String queueittoken = UserInQueueServiceTest.QueueITTokenGenerator.generateToken(date, "event1", true,
                null, secretKey, "debug", "");

        RequestValidationResult result = KnownUser.cancelRequestByLocalConfig("http://test.com?event1=true",
                queueittoken, cancelEventConfig, null, secretKey, connectorContextProviderMock);

        // Assert
        assertEquals("https://api2.queue-it.net/diagnostics/connector/error/?code=setup", result.getRedirectUrl());
        assertTrue(responseMock.addedCookies.isEmpty());
    }

    @Test
    public void CancelRequestByLocalConfig_Debug_Missing_SecretKey() throws Exception {

        // Arrange
        HttpServletResponseMock responseMock = new HttpServletResponseMock();

        CancelEventConfig cancelEventConfig = new CancelEventConfig();
        cancelEventConfig.setCookieDomain("cookiedomain");
        cancelEventConfig.setEventId("event1");
        cancelEventConfig.setQueueDomain("queuedomain");
        cancelEventConfig.setVersion(1);
        cancelEventConfig.setActionName("cancelAction");

        // Act
        String secretKey = "secretkey";
        Date date = new Date();
        date = addDays(date, 1);

        String queueittoken = UserInQueueServiceTest.QueueITTokenGenerator.generateToken(date, "event1", true,
                null, secretKey, "debug", "");

        RequestValidationResult result = KnownUser.cancelRequestByLocalConfig("http://test.com?event1=true",
                queueittoken, cancelEventConfig, "customerId", null, connectorContextProviderMock);

        // Assert
        assertEquals("https://api2.queue-it.net/diagnostics/connector/error/?code=setup", result.getRedirectUrl());
        assertTrue(responseMock.addedCookies.isEmpty());
    }

    @Test
    public void CancelRequestByLocalConfig_Debug_ExpiredToken() throws Exception {

        // Arrange
        HttpServletResponseMock responseMock = new HttpServletResponseMock();

        CancelEventConfig cancelEventConfig = new CancelEventConfig();
        cancelEventConfig.setCookieDomain("cookiedomain");
        cancelEventConfig.setEventId("eventid");
        cancelEventConfig.setQueueDomain("queuedomain");
        cancelEventConfig.setVersion(1);
        cancelEventConfig.setActionName("cancelAction");

        // Act
        String secretKey = "secretkey";
        Date date = new Date();
        date = addDays(date, -1);
        String queueittoken = UserInQueueServiceTest.QueueITTokenGenerator.generateToken(date, "event1", true,
                null, secretKey, "debug", "");

        RequestValidationResult result = KnownUser.cancelRequestByLocalConfig("http://test.com?event1=true",
                queueittoken, cancelEventConfig, "customerId", secretKey, connectorContextProviderMock);

        // Assert
        assertEquals("https://customerId.api2.queue-it.net/customerId/diagnostics/connector/error/?code=timestamp",
                result.getRedirectUrl());
        assertTrue(responseMock.addedCookies.isEmpty());
    }

    @Test
    public void CancelRequestByLocalConfig_Debug_ModifiedToken() throws Exception {

        // Assert
        HttpServletResponseMock responseMock = new HttpServletResponseMock();
        HttpServletRequestMock requestMock = new HttpServletRequestMock();
        requestMock.Headers.put("x-queueit-ajaxpageurl", "http%3A%2F%2Furl");

        CancelEventConfig cancelEventConfig = new CancelEventConfig();
        cancelEventConfig.setCookieDomain("cookiedomain");
        cancelEventConfig.setEventId("eventid");
        cancelEventConfig.setQueueDomain("queuedomain");
        cancelEventConfig.setVersion(1);
        cancelEventConfig.setActionName("cancelAction");

        // Act
        String secretKey = "secretkey";
        Date date = new Date();
        String queueittoken = UserInQueueServiceTest.QueueITTokenGenerator.generateToken(date, "event1", true,
                null, secretKey, "debug", "")
                + "invalid-hash";

        RequestValidationResult result = KnownUser.cancelRequestByLocalConfig("http://test.com?event1=true",
                queueittoken, cancelEventConfig, "customerId", secretKey, connectorContextProviderMock);

        // Assert
        assertEquals("https://customerId.api2.queue-it.net/customerId/diagnostics/connector/error/?code=hash",
                result.getRedirectUrl());
        assertTrue(responseMock.addedCookies.isEmpty());
    }

    public static class KnownUserRequestWrapperMock extends KnownUserRequestWrapper {

        public KnownUserRequestWrapperMock(HttpServletRequest request) {
            super(request);
        }
    }

    public static class HttpServletRequestMock implements HttpServletRequest {

        public Cookie[] CookiesValue;
        public String UserAgent;
        public String RequestURL;
        public String QueryString;
        public String RemoteAddr;
        public HashMap<String, String> Headers;

        public HttpServletRequestMock() {
            this.Headers = new HashMap<String, String>();
            this.RequestURL = "http://test.tesdomain.com/test";
            this.QueryString = "q=2#";
        }

        @Override
        public String getAuthType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Cookie[] getCookies() {
            return this.CookiesValue;
        }

        @Override
        public long getDateHeader(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getHeader(String key) {
            if ("User-Agent".equals(key)) {
                return this.UserAgent;
            }

            String value = this.Headers.get(key);

            if (value == null) {
                value = "";
            }

            return value;
        }

        @Override
        public Enumeration<String> getHeaders(String string) {
            if (this.Headers != null) {
                return this.Headers.values().stream().collect(null);
            }

            List<String> headers = Collections.emptyList();
            return Collections.enumeration(headers);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            if (this.Headers != null && this.Headers.values().size() > 0) {
                return Collections.enumeration(this.Headers.keySet());
            }

            List<String> headers = Collections.emptyList();
            return Collections.enumeration(headers);
        }

        @Override
        public int getIntHeader(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getMethod() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getPathInfo() {
            return "/test?q=2";
        }

        @Override
        public String getPathTranslated() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getContextPath() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getQueryString() {
            return this.QueryString;
        }

        @Override
        public String getRemoteUser() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isUserInRole(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Principal getUserPrincipal() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getRequestedSessionId() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getRequestURI() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public StringBuffer getRequestURL() {
            return new StringBuffer(this.RequestURL);
        }

        @Override
        public String getServletPath() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public HttpSession getSession(boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public HttpSession getSession() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isRequestedSessionIdValid() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isRequestedSessionIdFromCookie() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isRequestedSessionIdFromURL() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isRequestedSessionIdFromUrl() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean authenticate(HttpServletResponse hsr) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void login(String string, String string1) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void logout() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Collection<Part> getParts() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Part getPart(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object getAttribute(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getCharacterEncoding() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setCharacterEncoding(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getContentLength() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getContentType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ServletInputStream getInputStream() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getParameter(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Enumeration<String> getParameterNames() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String[] getParameterValues(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getProtocol() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getScheme() {
            return "http";
        }

        @Override
        public String getServerName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getServerPort() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public BufferedReader getReader() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getRemoteAddr() {
            return RemoteAddr;
        }

        @Override
        public String getRemoteHost() {
            return "test.tesdomain.com";
        }

        @Override
        public void setAttribute(String string, Object o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void removeAttribute(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Locale getLocale() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Enumeration<Locale> getLocales() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isSecure() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getRealPath(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getRemotePort() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getLocalName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getLocalAddr() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getLocalPort() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ServletContext getServletContext() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public AsyncContext startAsync() throws IllegalStateException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public AsyncContext startAsync(ServletRequest sr, ServletResponse sr1) throws IllegalStateException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isAsyncStarted() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isAsyncSupported() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public AsyncContext getAsyncContext() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public DispatcherType getDispatcherType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String changeSessionId() {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from
                                                                           // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        public <T extends HttpUpgradeHandler> T upgrade(Class<T> type) throws IOException, ServletException {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from
                                                                           // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        @Override
        public long getContentLengthLong() {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from
                                                                           // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
    }

    public static class HttpServletResponseMock implements HttpServletResponse {

        ArrayList<Cookie> addedCookies = new ArrayList<Cookie>();

        @Override
        public void addCookie(Cookie cookie) {
            addedCookies.add(cookie);
        }

        @Override
        public boolean containsHeader(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String encodeURL(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String encodeRedirectURL(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String encodeUrl(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String encodeRedirectUrl(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void sendError(int i, String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void sendError(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void sendRedirect(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setDateHeader(String string, long l) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addDateHeader(String string, long l) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setHeader(String string, String string1) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addHeader(String string, String string1) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setIntHeader(String string, int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addIntHeader(String string, int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setStatus(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setStatus(int i, String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getStatus() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getHeader(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Collection<String> getHeaders(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Collection<String> getHeaderNames() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getCharacterEncoding() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getContentType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ServletOutputStream getOutputStream() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public PrintWriter getWriter() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setCharacterEncoding(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setContentLength(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setContentType(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setBufferSize(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getBufferSize() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void flushBuffer() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void resetBuffer() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isCommitted() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void reset() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setLocale(Locale locale) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Locale getLocale() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setContentLengthLong(long l) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from
                                                                           // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
    }

    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); // minus number would decrement the days
        return cal.getTime();
    }

    public static String GetRuntimeVersion() throws UnsupportedEncodingException {
        return URLEncoder.encode(System.getProperty("java.runtime.version"), "UTF-8");
    }
}
