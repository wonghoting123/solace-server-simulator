package com.solace.simulator.service;

import com.solace.simulator.model.*;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ACP Message Parser based on acp_message.htm specifications
 * Parses messages using exact field specifications from the HTML file
 * Returns data in table format with: Data, Byte Position, Data Type, Size, Msg Data, Msg Data Value
 */
@Service
public class AcpMessageSpecParser {
    
    /**
     * Parse ACP message according to specifications from acp_message.htm
     * Returns table format with all field details
     */
    public AcpMessage parseWithSpec(String hexString) {
        AcpMessage acpMessage = new AcpMessage();
        
        // Clean and convert hex string to bytes
        String cleanHex = hexString.replaceAll("\\s+", "").toUpperCase();
        acpMessage.setRawHex(cleanHex);
        
        byte[] bytes = hexStringToByteArray(cleanHex);
        
        // Convert to binary string
        String fullBinary = bytesToBinaryString(bytes);
        acpMessage.setBinaryString(fullBinary);
        
        // Parse all fields according to specification
        List<AcpParsedField> parsedFields = new ArrayList<>();
        
        // Header fields (based on acp_message.htm Message Format table)
        addHeaderFields(parsedFields, bytes, fullBinary);
        
        // Optional content fields (bytes 52+) - message specific
        if (bytes.length > 52) {
            int messageCode = readUInt16LE(bytes, 0);
            addOptionalFields(parsedFields, bytes, fullBinary, messageCode);
        }
        
        acpMessage.setParsedFields(parsedFields);
        
        // Also populate the fields map for backward compatibility
        Map<String, Object> fields = new LinkedHashMap<>();
        for (AcpParsedField field : parsedFields) {
            fields.put(field.getData(), field.getMsgDataValue());
        }
        acpMessage.setFields(fields);
        
        return acpMessage;
    }
    
    /**
     * Add header fields according to acp_message.htm specification
     * Table columns: Message Format | Byte Position | Data Type | Size(bytes) | Data
     */
    private void addHeaderFields(List<AcpParsedField> fields, byte[] bytes, String fullBinary) {
        // Byte 0-1: Message code (Unsigned Integer, 2 bytes)
        addField(fields, "Message code", "0-1", "Unsigned Integer", "2", 
                 bytes, fullBinary, 0, 1, readUInt16LE(bytes, 0));
        
        // Byte 2: Source system number (Integer, 1 byte)
        addField(fields, "Source system number", "2", "Integer", "1",
                 bytes, fullBinary, 2, 2, bytes[2] & 0xFF);
        
        // Byte 3: Destination system number (Integer, 1 byte)
        addField(fields, "Destination system number", "3", "Integer", "1",
                 bytes, fullBinary, 3, 3, bytes[3] & 0xFF);
        
        // Byte 4-5: Reply code (Unsigned Integer, 2 bytes)
        addField(fields, "Reply code", "4-5", "Unsigned Integer", "2",
                 bytes, fullBinary, 4, 5, readUInt16LE(bytes, 4));
        
        // Byte 6-13: Last transaction ID (Unsigned Integer, 8 bytes)
        addField(fields, "Last transaction ID", "6-13", "Unsigned Integer", "8",
                 bytes, fullBinary, 6, 13, readUInt64LE(bytes, 6));
        
        // Byte 14-21: Message transaction ID (Unsigned Integer, 8 bytes)
        addField(fields, "Message transaction ID", "14-21", "Unsigned Integer", "8",
                 bytes, fullBinary, 14, 21, readUInt64LE(bytes, 14));
        
        // Byte 22-25: Date (Unsigned Integer, 4 bytes)
        addField(fields, "Date", "22-25", "Unsigned Integer", "4",
                 bytes, fullBinary, 22, 25, readUInt32LE(bytes, 22));
        
        // Byte 26-28: Time (Unsigned Integer, 3 bytes)
        addField(fields, "Time", "26-28", "Unsigned Integer", "3",
                 bytes, fullBinary, 26, 28, readUInt24LE(bytes, 26));
        
        // Byte 29-32: Location ID (Unsigned Integer, 4 bytes)
        addField(fields, "Location ID", "29-32", "Unsigned Integer", "4",
                 bytes, fullBinary, 29, 32, readUInt32LE(bytes, 29));
        
        // Byte 33-34: Position no (Unsigned Integer, 2 bytes)
        addField(fields, "Position no", "33-34", "Unsigned Integer", "2",
                 bytes, fullBinary, 33, 34, readUInt16LE(bytes, 33));
        
        // Byte 35-42: Physical terminal ID (String, 8 bytes)
        String terminalId = readString(bytes, 35, 8);
        addField(fields, "Physical terminal ID", "35-42", "String", "8",
                 bytes, fullBinary, 35, 42, terminalId.isEmpty() ? "(empty)" : terminalId);
        
        // Byte 43-46: Staff ID (Unsigned Integer, 4 bytes)
        addField(fields, "Staff ID", "43-46", "Unsigned Integer", "4",
                 bytes, fullBinary, 43, 46, readUInt32LE(bytes, 43));
        
        // Byte 47-50: Logical terminal ID (Unsigned Integer, 4 bytes)
        addField(fields, "Logical terminal ID", "47-50", "Unsigned Integer", "4",
                 bytes, fullBinary, 47, 50, readUInt32LE(bytes, 47));
        
        // Byte 51: Terminal type (Unsigned Integer, 1 byte)
        addField(fields, "Terminal type", "51", "Unsigned Integer", "1",
                 bytes, fullBinary, 51, 51, bytes[51] & 0xFF);
    }
    
