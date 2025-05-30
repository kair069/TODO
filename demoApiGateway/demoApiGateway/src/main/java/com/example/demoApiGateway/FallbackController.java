package com.example.demoApiGateway;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@RestController
public class FallbackController {

    @RequestMapping("/fallback")
    public ResponseEntity<Map<String, Object>> fallback() {
        return ResponseEntity.status(503).body(
                Map.of(
                        "error", "Service temporarily unavailable",
                        "message", "Circuit breaker is open. Try again later.",
                        "timestamp", System.currentTimeMillis()
                )
        );
    }
}