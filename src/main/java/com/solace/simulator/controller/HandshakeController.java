package com.solace.simulator.controller;

import com.solace.simulator.model.HandshakeRequest;
import com.solace.simulator.model.HandshakeSimulation;
import com.solace.simulator.model.SourceSystem;
import com.solace.simulator.service.HandshakeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controller for handshake simulation operations
 */
@RestController
@RequestMapping("/api/handshake")
public class HandshakeController {

    @Autowired
    private HandshakeService handshakeService;

    /**
     * Start a handshake simulation
     */
    @PostMapping("/start")
    public ResponseEntity<?> startSimulation(@RequestBody HandshakeRequest request) {
        try {
            HandshakeSimulation simulation = handshakeService.startSimulation(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Handshake simulation started");
            response.put("simulation", simulation);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("Failed to start simulation: " + e.getMessage()));
        }
    }

    /**
     * Stop a handshake simulation
     */
    @PostMapping("/stop")
    public ResponseEntity<?> stopSimulation(@RequestBody Map<String, String> request) {
        try {
            String sourceSystem = request.get("sourceSystem");
            if (sourceSystem == null || sourceSystem.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("sourceSystem is required"));
            }
            
            handshakeService.stopSimulation(sourceSystem);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Handshake simulation stopped");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("Failed to stop simulation: " + e.getMessage()));
        }
    }

    /**
     * Get list of active simulations
     */
    @GetMapping("/list")
    public ResponseEntity<?> listSimulations() {
        try {
            List<HandshakeSimulation> simulations = handshakeService.getActiveSimulations();
            return ResponseEntity.ok(simulations);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("Failed to list simulations: " + e.getMessage()));
        }
    }

    /**
     * Get available source systems
     */
    @GetMapping("/source-systems")
    public ResponseEntity<?> getSourceSystems() {
        try {
            List<Map<String, Object>> systems = new ArrayList<>();
            for (SourceSystem system : SourceSystem.values()) {
                Map<String, Object> systemInfo = new HashMap<>();
                systemInfo.put("name", system.name());
                systemInfo.put("displayName", system.getDisplayName());
                systemInfo.put("systemNumber", system.getSystemNumber());
                systemInfo.put("destinationSystemNumber", system.getDestinationSystemNumber());
                systemInfo.put("incomingQueue", system.getIncomingQueue());
                systemInfo.put("outgoingTopic", system.getOutgoingTopic());
                systems.add(systemInfo);
            }
            return ResponseEntity.ok(systems);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("Failed to get source systems: " + e.getMessage()));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", message);
        return error;
    }
}
