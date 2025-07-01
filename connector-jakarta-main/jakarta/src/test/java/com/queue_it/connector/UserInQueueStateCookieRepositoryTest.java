package com.queue_it.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;

import org.junit.Test;

import com.queue_it.connector.helpers.HashHelper;
import com.queue_it.connector.mocks.HttpServletRequestMock;
import com.queue_it.connector.mocks.HttpServletResponseMock;
import com.queue_it.connector.mocks.KnownUserRequestWrapperMock;
import com.queue_it.connector.models.StateInfo;

import jakarta.servlet.http.Cookie;

public class UserInQueueStateCookieRepositoryTest {

    public static HashMap<String, Cookie> CookiesValue;

    static class ConnectorContextProviderMock implements IConnectorContextProvider {

        KnownUserRequestWrapperMock knownUserRequestWrapperMock;
        HttpServletRequestMock httpServletRequestMock;
        IHttpRequest httpRequest;
        HttpServletResponseMock httpServletResponseMock;
        IHttpResponse httpResponse;

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

    public UserInQueueStateCookieRepositoryTest() {
        CookiesValue = new HashMap<String, Cookie>();
    }

    @Test
    public void store_getState_ExtendableCookie_CookieIsSaved() throws Exception {
        String eventId = "event1";
        String secretKey = "4e1db821-a825-49da-acd0-5d376f2068db";
        String cookieDomain = ".test.com";
        String queueId = "528f01d4-30f9-4753-95b3-2c8c33966abc";
        String cookieKey = UserInQueueStateCookieRepository.getCookieKey(eventId);
        int cookieValidity = 10;

        ConnectorContextProviderMock connectorContextProviderMock = new ConnectorContextProviderMock();

        HttpServletResponseMock httpResponseMock = new HttpServletResponseMock();
        HttpServletRequestMock httpRequestMock = new HttpServletRequestMock();
        connectorContextProviderMock.httpServletRequestMock = httpRequestMock;
        connectorContextProviderMock.httpServletResponseMock = httpResponseMock;

        UserInQueueStateCookieRepository sut = new UserInQueueStateCookieRepository(connectorContextProviderMock);

        sut.store(eventId, queueId, null, cookieDomain, true, false, "Queue", "", secretKey);
        StateInfo state = sut.getState(eventId, cookieValidity, secretKey, true);

        assertTrue(state.isValid());
        assertEquals(state.getQueueId(), queueId);
        assertTrue(state.isStateExtendable());
        assertEquals("Queue", state.getRedirectType());

        String cookieValue = getCookieValueByKey(cookieKey);

        cookieValue = String.format("%s&expiration=%d&cookieDomain=%s&isCookieHttpOnly=%s&isCookieSecure=%s",
                cookieValue, 24 * 60 * 60, cookieDomain, true, false);

        HashMap<String, String> cookieNameValueMap = UserInQueueStateCookieRepository
                .getCookieNameValueMap(String.valueOf(cookieValue));

        long issueTime = Long.parseLong(cookieNameValueMap.get("IssueTime"));
        assertTrue(Math.abs(System.currentTimeMillis() / 1000L - issueTime) < 2);
        assertEquals(Integer.parseInt(cookieNameValueMap.get("expiration").toString()),
                24 * 60 * 60);
        assertEquals(cookieNameValueMap.get("cookieDomain"), cookieDomain);
        assertEquals(cookieNameValueMap.get("isCookieHttpOnly"), "true");
        assertEquals(cookieNameValueMap.get("isCookieSecure"), "false");
    }

    @Test
    public void store_getState_TamperedCookie_StateIsNotValid() throws Exception {
        String eventId = "event1";
        String secretKey = "4e1db821-a825-49da-acd0-5d376f2068db";
        String cookieDomain = ".test.com";
        String queueId = "528f01d4-30f9-4753-95b3-2c8c33966abc";
        String cookieKey = UserInQueueStateCookieRepository.getCookieKey(eventId);
        int cookieValidity = 10;

        ConnectorContextProviderMock connectorContextProviderMock = new ConnectorContextProviderMock();
        HttpServletResponseMock httpResponseMock = new HttpServletResponseMock();
        HttpServletRequestMock httpRequestMock = new HttpServletRequestMock();
        connectorContextProviderMock.httpServletRequestMock = httpRequestMock;
        connectorContextProviderMock.httpServletResponseMock = httpResponseMock;

        UserInQueueStateCookieRepository sut = new UserInQueueStateCookieRepository(connectorContextProviderMock);
        sut.store(eventId, queueId, cookieValidity, cookieDomain, true, true, "Queue", "", secretKey);

        StateInfo state = sut.getState(eventId, 10, secretKey, true);
        assertTrue(state.isValid());

        String currentCookie = getCookieValueByKey(cookieKey);

        currentCookie = currentCookie.replace("FixedValidityMins=10&", "");
        Cookie updatedCookie = new Cookie(cookieKey, currentCookie);
        connectorContextProviderMock.httpServletResponseMock.addCookie(updatedCookie);
        state = sut.getState(eventId, 10, secretKey, true);
        assertFalse(state.isValid());
    }

