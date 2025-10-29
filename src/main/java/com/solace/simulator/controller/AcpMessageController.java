package com.solace.simulator.controller;

import com.solace.simulator.model.AcpMessage;
import com.solace.simulator.model.AcpMessageRequest;
import com.solace.simulator.service.AcpMessageParser;
import com.solace.simulator.service.AcpMessageHeaderParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for ACP message parsing operations
 */
@RestController
@RequestMapping("/api/acp")
public class AcpMessageController {
    
    @Autowired
    private AcpMessageParser acpMessageParser;
    
    @Autowired
    private AcpMessageHeaderParser acpMessageHeaderParser;
    
    /**
     * Parse a hexadecimal string into an ACP message with mapped fields
     * @param request Contains the hexadecimal string to parse
     * @return Parsed ACP message with binary representation and field mappings
     */
    @PostMapping("/parse")
    public ResponseEntity<?> parseMessage(@RequestBody AcpMessageRequest request) {
        try {
            if (request.getHexString() == null || request.getHexString().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Hex string is required"));
            }
            
            AcpMessage acpMessage = acpMessageParser.parseHexToAcpMessage(request.getHexString());
            return ResponseEntity.ok(acpMessage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("Failed to parse message: " + e.getMessage()));
        }
    }
    
    /**
     * Parse a complete ACP message with header and message code-based field mapping
     * This endpoint maps fields to their corresponding positions based on the message code
     * 
     * @param request Contains the hexadecimal string to parse
     * @return Parsed ACP message with header fields and optional content mapped by message code
     */
    @PostMapping("/parse-complete")
    public ResponseEntity<?> parseCompleteMessage(@RequestBody AcpMessageRequest request) {
        try {
            if (request.getHexString() == null || request.getHexString().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Hex string is required"));
            }
            
            AcpMessage acpMessage = acpMessageHeaderParser.parseCompleteMessage(request.getHexString());
            return ResponseEntity.ok(acpMessage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("Failed to parse message: " + e.getMessage()));
        }
    }
    
    /**
     * Get information about ACP message structure and parsing rules
     * @return Information about the ACP message format
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("description", "ACP Message Parser - Converts hexadecimal byte strings to parsed message fields");
        info.put("byteOrder", "Little-Endian");
        info.put("inputFormat", "Hexadecimal string (with or without spaces)");
        info.put("example", "F30A291408000101...");
        info.put("exampleParsing", "F30A (hex) = 0x0AF3 = 2803 (decimal, little-endian)");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("POST /api/acp/parse", "Basic parser - parses hex as 16-bit fields");
        endpoints.put("POST /api/acp/parse-complete", "Complete parser - parses header + message code-based field mapping");
        endpoints.put("GET /api/acp/info", "Get API information");
        info.put("endpoints", endpoints);
        
        Map<String, String> fieldTypes = new HashMap<>();
        fieldTypes.put("Field_X_Y", "16-bit unsigned integer (2 bytes, little-endian)");
        fieldTypes.put("Field_X_Y_Hex", "Hexadecimal representation of the field");
        info.put("fieldTypes", fieldTypes);
        
        return ResponseEntity.ok(info);
    }
    
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", message);
        return error;
    }
}