    /**
     * Add optional content fields based on message code
     * TODO: These should be mapped from acp_message.htm for each message code
     */
    private void addOptionalFields(List<AcpParsedField> fields, byte[] bytes, 
                                     String fullBinary, int messageCode) {
        int offset = 52;
        
        // Generic optional fields - should be replaced with actual spec per message code
        // For now, parse remaining bytes as 4-byte unsigned integers
        int fieldNum = 0;
        while (offset + 3 < bytes.length) {
            String fieldName = getOptionalFieldName(fieldNum, messageCode);
            addField(fields, fieldName, String.format("%d-%d", offset, offset + 3), 
                     "Unsigned Integer", "4",
                     bytes, fullBinary, offset, offset + 3, readUInt32LE(bytes, offset));
            offset += 4;
            fieldNum++;
        }
        
        // Handle remaining bytes
        while (offset < bytes.length) {
            String fieldName = "Optional byte " + offset;
            addField(fields, fieldName, String.valueOf(offset), "Unsigned Integer", "1",
                     bytes, fullBinary, offset, offset, bytes[offset] & 0xFF);
            offset++;
        }
    }
    
    /**
     * Get optional field name based on message code
     * TODO: Load from acp_message.htm specifications
     */
    private String getOptionalFieldName(int fieldNum, int messageCode) {
        // These are generic names - should be loaded from HTML spec per message code
        switch (fieldNum) {
            case 0: return "Recorder track";
            case 1: return "Authority mask";
            case 2: return "Password expiry flag";
            case 3: return "Days to expiry";
            default: return "Optional field " + fieldNum;
        }
    }
    
    /**
     * Helper to add a field with all specification details
     */
    private void addField(List<AcpParsedField> fields, String fieldName, String bytePosition,
                          String dataType, String size, byte[] bytes, String fullBinary,
                          int startByte, int endByte, Object value) {
        // Extract binary representation
        String msgData = extractBinaryForBytes(fullBinary, startByte, endByte);
        
        // Format value
        String msgDataValue = String.valueOf(value);
        
        AcpParsedField field = new AcpParsedField(
            fieldName,      // Data
            bytePosition,   // Byte Position
            dataType,       // Data Type
            size,           // Size
            msgData,        // Msg Data (binary)
            msgDataValue    // Msg Data Value
        );
        
        fields.add(field);
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
        if (offset + 1 >= bytes.length) return 0;
        return ((bytes[offset] & 0xFF)) | ((bytes[offset + 1] & 0xFF) << 8);
    }
    
    private int readUInt24LE(byte[] bytes, int offset) {
        if (offset + 2 >= bytes.length) return 0;
        return ((bytes[offset] & 0xFF)) | 
               ((bytes[offset + 1] & 0xFF) << 8) | 
               ((bytes[offset + 2] & 0xFF) << 16);
    }
    
    private long readUInt32LE(byte[] bytes, int offset) {
        if (offset + 3 >= bytes.length) return 0;
        return ((bytes[offset] & 0xFFL)) | 
               ((bytes[offset + 1] & 0xFFL) << 8) | 
               ((bytes[offset + 2] & 0xFFL) << 16) | 
               ((bytes[offset + 3] & 0xFFL) << 24);
    }
    
    private long readUInt64LE(byte[] bytes, int offset) {
        if (offset + 7 >= bytes.length) return 0;
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
        if (offset + length > bytes.length) return "";
        
        int actualLength = length;
        for (int i = 0; i < length; i++) {
            if (bytes[offset + i] == 0) {
                actualLength = i;
                break;
            }
        }
        
        if (actualLength == 0) return "";
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