    @Test
    public void store_getState_ExpiredCookie_StateIsNotValid_Queue() throws Exception {
        String eventId = "event1";
        String secretKey = "4e1db821-a825-49da-acd0-5d376f2068db";
        String cookieDomain = ".test.com";
        String queueId = "528f01d4-30f9-4753-95b3-2c8c33966abc";

        IConnectorContextProvider connectorContextProvider = new ConnectorContextProviderMock();

        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(connectorContextProvider);

        testObject.store(eventId, queueId, null, cookieDomain, true, true, "Queue", "", secretKey);

        StateInfo state = testObject.getState(eventId, -1, secretKey, true);

        assertFalse(state.isValid());
    }

    @Test
    public void store_getState_ExpiredCookie_StateIsNotValid_Idle() throws Exception {
        String eventId = "event1";
        String secretKey = "4e1db821-a825-49da-acd0-5d376f2068db";
        String cookieDomain = ".test.com";
        String queueId = "528f01d4-30f9-4753-95b3-2c8c33966abc";

        IConnectorContextProvider connectorContextProvider = new ConnectorContextProviderMock();

        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(connectorContextProvider);

        testObject.store(eventId, queueId, -1, cookieDomain, true, true, "Idle", "", secretKey);

        StateInfo state = testObject.getState(eventId, 10, secretKey, true);
        assertFalse(state.isValid());
    }

    @Test
    public void store_getState_DifferentEventId_StateIsNotValid() throws Exception {
        String eventId = "event1";
        String secretKey = "4e1db821-a825-49da-acd0-5d376f2068db";
        String cookieDomain = ".test.com";
        String queueId = "528f01d4-30f9-4753-95b3-2c8c33966abc";

        IConnectorContextProvider connectorContextProvider = new ConnectorContextProviderMock();

        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(connectorContextProvider);

        testObject.store(eventId, queueId, null, cookieDomain, true, true, "Queue", "", secretKey);
        StateInfo state = testObject.getState(eventId, 10, secretKey, true);

        assertTrue(state.isValid());

        state = testObject.getState("event2", 10, secretKey, true);
        assertFalse(state.isValid());
    }

    @Test
    public void store_getState_InvalidCookie_StateIsNotValid() throws Exception {
        String eventId = "event1";
        String secretKey = "4e1db821-a825-49da-acd0-5d376f2068db";

        ConnectorContextProviderMock connectorContextProviderMock = new ConnectorContextProviderMock();
        HttpServletResponseMock httpResponseMock = new HttpServletResponseMock();
        HttpServletRequestMock httpRequestMock = new HttpServletRequestMock();
        connectorContextProviderMock.httpServletRequestMock = httpRequestMock;
        connectorContextProviderMock.httpServletResponseMock = httpResponseMock;

        UserInQueueStateCookieRepository sut = new UserInQueueStateCookieRepository(connectorContextProviderMock);

        Cookie invalidCookie = new Cookie("blablabla", "FixedValidityMins=ooOOO&Expires=|||&QueueId=000&Hash=23232$$$");

        connectorContextProviderMock.httpServletResponseMock.addCookie(invalidCookie);

        StateInfo state = sut.getState(eventId, 10, secretKey, true);

        assertFalse(state.isValid());
    }

