package com.dharshi.apigateway.controllers;

import com.queue_it.connector.*;
import com.queue_it.connector.models.*;
import com.queue_it.connector.integrationconfig.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/queueit")
@CrossOrigin(origins = "*")
public class QueueItController {
    private static final Logger logger = LoggerFactory.getLogger(QueueItController.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${queueit.customer-id:futuraforge}")
    private String customerId;
    @Value("${queueit.secret-key:your-secret-key}")
    private String secretKey;
    @Value("${queueit.api-key:your-api-key}")
    private String apiKey;
    @Value("${queueit.queue-domain:futuraforge.queue-it.net}")
    private String queueDomain;

    // 1. Validate a user's queue token
    @PostMapping("/validate")
    public ResponseEntity<?> validateQueueToken(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> body) {
        try {
            String eventId = (String) body.get("eventId");
            String queueitToken = (String) body.get("queueitToken");
            String originalUrl = (String) body.get("originalUrl");

            HttpRequest httpRequest = new HttpRequest(request);
            ConnectorSettings settings = new ConnectorSettings(customerId, secretKey, apiKey);
            ConnectorContextProvider contextProvider = new ConnectorContextProvider(settings, httpRequest, response, null);
            QueueEventConfig eventConfig = new QueueEventConfig();
            eventConfig.setEventId(eventId);
            eventConfig.setQueueDomain(queueDomain);
            eventConfig.setCookieDomain(queueDomain);
            eventConfig.setLayoutName("Queue");
            eventConfig.setCulture("en-US");

            RequestValidationResult result = KnownUser.resolveQueueRequestByLocalConfig(
                originalUrl, queueitToken, eventConfig, customerId, secretKey, contextProvider
            );

            Map<String, Object> resp = new HashMap<>();
            resp.put("redirect", result.doRedirect());
            resp.put("redirectUrl", result.getRedirectUrl());
            resp.put("queueId", result.getQueueId());
            resp.put("eventId", result.getEventId());
            resp.put("actionType", result.getActionType());
            resp.put("timestamp", Instant.now().toString());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            logger.error("Error in validateQueueToken: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // 2. Place a user in the queue (simulate, as SDK does not provide direct enqueue)
    @PostMapping("/queue")
    public ResponseEntity<?> queueUser(@RequestBody Map<String, Object> body) {
        // This endpoint is a placeholder, as the SDK does not provide a direct enqueue method
        // Typically, the frontend should redirect to the queue using the URL from /validate
        return ResponseEntity.ok(Map.of("message", "Queue placement is handled via /validate and redirect URLs."));
    }

    // 3. Cancel a queue session
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelQueue(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> body) {
        try {
            String eventId = (String) body.get("eventId");
            String queueitToken = (String) body.get("queueitToken");
            // You may need to build a CancelEventConfig here
            CancelEventConfig cancelConfig = new CancelEventConfig();
            cancelConfig.setEventId(eventId);
            ConnectorSettings settings = new ConnectorSettings(customerId, secretKey, apiKey);
            ConnectorContextProvider contextProvider = new ConnectorContextProvider(settings, new HttpRequest(request), response, null);
            RequestValidationResult result = KnownUser.cancelRequestByLocalConfig(
                request.getRequestURL().toString(), queueitToken, cancelConfig, customerId, secretKey, contextProvider
            );
            Map<String, Object> resp = new HashMap<>();
            resp.put("redirect", result.doRedirect());
            resp.put("redirectUrl", result.getRedirectUrl());
            resp.put("eventId", result.getEventId());
            resp.put("actionType", result.getActionType());
            resp.put("timestamp", Instant.now().toString());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            logger.error("Error in cancelQueue: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // 4. Extend queue cookie
    @PostMapping("/extend-cookie")
    public ResponseEntity<?> extendQueueCookie(HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Object> body) {
        try {
            String eventId = (String) body.get("eventId");
            String queueId = (String) body.get("queueId");
            int cookieValidityMinutes = body.get("cookieValidityMinutes") != null ? (int) body.get("cookieValidityMinutes") : 30;
            String cookieDomain = (String) body.getOrDefault("cookieDomain", queueDomain);
            boolean isCookieHttpOnly = body.get("isCookieHttpOnly") != null && (boolean) body.get("isCookieHttpOnly");
            boolean isCookieSecure = body.get("isCookieSecure") != null && (boolean) body.get("isCookieSecure");
            KnownUser.extendQueueCookie(
                eventId, cookieValidityMinutes, cookieDomain, isCookieHttpOnly, isCookieSecure, request, response, queueId, null
            );
            return ResponseEntity.ok(Map.of("message", "Queue cookie extended."));
        } catch (Exception e) {
            logger.error("Error in extendQueueCookie: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // 5. Get queue/event status (integration config)
    @GetMapping("/status")
    public ResponseEntity<?> getQueueStatus() {
        // The SDK does not provide a direct status API; this is typically handled by your own event/config logic
        // You may want to return a static or dynamic status here
        return ResponseEntity.ok(Map.of("status", "QueueIt integration active", "timestamp", Instant.now().toString()));
    }

    // 6. Health check
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "queueit-integration",
            "timestamp", Instant.now().toString(),
            "customerId", customerId,
            "connector", "official-java-connector"
        ));
    }

    // --- Integration Test Endpoints (for test environments only) ---
    @PostMapping("/simulate-event")
    public ResponseEntity<?> simulateEvent(@RequestBody Map<String, Object> body) {
        // Stub: Implement event simulation logic for integration testing
        return ResponseEntity.status(501).body(Map.of("error", "Not implemented: simulateEvent"));
    }

    @GetMapping("/session-info")
    public ResponseEntity<?> getSessionInfo(@RequestParam(required = false) String queueId) {
        // Stub: Implement session info inspection for integration testing
        return ResponseEntity.status(501).body(Map.of("error", "Not implemented: getSessionInfo"));
    }

    @PostMapping("/reset-test-state")
    public ResponseEntity<?> resetTestState() {
        // Stub: Implement test state reset for integration testing
        return ResponseEntity.status(501).body(Map.of("error", "Not implemented: resetTestState"));
    }
} 