package com.dharshi.loadtesting.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_results")
public class TestResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "test_name")
    private String testName;
    
    @Column(name = "endpoint")
    private String endpoint;
    
    @Column(name = "http_method")
    private String httpMethod;
    
    @Column(name = "response_time_ms")
    private Long responseTimeMs;
    
    @Column(name = "status_code")
    private Integer statusCode;
    
    @Column(name = "success")
    private Boolean success;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "thread_id")
    private String threadId;
    
    @Column(name = "timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    @Column(name = "request_size_bytes")
    private Long requestSizeBytes;
    
    @Column(name = "response_size_bytes")
    private Long responseSizeBytes;
    
    // Constructors
    public TestResult() {}
    
    public TestResult(String testName, String endpoint, String httpMethod, 
                     Long responseTimeMs, Integer statusCode, Boolean success) {
        this.testName = testName;
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.responseTimeMs = responseTimeMs;
        this.statusCode = statusCode;
        this.success = success;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }
    
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    
    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    
    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    
    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
    
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public String getThreadId() { return threadId; }
    public void setThreadId(String threadId) { this.threadId = threadId; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public Long getRequestSizeBytes() { return requestSizeBytes; }
    public void setRequestSizeBytes(Long requestSizeBytes) { this.requestSizeBytes = requestSizeBytes; }
    
    public Long getResponseSizeBytes() { return responseSizeBytes; }
    public void setResponseSizeBytes(Long responseSizeBytes) { this.responseSizeBytes = responseSizeBytes; }
} 