    @Test
    public void cancelQueueCookie_Test() throws Exception {
        String eventId = "event1";
        String secretKey = "secretKey";
        String queueId = "528f01d4-30f9-4753-95b3-2c8c33966abc";
        String cookieDomain = "testdomain";

        String cookieKey = UserInQueueStateCookieRepository.getCookieKey(eventId);

        ConnectorContextProviderMock connectorContextProviderMock = new ConnectorContextProviderMock();

        HttpServletResponseMock httpResponseMock = new HttpServletResponseMock();
        HttpServletRequestMock httpRequestMock = new HttpServletRequestMock();

        connectorContextProviderMock.httpServletRequestMock = httpRequestMock;
        connectorContextProviderMock.httpServletResponseMock = httpResponseMock;

        UserInQueueStateCookieRepository sut = new UserInQueueStateCookieRepository(connectorContextProviderMock);

        Cookie invalidCookie = new Cookie("blablabla", "FixedValidityMins=ooOOO&Expires=|||&QueueId=000&Hash=23232$$$");

        connectorContextProviderMock.httpServletResponseMock.addCookie(invalidCookie);

        sut.store(eventId, queueId, -1, "cookiedomain", true, true, "Idle", "", secretKey);

        assertTrue(sut.getState(eventId, 10, secretKey, false).isValid());

        sut.cancelQueueCookie(eventId, cookieDomain, true, true);

        Cookie cookie1 = new Cookie(cookieKey + "1", null);
        cookie1.setMaxAge(0);
        cookie1.setDomain(cookieDomain);
        cookie1.setHttpOnly(false);
        cookie1.setSecure(false);

        Cookie cookie2 = new Cookie(cookieKey + "2", null);
        cookie2.setMaxAge(0);
        cookie2.setDomain(cookieDomain);
        cookie2.setHttpOnly(false);
        cookie2.setSecure(false);

        connectorContextProviderMock.httpServletResponseMock.addCookie(cookie1);
        connectorContextProviderMock.httpServletResponseMock.addCookie(cookie2);

        assertEquals(0, cookie2.getMaxAge());
        assertNull(cookie2.getValue());
        assertEquals(cookieDomain, cookie2.getDomain());
    }

    @Test
    public void extendQueueCookie_CookieExists_Test() throws URISyntaxException, UnsupportedEncodingException {

        String eventId = "event1";
        String secretKey = "secretKey";
        String queueId = "528f01d4-30f9-4753-95b3-2c8c33966abc";
        final String cookieKey = UserInQueueStateCookieRepository.getCookieKey(eventId);
        long issueTime = (System.currentTimeMillis() / 1000L - 120);
        String hash = HashHelper.generateSHA256Hash(secretKey, eventId + queueId + "3" + "idle" + issueTime);
        final String cookieValue = "EventId=" + eventId + "&QueueId=" + queueId
                + "&FixedValidityMins=3&RedirectType=idle&IssueTime=" + issueTime
                + "&IsCookieHttpOnly=True&IsCookieSecure=True&Hash=" + hash;

        ConnectorContextProviderMock connectorContextProviderMock = new ConnectorContextProviderMock();

        connectorContextProviderMock.getHttpResponse().setCookie(cookieKey, cookieValue, 180, cookieValue, true, true);

        UserInQueueStateCookieRepository sut = new UserInQueueStateCookieRepository(connectorContextProviderMock);

        assertTrue(sut.getState(eventId, 10, secretKey, true).isValid());

        sut.reissueQueueCookie(eventId, 12, "cookieDomain", true, true, secretKey);

        StateInfo state = sut.getState(eventId, 10, secretKey, true);

        assertTrue(state.isValid());
        assertEquals(state.getQueueId(), queueId);
        assertFalse(state.isStateExtendable());
        assertEquals("idle", state.getRedirectType());

        Cookie reissuedCookie = getCookieByKey(cookieKey);

        HashMap<String, String> cookieNameValueMap = UserInQueueStateCookieRepository
                .getCookieNameValueMap(String.valueOf(URLDecoder.decode(reissuedCookie.getValue(), "UTF-8")));

        long newIssueTime = Long.parseLong(cookieNameValueMap.get("IssueTime"));

        assertTrue(Math.abs(System.currentTimeMillis() / 1000L - newIssueTime) < 2);
        assertEquals(24 * 60 * 60, reissuedCookie.getMaxAge());
        assertEquals("cookiedomain", reissuedCookie.getDomain());
        assertTrue(reissuedCookie.isHttpOnly());
        assertTrue(reissuedCookie.getSecure());
    }

