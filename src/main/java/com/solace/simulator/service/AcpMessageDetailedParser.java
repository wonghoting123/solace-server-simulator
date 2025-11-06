package com.solace.simulator.service;

import com.solace.simulator.model.*;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Enhanced ACP Message Parser that shows detailed field-by-field breakdown
 * with binary representation, byte positions, and field names from interface spec
 */
@Service
public class AcpMessageDetailedParser {
    
    /**
     * Parse ACP message with detailed field breakdown showing:
     * - Binary representation for each field
     * - Field name from interface specification  
     * - Byte position range
     * - Decoded value
     */
    public AcpMessage parseWithDetails(String hexString) {
        AcpMessage acpMessage = new AcpMessage();
        
        // Clean and convert hex string to bytes
        String cleanHex = hexString.replaceAll("\\s+", "").toUpperCase();
        acpMessage.setRawHex(cleanHex);
        
        byte[] bytes = hexStringToByteArray(cleanHex);
        
        // Convert to binary string
        String fullBinary = bytesToBinaryString(bytes);
        acpMessage.setBinaryString(fullBinary);
        
        // Parse all fields with detailed information
        List<AcpMessageFieldDetail> fieldDetails = new ArrayList<>();
        
        // Header fields (bytes 0-51)
        addHeaderFieldDetails(fieldDetails, bytes, fullBinary);
        
        // Optional content (bytes 52+) - parse based on message code if needed
        if (bytes.length > 52) {
            int messageCode = readUInt16LE(bytes, 0);
            addOptionalFieldDetails(fieldDetails, bytes, fullBinary, messageCode);
        }
        
        acpMessage.setFieldDetails(fieldDetails);
        
        // Also populate the fields map for backward compatibility
        Map<String, Object> fields = new LinkedHashMap<>();
        for (AcpMessageFieldDetail detail : fieldDetails) {
            fields.put(detail.getFieldName(), detail.getValue());
        }
        acpMessage.setFields(fields);
        
        return acpMessage;
    }
    
    /**
     * Add header field details (bytes 0-51)
     */
    private void addHeaderFieldDetails(List<AcpMessageFieldDetail> fieldDetails, byte[] bytes, String fullBinary) {
        // Byte 0-1: Message Code
        addFieldDetail(fieldDetails, "Message code", 0, 1, bytes, fullBinary, 
                       readUInt16LE(bytes, 0));
        
        // Byte 2: Source System Number
        addFieldDetail(fieldDetails, "Source system number", 2, 2, bytes, fullBinary,
                       bytes[2] & 0xFF);
        
        // Byte 3: Destination System Number
        addFieldDetail(fieldDetails, "Destination system number", 3, 3, bytes, fullBinary,
                       bytes[3] & 0xFF);
        
        // Byte 4-5: Reply Code
        addFieldDetail(fieldDetails, "Reply code", 4, 5, bytes, fullBinary,
                       readUInt16LE(bytes, 4));
        
        // Byte 6-13: Last Transaction ID
        addFieldDetail(fieldDetails, "Last transaction ID", 6, 13, bytes, fullBinary,
                       readUInt64LE(bytes, 6));
        
        // Byte 14-21: Message Transaction ID
        addFieldDetail(fieldDetails, "Message transaction ID", 14, 21, bytes, fullBinary,
                       readUInt64LE(bytes, 14));
        
        // Byte 22-25: Date
        addFieldDetail(fieldDetails, "Date", 22, 25, bytes, fullBinary,
                       readUInt32LE(bytes, 22));
        
        // Byte 26-28: Time
        addFieldDetail(fieldDetails, "Time", 26, 28, bytes, fullBinary,
                       readUInt24LE(bytes, 26));
        
        // Byte 29-32: Location ID
        addFieldDetail(fieldDetails, "Location ID", 29, 32, bytes, fullBinary,
                       readUInt32LE(bytes, 29));
        
        // Byte 33-34: Position Number
        addFieldDetail(fieldDetails, "Position no", 33, 34, bytes, fullBinary,
                       readUInt16LE(bytes, 33));
        
        // Byte 35-42: Physical Terminal ID (string)
        String terminalId = readString(bytes, 35, 8);
        addFieldDetail(fieldDetails, "Physical terminal ID", 35, 42, bytes, fullBinary,
                       terminalId.isEmpty() ? "(empty)" : terminalId);
        
        // Byte 43-46: Staff ID
        addFieldDetail(fieldDetails, "Staff ID", 43, 46, bytes, fullBinary,
                       readUInt32LE(bytes, 43));
        
        // Byte 47-50: Logical Terminal ID
        addFieldDetail(fieldDetails, "Logical terminal ID", 47, 50, bytes, fullBinary,
                       readUInt32LE(bytes, 47));
        
        // Byte 51: Terminal Type
        addFieldDetail(fieldDetails, "Terminal type", 51, 51, bytes, fullBinary,
                       bytes[51] & 0xFF);
    }
    
    /**
     * Add optional content field details (bytes 52+)
     * These field names should come from the interface specification based on message code
     */
    private void addOptionalFieldDetails(List<AcpMessageFieldDetail> fieldDetails, 
                                          byte[] bytes, String fullBinary, int messageCode) {
        int offset = 52;
        
        // For now, add generic optional fields
        // TODO: Map to specific field names based on message code from interface spec
        int fieldNum = 0;
        while (offset < bytes.length) {
            int fieldSize = Math.min(4, bytes.length - offset); // Default to 4-byte fields
            
            // Determine field name based on position and message code
            String fieldName = getOptionalFieldName(fieldNum, offset, messageCode);
            
            Object value;
            int endByte = offset + fieldSize - 1;
            
            if (fieldSize == 1) {
                value = bytes[offset] & 0xFF;
            } else if (fieldSize == 2) {
                value = readUInt16LE(bytes, offset);
            } else if (fieldSize == 4) {
                value = readUInt32LE(bytes, offset);
            } else {
                value = 0;
            }
            
            addFieldDetail(fieldDetails, fieldName, offset, endByte, bytes, fullBinary, value);
            
            offset += fieldSize;
            fieldNum++;
        }
    }
    
