package com.dharshi.loadtesting.controller;

import com.dharshi.loadtesting.model.TestResult;
import com.dharshi.loadtesting.service.LoadTestService;
import com.dharshi.loadtesting.repository.TestResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/loadtest")
@CrossOrigin(origins = "*")
public class LoadTestController {
    
    @Autowired
    private LoadTestService loadTestService;
    
    @Autowired
    private TestResultRepository testResultRepository;
    
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeLoadTest(@RequestBody LoadTestRequest request) {
        try {
            long startTime = System.currentTimeMillis();
            
            // Execute concurrent requests
            CompletableFuture<TestResult>[] futures = new CompletableFuture[request.getConcurrentUsers()];
            
            for (int i = 0; i < request.getConcurrentUsers(); i++) {
                futures[i] = loadTestService.executeHttpRequest(
                    request.getTestName(),
                    request.getUrl(),
                    request.getMethod(),
                    request.getPayload(),
                    request.getAuthToken()
                );
            }
            
            // Wait for all requests to complete
            CompletableFuture.allOf(futures).get(request.getTimeoutSeconds(), TimeUnit.SECONDS);
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            // Collect results
            List<TestResult> results = testResultRepository.findByTestName(request.getTestName());
            
            Map<String, Object> response = Map.of(
                "testName", request.getTestName(),
                "totalRequests", request.getConcurrentUsers(),
                "totalTimeMs", totalTime,
                "results", results
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/results/{testName}")
    public ResponseEntity<List<TestResult>> getTestResults(@PathVariable String testName) {
        List<TestResult> results = testResultRepository.findByTestName(testName);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/stats/{testName}")
    public ResponseEntity<Map<String, Object>> getTestStats(@PathVariable String testName) {
        Double avgResponseTime = testResultRepository.getAverageResponseTime(testName);
        Long successCount = testResultRepository.getSuccessCount(testName);
        Long failureCount = testResultRepository.getFailureCount(testName);
        Long minResponseTime = testResultRepository.getMinResponseTime(testName);
        Long maxResponseTime = testResultRepository.getMaxResponseTime(testName);
        
        long totalRequests = (successCount != null ? successCount : 0) + 
                           (failureCount != null ? failureCount : 0);
        
        double successRate = totalRequests > 0 ? 
            (double) (successCount != null ? successCount : 0) / totalRequests * 100 : 0;
        
        Map<String, Object> stats = Map.of(
            "testName", testName,
            "totalRequests", totalRequests,
            "successCount", successCount != null ? successCount : 0,
            "failureCount", failureCount != null ? failureCount : 0,
            "successRate", String.format("%.2f%%", successRate),
            "avgResponseTimeMs", avgResponseTime != null ? avgResponseTime : 0,
            "minResponseTimeMs", minResponseTime != null ? minResponseTime : 0,
            "maxResponseTimeMs", maxResponseTime != null ? maxResponseTime : 0
        );
        
        return ResponseEntity.ok(stats);
    }
    
    @DeleteMapping("/results/{testName}")
    public ResponseEntity<String> clearTestResults(@PathVariable String testName) {
        List<TestResult> results = testResultRepository.findByTestName(testName);
        testResultRepository.deleteAll(results);
        return ResponseEntity.ok("Test results cleared for: " + testName);
    }
    
    // Request DTO
    public static class LoadTestRequest {
        private String testName;
        private String url;
        private String method;
        private String payload;
        private String authToken;
        private int concurrentUsers;
        private int timeoutSeconds;
        
        // Getters and Setters
        public String getTestName() { return testName; }
        public void setTestName(String testName) { this.testName = testName; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        
        public String getPayload() { return payload; }
        public void setPayload(String payload) { this.payload = payload; }
        
        public String getAuthToken() { return authToken; }
        public void setAuthToken(String authToken) { this.authToken = authToken; }
        
        public int getConcurrentUsers() { return concurrentUsers; }
        public void setConcurrentUsers(int concurrentUsers) { this.concurrentUsers = concurrentUsers; }
        
        public int getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    }
} 