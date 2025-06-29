package com.dharshi.apigateway.controllers;

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

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.time.Instant;

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

    /**
     * Check if a queue is active for a specific event
     */
    @GetMapping("/status/{eventId}")
    public ResponseEntity<Map<String, Object>> checkQueueStatus(@PathVariable String eventId) {
        try {
            logger.info("Checking queue status for event: {}", eventId);
            
            // For demo purposes, we'll simulate queue status
            // In production, this would call Queue-it's API
            Map<String, Object> response = new HashMap<>();
            
            // Simulate different queue states based on event
            if (eventId.contains("flash-sale") || eventId.contains("black-friday")) {
                response.put("isActive", true);
                response.put("queueSize", 1500);
                response.put("estimatedWaitTime", 15);
            } else if (eventId.contains("high-traffic")) {
                response.put("isActive", Math.random() > 0.5); // Random activation
                response.put("queueSize", 800);
                response.put("estimatedWaitTime", 8);
            } else {
                response.put("isActive", false);
                response.put("queueSize", 0);
                response.put("estimatedWaitTime", 0);
            }
            
            response.put("eventId", eventId);
            response.put("timestamp", Instant.now().toString());
            
            return ResponseEntity.ok(response);
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
    public ResponseEntity<Map<String, Object>> enqueueUser(@RequestBody Map<String, Object> request) {
        try {
            String eventId = (String) request.get("eventId");
            String targetUrl = (String) request.get("targetUrl");
            String userAgent = (String) request.get("userAgent");
            String ipAddress = (String) request.get("ipAddress");
            
            logger.info("Enqueuing user for event: {}, IP: {}", eventId, ipAddress);
            
            // Generate a unique queue token
            String queueToken = UUID.randomUUID().toString();
            
            // For demo purposes, we'll simulate queue behavior
            // In production, this would integrate with Queue-it's API
            Map<String, Object> response = new HashMap<>();
            
            // Simulate queue behavior based on event type
            if (eventId.contains("flash-sale") || eventId.contains("black-friday")) {
                // High traffic events - redirect to Queue-it
                String redirectUrl = String.format("https://%s/%s?c=%s&e=%s&t=%s",
                    queueDomain, customerId, customerId, eventId, queueToken);
                response.put("redirectUrl", redirectUrl);
            } else {
                // Lower traffic events - provide queue token for polling
                response.put("queueToken", queueToken);
                response.put("position", 150);
                response.put("estimatedWaitTime", 5);
            }
            
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
     * Check user's position in queue
     */
    @GetMapping("/position/{eventId}")
    public ResponseEntity<Map<String, Object>> checkQueuePosition(
            @PathVariable String eventId,
            @RequestHeader("Authorization") String authorization) {
        try {
            String queueToken = authorization.replace("Bearer ", "");
            logger.info("Checking queue position for event: {}, token: {}", eventId, queueToken);
            
            // For demo purposes, we'll simulate position updates
            // In production, this would call Queue-it's API
            Map<String, Object> response = new HashMap<>();
            
            // Simulate position progression
            int currentPosition = getSimulatedPosition(queueToken, eventId);
            
            if (currentPosition <= 0) {
                // User can enter the site
                response.put("redirectUrl", "/");
                response.put("status", "entered");
            } else {
                // User is still in queue
                response.put("position", currentPosition);
                response.put("estimatedWaitTime", Math.max(1, currentPosition / 10));
                response.put("status", "queued");
            }
            
            response.put("eventId", eventId);
            response.put("timestamp", Instant.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error checking queue position: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to check queue position");
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
            
            Map<String, Object> response = new HashMap<>();
            
            // Simulate queue statistics
            if (eventId.contains("flash-sale")) {
                response.put("totalUsers", 2500);
                response.put("activeUsers", 1800);
                response.put("averageWaitTime", 12);
                response.put("queueThroughput", 120);
            } else if (eventId.contains("black-friday")) {
                response.put("totalUsers", 5000);
                response.put("activeUsers", 3200);
                response.put("averageWaitTime", 18);
                response.put("queueThroughput", 200);
            } else {
                response.put("totalUsers", 0);
                response.put("activeUsers", 0);
                response.put("averageWaitTime", 0);
                response.put("queueThroughput", 0);
            }
            
            response.put("eventId", eventId);
            response.put("timestamp", Instant.now().toString());
            
            return ResponseEntity.ok(response);
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
        return ResponseEntity.ok(response);
    }

    /**
     * Simulate position progression for demo purposes
     */
    private int getSimulatedPosition(String queueToken, String eventId) {
        // Use token hash to simulate consistent position progression
        int hash = queueToken.hashCode();
        int basePosition = Math.abs(hash % 200) + 50;
        
        // Simulate time-based progression
        long currentTime = System.currentTimeMillis();
        long tokenTime = Math.abs(hash) % 1000000; // Simulate token creation time
        long timeDiff = (currentTime - tokenTime) / 1000; // Seconds since "token creation"
        
        // Reduce position over time (simulate queue progression)
        int timeReduction = (int) (timeDiff / 2); // Reduce position every 2 seconds
        int finalPosition = Math.max(0, basePosition - timeReduction);
        
        logger.debug("Simulated position for token {}: {} -> {}", queueToken, basePosition, finalPosition);
        
        return finalPosition;
    }
} 