    @Test
    public void extendQueueCookie_CookieDoesNotExist_Test() throws URISyntaxException {

        String eventId = "event1";
        String secretKey = "secretKey";
        final HashMap<String, Boolean> conditions = new HashMap<String, Boolean>();
        conditions.put("isSetCookieCalled", false);

        IConnectorContextProvider connectorContextProvider = new ConnectorContextProviderMock();

        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(connectorContextProvider);

        testObject.reissueQueueCookie(eventId, 12, "queueDomain", true, true, secretKey);

        assertFalse(conditions.get("isSetCookieCalled"));
    }

    @Test
    public void getState_ValidCookieFormat_Extendable_Test() throws URISyntaxException {

        String eventId = "event1";
        String secretKey = "secretKey";
        String queueId = "f8757c2d-34c2-4639-bef2-1736cdd30bbb";
        final String cookieKey = UserInQueueStateCookieRepository.getCookieKey(eventId);
        long issueTime = (System.currentTimeMillis() / 1000L - 120);
        String hash = HashHelper.generateSHA256Hash(secretKey, eventId + queueId + "queue" + issueTime);
        final String cookieValue = "EventId=" + eventId + "&QueueId=" + queueId + "&RedirectType=queue&IssueTime="
                + issueTime + "&Hash=" + hash;

        IConnectorContextProvider connectorContextProvider = new ConnectorContextProviderMock();

        connectorContextProvider.getHttpResponse().setCookie(cookieKey, cookieValue, 180, cookieValue, true, true);

        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(connectorContextProvider);

        StateInfo cookieState = testObject.getState(eventId, 10, secretKey, true);

        assertTrue(cookieState.isValid());
        assertTrue(cookieState.isFound());
        assertEquals(cookieState.getQueueId(), queueId);
        assertEquals("queue", cookieState.getRedirectType());
        assertTrue(cookieState.isStateExtendable());
    }

    @Test
    public void getState_ValidCookieFormat_NonExtendable_Test() throws URISyntaxException {

        String eventId = "event1";
        String secretKey = "secretKey";
        String queueId = "f8757c2d-34c2-4639-bef2-1736cdd30bbb";
        final String cookieKey = UserInQueueStateCookieRepository.getCookieKey(eventId);
        long issueTime = (System.currentTimeMillis() / 1000L - 120);
        String hash = HashHelper.generateSHA256Hash(secretKey, eventId + queueId + "3" + "idle" + issueTime);
        final String cookieValue = "EventId=" + eventId + "&QueueId=" + queueId
                + "&FixedValidityMins=3&RedirectType=idle&IssueTime=" + issueTime + "&Hash=" + hash;

        IConnectorContextProvider connectorContextProvider = new ConnectorContextProviderMock();

        connectorContextProvider.getHttpResponse().setCookie(cookieKey, cookieValue, 180, cookieValue, true, true);

        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(connectorContextProvider);
        StateInfo cookieState = testObject.getState(eventId, 10, secretKey, true);

        assertTrue(cookieState.isValid());
        assertTrue(cookieState.isFound());
        assertEquals(cookieState.getQueueId(), queueId);
        assertEquals("idle", cookieState.getRedirectType());
        assertFalse(cookieState.isStateExtendable());
    }

    @Test
    public void getState_OldCookie_InValid_ExpiredCookie_Extendable_Test() throws URISyntaxException {

        String eventId = "event1";
        String secretKey = "secretKey";
        String queueId = "f8757c2d-34c2-4639-bef2-1736cdd30bbb";
        final String cookieKey = UserInQueueStateCookieRepository.getCookieKey(eventId);
        long issueTime = (System.currentTimeMillis() / 1000L - (11 * 60));
        String hash = HashHelper.generateSHA256Hash(secretKey, eventId + queueId + "queue" + issueTime);
        final String cookieValue = "EventId=" + eventId + "&QueueId=" + queueId + "&RedirectType=queue&IssueTime="
                + issueTime + "&Hash=" + hash;

        IConnectorContextProvider connectorContextProvider = new ConnectorContextProviderMock();

        connectorContextProvider.getHttpResponse().setCookie(cookieKey, cookieValue, 180, cookieValue, true, true);

        UserInQueueStateCookieRepository testObject = new UserInQueueStateCookieRepository(connectorContextProvider);
        StateInfo cookieState = testObject.getState(eventId, 10, secretKey, true);

        assertFalse(cookieState.isValid());
        assertTrue(cookieState.isFound());
    }

