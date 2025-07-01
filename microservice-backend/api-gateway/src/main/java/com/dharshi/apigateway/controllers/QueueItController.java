package com.dharshi.apigateway.controllers;

import com.queueit.connector.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.time.Instant;
import java.io.IOException;

@RestController
@RequestMapping("/api/queueit")
@CrossOrigin(origins = "*")
public class QueueItController {

    private static final Logger logger = LoggerFactory.getLogger(QueueItController.class);

    @Value("${queueit.customer-id:futuraforge}")
    private String customerId;

    @Value("${queueit.secret-key:your-secret-key}")
    private String secretKey;

    @Value("${queueit.api-key:your-api-key}")
    private String apiKey;

    @Value("${queueit.queue-domain:futuraforge.queue-it.net}")
    private String queueDomain;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final KnownUser knownUser = new KnownUser();

    /**
     * Validate Queue-It token and handle queue logic
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateQueueToken(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            String eventId = (String) requestBody.get("eventId");
            String queueitToken = (String) requestBody.get("queueitToken");
            String originalUrl = (String) requestBody.get("originalUrl");
            
            logger.info("Validating Queue-It token for event: {}", eventId);
            
            // Create Queue-It request
            IHttpRequestProvider httpRequestProvider = new HttpRequestProvider(request);
            IHttpResponseProvider httpResponseProvider = new HttpResponseProvider(response);
            
            // Validate the token using Queue-It connector
            ValidateResult result = knownUser.validateRequestByLocalEvent(
                originalUrl,
                queueitToken,
                eventId,
                customerId,
                secretKey,
                httpRequestProvider,
                httpResponseProvider
            );
            
            Map<String, Object> responseMap = new HashMap<>();
            
            if (result.doRedirect()) {
                // User needs to be redirected to queue
                responseMap.put("redirect", true);
                responseMap.put("redirectUrl", result.getRedirectUrl());
                responseMap.put("eventId", eventId);
                logger.info("User redirected to queue: {}", result.getRedirectUrl());
            } else {
                // User can proceed
                responseMap.put("redirect", false);
                responseMap.put("eventId", eventId);
                responseMap.put("queueId", result.getQueueId());
                responseMap.put("placeInQueue", result.getPlaceInQueue());
                logger.info("User can proceed, queueId: {}", result.getQueueId());
            }
            
            responseMap.put("timestamp", Instant.now().toString());
            return ResponseEntity.ok(responseMap);
            
        } catch (Exception e) {
            logger.error("Error validating Queue-It token: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to validate queue token");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Check if a queue is active for a specific event
     */
    @GetMapping("/status/{eventId}")
    public ResponseEntity<Map<String, Object>> checkQueueStatus(@PathVariable String eventId) {
        try {
            logger.info("Checking queue status for event: {}", eventId);
            
            // Call Queue-It API to get event status
            String apiUrl = String.format("https://%s/status/integrationconfig/secure/%s", 
                queueDomain, customerId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", apiKey);
            headers.set("Host", "queue-it.net");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> apiResponse = restTemplate.exchange(
                apiUrl, HttpMethod.GET, entity, String.class);
            
            if (apiResponse.getStatusCode().is2xxSuccessful()) {
                JsonNode config = objectMapper.readTree(apiResponse.getBody());
                
                // Find the specific event configuration
                boolean isActive = false;
                int queueSize = 0;
                int estimatedWaitTime = 0;
                
                if (config.has("Events")) {
                    for (JsonNode event : config.get("Events")) {
                        if (event.has("EventId") && event.get("EventId").asText().equals(eventId)) {
                            isActive = event.has("IsActive") ? event.get("IsActive").asBoolean() : false;
                            queueSize = event.has("QueueSize") ? event.get("QueueSize").asInt() : 0;
                            estimatedWaitTime = event.has("EstimatedWaitTime") ? event.get("EstimatedWaitTime").asInt() : 0;
                            break;
                        }
                    }
                }
                
                Map<String, Object> response = new HashMap<>();
                response.put("isActive", isActive);
                response.put("queueSize", queueSize);
                response.put("estimatedWaitTime", estimatedWaitTime);
                response.put("eventId", eventId);
                response.put("timestamp", Instant.now().toString());
                
                return ResponseEntity.ok(response);
            } else {
                throw new RuntimeException("Failed to get queue status from Queue-It API");
            }
            
        } catch (Exception e) {
            logger.error("Error checking queue status for event {}: {}", eventId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to check queue status");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Enqueue a user for a specific event
     */
    @PostMapping("/enqueue")
    public ResponseEntity<Map<String, Object>> enqueueUser(
            HttpServletRequest request,
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            String eventId = (String) requestBody.get("eventId");
            String targetUrl = (String) requestBody.get("targetUrl");
            String userAgent = (String) requestBody.get("userAgent");
            String ipAddress = (String) requestBody.get("ipAddress");
            
            logger.info("Enqueuing user for event: {}, IP: {}", eventId, ipAddress);
            
            // Create Queue-It request
            IHttpRequestProvider httpRequestProvider = new HttpRequestProvider(request);
            
            // Generate redirect URL using Queue-It connector
            String redirectUrl = knownUser.resolveUserInQueue(
                targetUrl,
                eventId,
                customerId,
                secretKey,
                httpRequestProvider
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("redirectUrl", redirectUrl);
            response.put("eventId", eventId);
            response.put("timestamp", Instant.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error enqueuing user: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to enqueue user");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get queue position for a user
     */
    @GetMapping("/position/{eventId}")
    public ResponseEntity<Map<String, Object>> getQueuePosition(
            @PathVariable String eventId,
            @RequestParam String queueitToken) {
        
        try {
            logger.info("Getting queue position for event: {}, token: {}", eventId, queueitToken);
            
            // Call Queue-It API to get position
            String apiUrl = String.format("https://%s/status/queue/%s/%s", 
                queueDomain, customerId, eventId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", apiKey);
            headers.set("Host", "queue-it.net");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> apiResponse = restTemplate.exchange(
                apiUrl, HttpMethod.GET, entity, String.class);
            
            if (apiResponse.getStatusCode().is2xxSuccessful()) {
                JsonNode queueData = objectMapper.readTree(apiResponse.getBody());
                
                Map<String, Object> response = new HashMap<>();
                response.put("eventId", eventId);
                response.put("queueId", queueData.has("QueueId") ? queueData.get("QueueId").asText() : "");
                response.put("position", queueData.has("Position") ? queueData.get("Position").asInt() : 0);
                response.put("estimatedWaitTime", queueData.has("EstimatedWaitTime") ? queueData.get("EstimatedWaitTime").asInt() : 0);
                response.put("timestamp", Instant.now().toString());
                
                return ResponseEntity.ok(response);
            } else {
                throw new RuntimeException("Failed to get queue position from Queue-It API");
            }
            
        } catch (Exception e) {
            logger.error("Error getting queue position: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get queue position");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get queue statistics
     */
    @GetMapping("/stats/{eventId}")
    public ResponseEntity<Map<String, Object>> getQueueStats(@PathVariable String eventId) {
        try {
            logger.info("Getting queue stats for event: {}", eventId);
            
            // Call Queue-It API to get queue statistics
            String apiUrl = String.format("https://%s/status/queue/%s/%s", 
                queueDomain, customerId, eventId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", apiKey);
            headers.set("Host", "queue-it.net");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> apiResponse = restTemplate.exchange(
                apiUrl, HttpMethod.GET, entity, String.class);
            
            if (apiResponse.getStatusCode().is2xxSuccessful()) {
                JsonNode queueData = objectMapper.readTree(apiResponse.getBody());
                
                Map<String, Object> response = new HashMap<>();
                response.put("eventId", eventId);
                response.put("queueSize", queueData.has("QueueSize") ? queueData.get("QueueSize").asInt() : 0);
                response.put("estimatedWaitTime", queueData.has("EstimatedWaitTime") ? queueData.get("EstimatedWaitTime").asInt() : 0);
                response.put("isActive", queueData.has("IsActive") ? queueData.get("IsActive").asBoolean() : false);
                response.put("timestamp", Instant.now().toString());
                
                return ResponseEntity.ok(response);
            } else {
                throw new RuntimeException("Failed to get queue stats from Queue-It API");
            }
            
        } catch (Exception e) {
            logger.error("Error getting queue stats: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get queue stats");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Health check endpoint for Queue-it integration
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "queueit-integration");
        response.put("timestamp", Instant.now().toString());
        response.put("customerId", customerId);
        response.put("connector", "official-java-connector");
        return ResponseEntity.ok(response);
    }

    /**
     * Helper class to provide HTTP request to Queue-It connector
     */
    private static class HttpRequestProvider implements IHttpRequestProvider {
        private final HttpServletRequest request;
        
        public HttpRequestProvider(HttpServletRequest request) {
            this.request = request;
        }
        
        @Override
        public String getUserAgent() {
            return request.getHeader("User-Agent");
        }
        
        @Override
        public String getClientIP() {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        
        @Override
        public String getRequestUrl() {
            return request.getRequestURL().toString();
        }
        
        @Override
        public String getRequestUri() {
            return request.getRequestURI();
        }
        
        @Override
        public String getHeader(String name) {
            return request.getHeader(name);
        }
        
        @Override
        public String getCookieValue(String name) {
            jakarta.servlet.http.Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (jakarta.servlet.http.Cookie cookie : cookies) {
                    if (cookie.getName().equals(name)) {
                        return cookie.getValue();
                    }
                }
            }
            return null;
        }
    }

    /**
     * Helper class to provide HTTP response to Queue-It connector
     */
    private static class HttpResponseProvider implements IHttpResponseProvider {
        private final HttpServletResponse response;
        
        public HttpResponseProvider(HttpServletResponse response) {
            this.response = response;
        }
        
        @Override
        public void setCookie(String name, String value, String domain, String path, int maxAge) {
            jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(name, value);
            if (domain != null) cookie.setDomain(domain);
            if (path != null) cookie.setPath(path);
            cookie.setMaxAge(maxAge);
            response.addCookie(cookie);
        }
        
        @Override
        public void setHeader(String name, String value) {
            response.setHeader(name, value);
        }
    }
} 