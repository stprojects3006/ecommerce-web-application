package com.queue_it.connector.mocks;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import com.queue_it.connector.UserInQueueStateCookieRepositoryTest;

public class HttpServletResponseMock implements HttpServletResponse {

    @Override
    public void addCookie(Cookie cookie) {
        UserInQueueStateCookieRepositoryTest.CookiesValue.put(cookie.getName(), cookie);
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
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}