    /**
     * Get optional field name based on position and message code
     * This should be enhanced to use actual interface specification
     */
    private String getOptionalFieldName(int fieldNum, int byteOffset, int messageCode) {
        // Generic field names - should be replaced with actual spec field names
        // based on message code
        switch (fieldNum) {
            case 0: return "Recorder track";
            case 1: return "Authority mask";
            case 2: return "Password will be expired soon";
            case 3: return "Number of days to expiry date";
            case 4: return "Remaining";
            case 5: return "Checksum";
            default: return "Optional field " + fieldNum;
        }
    }
    
    /**
     * Helper to add a field detail with binary representation
     */
    private void addFieldDetail(List<AcpMessageFieldDetail> fieldDetails, String fieldName,
                                  int startByte, int endByte, byte[] bytes, 
                                  String fullBinary, Object value) {
        // Extract binary representation for this field
        String binaryRep = extractBinaryForBytes(fullBinary, startByte, endByte);
        
        AcpMessageFieldDetail detail = new AcpMessageFieldDetail();
        detail.setFieldName(fieldName);
        detail.setBinaryRepresentation(binaryRep);
        detail.setStartByte(startByte);
        detail.setEndByte(endByte);
        detail.setValue(value);
        
        // Also set hex value
        StringBuilder hexValue = new StringBuilder();
        for (int i = startByte; i <= endByte && i < bytes.length; i++) {
            if (hexValue.length() > 0) hexValue.append(" ");
            hexValue.append(String.format("%02X", bytes[i]));
        }
        detail.setHexValue(hexValue.toString());
        
        fieldDetails.add(detail);
    }
    
    /**
     * Extract binary string for specific byte range
     */
    private String extractBinaryForBytes(String fullBinary, int startByte, int endByte) {
        int startBit = startByte * 8;
        int endBit = (endByte + 1) * 8;
        
        if (endBit > fullBinary.length()) {
            endBit = fullBinary.length();
        }
        
        String binarySegment = fullBinary.substring(startBit, endBit);
        
        // Format as space-separated bytes (8 bits each)
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < binarySegment.length(); i += 8) {
            if (i > 0) formatted.append(" ");
            int end = Math.min(i + 8, binarySegment.length());
            formatted.append(binarySegment.substring(i, end));
        }
        
        return formatted.toString();
    }
    
    // Utility methods for reading different data types
    
    private int readUInt16LE(byte[] bytes, int offset) {
        if (offset + 1 >= bytes.length) {
            return 0;
        }
        return ((bytes[offset] & 0xFF)) | ((bytes[offset + 1] & 0xFF) << 8);
    }
    
    private int readUInt24LE(byte[] bytes, int offset) {
        if (offset + 2 >= bytes.length) {
            return 0;
        }
        return ((bytes[offset] & 0xFF)) | 
               ((bytes[offset + 1] & 0xFF) << 8) | 
               ((bytes[offset + 2] & 0xFF) << 16);
    }
    
    private long readUInt32LE(byte[] bytes, int offset) {
        if (offset + 3 >= bytes.length) {
            return 0;
        }
        return ((bytes[offset] & 0xFFL)) | 
               ((bytes[offset + 1] & 0xFFL) << 8) | 
               ((bytes[offset + 2] & 0xFFL) << 16) | 
               ((bytes[offset + 3] & 0xFFL) << 24);
    }
    
    private long readUInt64LE(byte[] bytes, int offset) {
        if (offset + 7 >= bytes.length) {
            return 0;
        }
        return ((bytes[offset] & 0xFFL)) | 
               ((bytes[offset + 1] & 0xFFL) << 8) | 
               ((bytes[offset + 2] & 0xFFL) << 16) | 
               ((bytes[offset + 3] & 0xFFL) << 24) |
               ((bytes[offset + 4] & 0xFFL) << 32) |
               ((bytes[offset + 5] & 0xFFL) << 40) |
               ((bytes[offset + 6] & 0xFFL) << 48) |
               ((bytes[offset + 7] & 0xFFL) << 56);
    }
    
    private String readString(byte[] bytes, int offset, int length) {
        if (offset + length > bytes.length) {
            return "";
        }
        
        // Find the first null byte or use full length
        int actualLength = length;
        for (int i = 0; i < length; i++) {
            if (bytes[offset + i] == 0) {
                actualLength = i;
                break;
            }
        }
        
        if (actualLength == 0) {
            return "";
        }
        
        return new String(bytes, offset, actualLength, StandardCharsets.US_ASCII).trim();
    }
    
    private byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have an even number of characters");
        }
        
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int highNibble = Character.digit(hexString.charAt(i), 16);
            int lowNibble = Character.digit(hexString.charAt(i + 1), 16);
            
            if (highNibble == -1 || lowNibble == -1) {
                throw new IllegalArgumentException("Invalid hexadecimal character at position " + i);
            }
            
            data[i / 2] = (byte) ((highNibble << 4) + lowNibble);
        }
        return data;
    }
    
    private String bytesToBinaryString(byte[] bytes) {
        StringBuilder binary = new StringBuilder();
        for (byte b : bytes) {
            binary.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        return binary.toString();
    }
}
