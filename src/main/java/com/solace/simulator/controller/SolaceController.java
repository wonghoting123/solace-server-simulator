package com.solace.simulator.controller;

import com.solace.simulator.model.ConnectionConfig;
import com.solace.simulator.model.MessageRequest;
import com.solace.simulator.model.SubscriptionRequest;
import com.solace.simulator.service.SolaceJmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SolaceController {

    @Autowired
    private SolaceJmsService solaceJmsService;

    @PostMapping("/connect")
    public ResponseEntity<Map<String, String>> connect(@RequestBody ConnectionConfig config) {
        try {
            solaceJmsService.connect(config);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Connected to Solace broker");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/disconnect")
    public ResponseEntity<Map<String, String>> disconnect() {
        try {
            solaceJmsService.disconnect();
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Disconnected from Solace broker");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("connected", solaceJmsService.isConnected());
        if (solaceJmsService.isConnected()) {
            ConnectionConfig config = solaceJmsService.getCurrentConfig();
            response.put("host", config.getHost());
            response.put("vpnName", config.getVpnName());
            response.put("username", config.getUsername());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendMessage(@RequestBody MessageRequest request) {
        try {
            solaceJmsService.sendMessage(request);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Message sent successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, String>> subscribe(@RequestBody SubscriptionRequest request) {
        try {
            solaceJmsService.subscribe(request.getDestination(), request.getDestinationType());
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Subscribed to " + request.getDestination());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<Map<String, String>> unsubscribe(@RequestBody SubscriptionRequest request) {
        try {
            solaceJmsService.unsubscribe(request.getDestination(), request.getDestinationType());
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Unsubscribed from " + request.getDestination());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