    @Test
    public void getState_OldCookie_InValid_ExpiredCookie_NonExtendable_Test() throws URISyntaxException {
        String eventId = "event1";
        String secretKey = "secretKey";
        String queueId = "f8757c2d-34c2-4639-bef2-1736cdd30bbb";
        final String cookieKey = UserInQueueStateCookieRepository.getCookieKey(eventId);
        long issueTime = (System.currentTimeMillis() / 1000L - (4 * 60));
        String hash = HashHelper.generateSHA256Hash(secretKey, eventId + queueId + "3" + "idle" + issueTime);
        final String cookieValue = "EventId=" + eventId + "&QueueId=" + queueId
                + "&FixedValidityMins=3&RedirectType=idle&IssueTime=" + issueTime + "&Hash=" + hash;

        IConnectorContextProvider connectorContextProvider = new ConnectorContextProviderMock();

        connectorContextProvider.getHttpResponse().setCookie(cookieKey, cookieValue, 180, cookieValue, true, true);

        UserInQueueStateCookieRepository sut = new UserInQueueStateCookieRepository(connectorContextProvider);
        StateInfo cookieState = sut.getState(eventId, 3, secretKey, true);

        assertFalse(cookieState.isValid());
        assertTrue(cookieState.isFound());
    }

    @Test
    public void getState_NoCookie_Test() throws URISyntaxException {
        String eventId = "event1";
        String secretKey = "4e1db821-a825-49da-acd0-5d376f2068db";

        IConnectorContextProvider connectorContextProvider = new ConnectorContextProviderMock();

        UserInQueueStateCookieRepository sut = new UserInQueueStateCookieRepository(connectorContextProvider);
        StateInfo cookieState = sut.getState(eventId, 10, secretKey, true);

        assertFalse(cookieState.isValid());
        assertFalse(cookieState.isFound());
    }

    @Test
    public void test_getState_noQueueId_validateCookieDoesNotContainQueueId() throws Exception {
        String eventId = "event1";
        String secretKey = "4e1db821-a825-49da-acd0-5d376f2068db";
        String cookieDomain = ".test.com";
        String queueId = null;
        String cookieKey = UserInQueueStateCookieRepository.getCookieKey(eventId);
        int cookieValidity = 10;
        final HashMap<String, HashMap<String, Object>> cookies = new HashMap<String, HashMap<String, Object>>();
        cookies.put(cookieKey, new HashMap<String, Object>());

        IConnectorContextProvider connectorContextProvider = new ConnectorContextProviderMock();

        UserInQueueStateCookieRepository sut = new UserInQueueStateCookieRepository(connectorContextProvider);
        sut.store(eventId, queueId, null, cookieDomain, true, true, "Queue", "", secretKey);
        StateInfo state = sut.getState(eventId, cookieValidity, secretKey, true);

        assertTrue(state.isValid());
        assertEquals(state.getQueueId(), null);
        assertTrue(state.isStateExtendable());
        assertEquals("Queue", state.getRedirectType());

        String cookieValue = connectorContextProvider.getHttpRequest().getCookieValue(cookieKey);
        cookieValue = String.format("%s&expiration=%d&cookieDomain=%s&isCookieHttpOnly=%b&isCookieSecure=%b",
                cookieValue,
                24 * 60 * 60, cookieDomain, true, true);

        connectorContextProvider.getHttpResponse().setCookie(cookieKey, cookieValue, 24 * 60 * 60, cookieDomain, true,
                true);

        HashMap<String, String> cookieNameValueMap = UserInQueueStateCookieRepository
                .getCookieNameValueMap(String.valueOf(cookieValue));

        assertEquals(Integer.parseInt(cookieNameValueMap.get("expiration").toString()), 24 * 60 * 60);
        assertEquals(cookieNameValueMap.get("cookieDomain"), cookieDomain);
        assertEquals(cookieNameValueMap.get("isCookieHttpOnly"), "true");
        assertEquals(cookieNameValueMap.get("isCookieSecure"), "true");
    }

    public String getCookieValueByKey(String cookieKey) throws UnsupportedEncodingException {
        for (Cookie cookie : CookiesValue.values()) {
            if (cookie.getName().equals(cookieKey))
                return URLDecoder.decode(cookie.getValue(), "UTF-8");
        }
        return "";
    }

    public Cookie getCookieByKey(String cookieKey) {
        for (Cookie cookie : CookiesValue.values()) {
            if (cookie.getName().equals(cookieKey))
                return cookie;
        }
        return null;
    }
}
