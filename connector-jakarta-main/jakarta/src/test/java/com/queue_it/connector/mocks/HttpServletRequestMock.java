package com.queue_it.connector.mocks;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
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
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;

import com.queue_it.connector.UserInQueueStateCookieRepositoryTest;

public class HttpServletRequestMock implements HttpServletRequest {

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
        Cookie[] cookies = new Cookie[UserInQueueStateCookieRepositoryTest.CookiesValue.size()];
        return UserInQueueStateCookieRepositoryTest.CookiesValue.values().toArray(cookies);
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
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
            throws IllegalStateException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'startAsync'");
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authenticate'");
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