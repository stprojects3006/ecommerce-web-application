package com.dharshi.loadtesting.service;

import com.dharshi.loadtesting.model.TestResult;
import com.dharshi.loadtesting.repository.TestResultRepository;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LoadTestService {
    
    private static final Logger logger = LoggerFactory.getLogger(LoadTestService.class);
    
    @Autowired
    private TestResultRepository testResultRepository;
    
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final AtomicInteger threadCounter = new AtomicInteger(0);
    
    @Async
    public CompletableFuture<TestResult> executeHttpRequest(String testName, String url, 
                                                          String method, String payload, 
                                                          String authToken) {
        long startTime = System.currentTimeMillis();
        TestResult result = new TestResult();
        result.setTestName(testName);
        result.setEndpoint(url);
        result.setHttpMethod(method);
        result.setThreadId("Thread-" + threadCounter.incrementAndGet());
        
        try {
            HttpUriRequestBase request = createRequest(method, url, payload, authToken);
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                long endTime = System.currentTimeMillis();
                long responseTime = endTime - startTime;
                
                result.setResponseTimeMs(responseTime);
                result.setStatusCode(response.getCode());
                result.setSuccess(response.getCode() >= 200 && response.getCode() < 300);
                
                if (response.getEntity() != null) {
                    result.setResponseSizeBytes((long) response.getEntity().getContent().available());
                }
                
                logger.info("Request completed: {} {} - Status: {} - Time: {}ms", 
                           method, url, response.getCode(), responseTime);
            }
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;
            
            result.setResponseTimeMs(responseTime);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            
            logger.error("Request failed: {} {} - Error: {}", method, url, e.getMessage());
        }
        
        // Save result to database
        testResultRepository.save(result);
        
        return CompletableFuture.completedFuture(result);
    }
    
    private HttpUriRequestBase createRequest(String method, String url, String payload, String authToken) {
        HttpUriRequestBase request;
        
        switch (method.toUpperCase()) {
            case "GET":
                request = new HttpGet(url);
                break;
            case "POST":
                HttpPost postRequest = new HttpPost(url);
                if (payload != null && !payload.isEmpty()) {
                    postRequest.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
                }
                request = postRequest;
                break;
            case "PUT":
                HttpPut putRequest = new HttpPut(url);
                if (payload != null && !payload.isEmpty()) {
                    putRequest.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
                }
                request = putRequest;
                break;
            case "DELETE":
                request = new HttpDelete(url);
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
        
        // Add headers
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");
        
        if (authToken != null && !authToken.isEmpty()) {
            request.setHeader("Authorization", "Bearer " + authToken);
        }
        
        return request;
    }
    
    public void cleanup() {
        try {
            httpClient.close();
        } catch (IOException e) {
            logger.error("Error closing HTTP client", e);
        }
    }
} 