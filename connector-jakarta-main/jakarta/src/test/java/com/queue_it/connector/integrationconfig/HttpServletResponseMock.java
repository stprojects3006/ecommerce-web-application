package com.queue_it.connector.integrationconfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class HttpServletResponseMock implements HttpServletResponse {

    HttpServletRequestMock httpRequest;

    public HttpServletResponseMock(HttpServletRequestMock httpRequest) {
        this.httpRequest = httpRequest;
        this.httpRequest.CookiesValue = new HashMap<String, Cookie>();
    }

    @Override
    public String getCharacterEncoding() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCharacterEncoding'");
    }

    @Override
    public String getContentType() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getContentType'");
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getOutputStream'");
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getWriter'");
    }

    @Override
    public void setCharacterEncoding(String charset) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setCharacterEncoding'");
    }

    @Override
    public void setContentLength(int len) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setContentLength'");
    }

    @Override
    public void setContentType(String type) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setContentType'");
    }

    @Override
    public void setBufferSize(int size) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setBufferSize'");
    }

    @Override
    public int getBufferSize() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBufferSize'");
    }

    @Override
    public void flushBuffer() throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'flushBuffer'");
    }

    @Override
    public void resetBuffer() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'resetBuffer'");
    }

    @Override
    public boolean isCommitted() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isCommitted'");
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'reset'");
    }

    @Override
    public void setLocale(Locale loc) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setLocale'");
    }

    @Override
    public Locale getLocale() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLocale'");
    }

    @Override
    public void addCookie(Cookie cookie) {
        httpRequest.CookiesValue.put(cookie.getName(), cookie);
    }

    @Override
    public boolean containsHeader(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'containsHeader'");
    }

    @Override
    public String encodeURL(String url) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'encodeURL'");
    }

    @Override
    public String encodeRedirectURL(String url) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'encodeRedirectURL'");
    }

    @Override
    public String encodeUrl(String url) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'encodeUrl'");
    }

    @Override
    public String encodeRedirectUrl(String url) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'encodeRedirectUrl'");
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendError'");
    }

    @Override
    public void sendError(int sc) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendError'");
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendRedirect'");
    }

    @Override
    public void setDateHeader(String name, long date) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setDateHeader'");
    }

    @Override
    public void addDateHeader(String name, long date) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addDateHeader'");
    }

    @Override
    public void setHeader(String name, String value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setHeader'");
    }

    @Override
    public void addHeader(String name, String value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addHeader'");
    }

    @Override
    public void setIntHeader(String name, int value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setIntHeader'");
    }

    @Override
    public void addIntHeader(String name, int value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addIntHeader'");
    }

    @Override
    public void setStatus(int sc) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setStatus'");
    }

    @Override
    public void setStatus(int sc, String sm) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setStatus'");
    }

    @Override
    public int getStatus() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStatus'");
    }

    @Override
    public String getHeader(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHeader'");
    }

    @Override
    public Collection<String> getHeaders(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHeaders'");
    }

    @Override
    public Collection<String> getHeaderNames() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHeaderNames'");
    }

    @Override
    public void setContentLengthLong(long l) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from
                                                                       // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}