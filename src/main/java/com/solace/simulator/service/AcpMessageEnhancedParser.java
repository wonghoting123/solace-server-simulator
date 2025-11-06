package com.solace.simulator.service;

import com.solace.simulator.model.*;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Enhanced ACP Message Parser with support for:
 * - BCS-specific header format (56 bytes with packet fields)
 * - Standard ACP header format (52 bytes)
 * - Message-specific body parsing based on acp_message.htm specifications
 */
@Service
public class AcpMessageEnhancedParser {
    
    // Source system numbers for BCS
    private static final int BCS_SOURCE_SYSTEM_22 = 22;
    private static final int BCS_SOURCE_SYSTEM_31 = 31;
    
    /**
     * Parse ACP message with enhanced header and body parsing
     */
    public AcpMessage parseEnhanced(String hexString) {
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
        
        // Read message code and source system to determine header format
        int messageCode = readUInt16LE(bytes, 0);
        int sourceSystem = bytes[2] & 0xFF;
        
        // Determine if this is a BCS message (56-byte header) or standard (52-byte header)
        boolean isBcsMessage = (sourceSystem == BCS_SOURCE_SYSTEM_22 || sourceSystem == BCS_SOURCE_SYSTEM_31);
        int headerSize = isBcsMessage ? 56 : 52;
        
        // Parse header fields
        if (isBcsMessage) {
            addBcsHeaderFields(parsedFields, bytes, fullBinary);
        } else {
            addStandardHeaderFields(parsedFields, bytes, fullBinary);
        }
        
        // Parse body fields (message-specific)
        if (bytes.length > headerSize) {
            addBodyFields(parsedFields, bytes, fullBinary, messageCode, sourceSystem, headerSize);
        }
        
        // Add checksum if present (last byte)
        if (bytes.length > headerSize) {
            int checksumByte = bytes.length - 1;
            addField(parsedFields, "Checksum", String.valueOf(checksumByte), "Unsigned Integer", "1",
                     bytes, fullBinary, checksumByte, checksumByte, bytes[checksumByte] & 0xFF);
        }
        
        acpMessage.setParsedFields(parsedFields);
        
        // Also populate the fields map for backward compatibility
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("Message_Code", messageCode);
        fields.put("Source_System", sourceSystem);
        fields.put("Header_Type", isBcsMessage ? "BCS (56 bytes)" : "Standard (52 bytes)");
        for (AcpParsedField field : parsedFields) {
            fields.put(field.getData().replaceAll("\\s+", "_"), field.getMsgDataValue());
        }
        acpMessage.setFields(fields);
        
        return acpMessage;
    }
    
    /**
     * Add standard ACP header fields (52 bytes)
     */
    private void addStandardHeaderFields(List<AcpParsedField> fields, byte[] bytes, String fullBinary) {
        // Byte 0-1: Message code
        addField(fields, "Message code", "0-1", "Unsigned Integer", "2", 
                 bytes, fullBinary, 0, 1, readUInt16LE(bytes, 0));
        
        // Byte 2: Source system number
        addField(fields, "Source system number", "2", "Integer", "1",
                 bytes, fullBinary, 2, 2, bytes[2] & 0xFF);
        
        // Byte 3: Destination system number
        addField(fields, "Destination system number", "3", "Integer", "1",
                 bytes, fullBinary, 3, 3, bytes[3] & 0xFF);
        
        // Byte 4-5: Reply code
        addField(fields, "Reply code", "4-5", "Unsigned Integer", "2",
                 bytes, fullBinary, 4, 5, readUInt16LE(bytes, 4));
        
        // Byte 6-13: Last transaction ID
        addField(fields, "Last transaction ID", "6-13", "Unsigned Integer", "8",
                 bytes, fullBinary, 6, 13, readUInt64LE(bytes, 6));
        
        // Byte 14-21: Message transaction ID
        addField(fields, "Message transaction ID", "14-21", "Unsigned Integer", "8",
                 bytes, fullBinary, 14, 21, readUInt64LE(bytes, 14));
        
        // Byte 22-25: Date
        addField(fields, "Date", "22-25", "Unsigned Integer", "4",
                 bytes, fullBinary, 22, 25, readUInt32LE(bytes, 22));
        
        // Byte 26-28: Time
        addField(fields, "Time", "26-28", "Unsigned Integer", "3",
                 bytes, fullBinary, 26, 28, readUInt24LE(bytes, 26));
        
        // Byte 29-32: Location ID
        addField(fields, "Location ID", "29-32", "Unsigned Integer", "4",
                 bytes, fullBinary, 29, 32, readUInt32LE(bytes, 29));
        
        // Byte 33-34: Position no
        addField(fields, "Position no", "33-34", "Unsigned Integer", "2",
                 bytes, fullBinary, 33, 34, readUInt16LE(bytes, 33));
        
        // Byte 35-42: Physical terminal ID
        String terminalId = readString(bytes, 35, 8);
        addField(fields, "Physical terminal ID", "35-42", "String", "8",
                 bytes, fullBinary, 35, 42, terminalId.isEmpty() ? "(empty)" : terminalId);
        
        // Byte 43-46: Staff ID
        addField(fields, "Staff ID", "43-46", "Unsigned Integer", "4",
                 bytes, fullBinary, 43, 46, readUInt32LE(bytes, 43));
        
        // Byte 47-50: Logical terminal ID
        addField(fields, "Logical terminal ID", "47-50", "Unsigned Integer", "4",
                 bytes, fullBinary, 47, 50, readUInt32LE(bytes, 47));
        
        // Byte 51: Terminal type
        addField(fields, "Terminal type", "51", "Unsigned Integer", "1",
                 bytes, fullBinary, 51, 51, bytes[51] & 0xFF);
    }
    
    /**
     * Add BCS-specific header fields (56 bytes)
     * Includes 3 additional packet-related fields before Reply code
     */
    private void addBcsHeaderFields(List<AcpParsedField> fields, byte[] bytes, String fullBinary) {
        // Byte 0-1: Message code
        addField(fields, "Message code", "0-1", "Unsigned Integer", "2", 
                 bytes, fullBinary, 0, 1, readUInt16LE(bytes, 0));
        
        // Byte 2: Source system number
        addField(fields, "Source system number", "2", "Integer", "1",
                 bytes, fullBinary, 2, 2, bytes[2] & 0xFF);
        
        // Byte 3: Destination system number
        addField(fields, "Destination system number", "3", "Integer", "1",
                 bytes, fullBinary, 3, 3, bytes[3] & 0xFF);
        
        // *** BCS-specific fields ***
        // Byte 4-5: Packet group ID
        addField(fields, "Packet group ID", "4-5", "Unsigned Integer", "2",
                 bytes, fullBinary, 4, 5, readUInt16LE(bytes, 4));
        
        // Byte 6: Packet sequence
        addField(fields, "Packet sequence", "6", "Unsigned Integer", "1",
                 bytes, fullBinary, 6, 6, bytes[6] & 0xFF);
        
        // Byte 7: Packet total
        addField(fields, "Packet total", "7", "Unsigned Integer", "1",
                 bytes, fullBinary, 7, 7, bytes[7] & 0xFF);
        
        // Byte 8-9: Reply code
        addField(fields, "Reply code", "8-9", "Unsigned Integer", "2",
                 bytes, fullBinary, 8, 9, readUInt16LE(bytes, 8));
        
        // Byte 10-17: Last transaction ID
        addField(fields, "Last transaction ID", "10-17", "Unsigned Integer", "8",
                 bytes, fullBinary, 10, 17, readUInt64LE(bytes, 10));
        
        // Byte 18-25: Message transaction ID
        addField(fields, "Message transaction ID", "18-25", "Unsigned Integer", "8",
                 bytes, fullBinary, 18, 25, readUInt64LE(bytes, 18));
        
        // Byte 26-29: Date
        addField(fields, "Date", "26-29", "Unsigned Integer", "4",
                 bytes, fullBinary, 26, 29, readUInt32LE(bytes, 26));
        
        // Byte 30-32: Time
        addField(fields, "Time", "30-32", "Unsigned Integer", "3",
                 bytes, fullBinary, 30, 32, readUInt24LE(bytes, 30));
        
        // Byte 33-36: Location ID
        addField(fields, "Location ID", "33-36", "Unsigned Integer", "4",
                 bytes, fullBinary, 33, 36, readUInt32LE(bytes, 33));
        
        // Byte 37-38: Position no
        addField(fields, "Position no", "37-38", "Unsigned Integer", "2",
                 bytes, fullBinary, 37, 38, readUInt16LE(bytes, 37));
        
        // Byte 39-46: Physical terminal ID
        String terminalId = readString(bytes, 39, 8);
        addField(fields, "Physical terminal ID", "39-46", "String", "8",
                 bytes, fullBinary, 39, 46, terminalId.isEmpty() ? "(empty)" : terminalId);
        
        // Byte 47-50: Staff ID
        addField(fields, "Staff ID", "47-50", "Unsigned Integer", "4",
                 bytes, fullBinary, 47, 50, readUInt32LE(bytes, 47));
        
        // Byte 51-54: Logical terminal ID
        addField(fields, "Logical terminal ID", "51-54", "Unsigned Integer", "4",
                 bytes, fullBinary, 51, 54, readUInt32LE(bytes, 51));
        
        // Byte 55: Terminal type
        addField(fields, "Terminal type", "55", "Unsigned Integer", "1",
                 bytes, fullBinary, 55, 55, bytes[55] & 0xFF);
    }
    
    /**
     * Add body fields based on message code
     * This method contains message-specific parsing logic
     */
    private void addBodyFields(List<AcpParsedField> fields, byte[] bytes, String fullBinary,
                                int messageCode, int sourceSystem, int headerSize) {
        int offset = headerSize;
        int bodyEnd = bytes.length - 1; // Exclude checksum
        
        switch (messageCode) {
            case 2658:
                // BCS-RT Request Account Info
                parseMessage2658Body(fields, bytes, fullBinary, offset, bodyEnd);
                break;
            case 2659:
                // BCS-RT Reply Account Info
                parseMessage2659Body(fields, bytes, fullBinary, offset, bodyEnd);
                break;
            default:
                // Generic parsing for unknown messages
                parseGenericBody(fields, bytes, fullBinary, offset, bodyEnd);
                break;
        }
    }
    
    /**
     * Parse message 2658 body (BCS-RT Request Account Info)
     */
    private void parseMessage2658Body(List<AcpParsedField> fields, byte[] bytes, 
                                       String fullBinary, int offset, int bodyEnd) {
        // Standard request fields
        addField(fields, "A/c number", String.format("%d-%d", offset, offset+3), 
                 "Unsigned Integer", "4", bytes, fullBinary, offset, offset+3, 
                 readUInt32LE(bytes, offset));
        offset += 4;
        
        addField(fields, "Recorder track", String.format("%d-%d", offset, offset+3), 
                 "Unsigned Integer", "4", bytes, fullBinary, offset, offset+3, 
                 readUInt32LE(bytes, offset));
        offset += 4;
        
        // Extended account opening fields
        if (offset < bodyEnd) {
            addField(fields, "Customer salutation", String.valueOf(offset), 
                     "Unsigned Integer", "1", bytes, fullBinary, offset, offset, 
                     bytes[offset] & 0xFF);
            offset += 1;
            
            String surname = readString(bytes, offset, 21);
            addField(fields, "Customer surname", String.format("%d-%d", offset, offset+20), 
                     "String", "21", bytes, fullBinary, offset, offset+20, 
                     surname.isEmpty() ? "(empty)" : surname);
            offset += 21;
            
            String otherName = readString(bytes, offset, 41);
            addField(fields, "Customer other name", String.format("%d-%d", offset, offset+40), 
                     "String", "41", bytes, fullBinary, offset, offset+40, 
                     otherName.isEmpty() ? "(empty)" : otherName);
            offset += 41;
            
            String chineseSurname = readString(bytes, offset, 12);
            addField(fields, "Customer Chinese surname", String.format("%d-%d", offset, offset+11), 
                     "String", "12", bytes, fullBinary, offset, offset+11, 
                     chineseSurname.isEmpty() ? "(empty)" : chineseSurname);
            offset += 12;
            
            String chineseOtherName = readString(bytes, offset, 12);
            addField(fields, "Customer Chinese other name", String.format("%d-%d", offset, offset+11), 
                     "String", "12", bytes, fullBinary, offset, offset+11, 
                     chineseOtherName.isEmpty() ? "(empty)" : chineseOtherName);
            offset += 12;
            
            addField(fields, "Channel Accessibility", String.valueOf(offset), 
                     "Unsigned Integer", "1", bytes, fullBinary, offset, offset, 
                     bytes[offset] & 0xFF);
            offset += 1;
            
            addField(fields, "Ticket type", String.valueOf(offset), 
                     "Unsigned Integer", "1", bytes, fullBinary, offset, offset, 
                     bytes[offset] & 0xFF);
            offset += 1;
            
            addField(fields, "Football type", String.valueOf(offset), 
                     "Unsigned Integer", "1", bytes, fullBinary, offset, offset, 
                     bytes[offset] & 0xFF);
            offset += 1;
            
            addField(fields, "Spoken language", String.valueOf(offset), 
                     "Unsigned Integer", "1", bytes, fullBinary, offset, offset, 
                     bytes[offset] & 0xFF);
            offset += 1;
            
            addField(fields, "Special A/C", String.valueOf(offset), 
                     "Unsigned Integer", "1", bytes, fullBinary, offset, offset, 
                     bytes[offset] & 0xFF);
            offset += 1;
            
            addField(fields, "Account type", String.valueOf(offset), 
                     "Unsigned Integer", "1", bytes, fullBinary, offset, offset, 
                     bytes[offset] & 0xFF);
            offset += 1;
            
            addField(fields, "Security Code", String.format("%d-%d", offset, offset+3), 
                     "Unsigned Integer", "4", bytes, fullBinary, offset, offset+3, 
                     readUInt32LE(bytes, offset));
            offset += 4;
            
            addField(fields, "Flag", String.valueOf(offset), 
                     "Unsigned Integer", "1", bytes, fullBinary, offset, offset, 
                     bytes[offset] & 0xFF);
            offset += 1;
            
            // Bank information 1
            addField(fields, "Bank sequence - 1", String.valueOf(offset), 
                     "Unsigned Integer", "1", bytes, fullBinary, offset, offset, 
                     bytes[offset] & 0xFF);
            offset += 1;
            
            addField(fields, "Bank number - 1", String.format("%d-%d", offset, offset+3), 
                     "Unsigned Integer", "4", bytes, fullBinary, offset, offset+3, 
                     readUInt32LE(bytes, offset));
            offset += 4;
            
            addField(fields, "Branch number - 1", String.format("%d-%d", offset, offset+3), 
                     "Unsigned Integer", "4", bytes, fullBinary, offset, offset+3, 
                     readUInt32LE(bytes, offset));
            offset += 4;
            
            String bankAccount1 = readString(bytes, offset, 13);
            addField(fields, "Bank account number - 1", String.format("%d-%d", offset, offset+12), 
                     "String", "13", bytes, fullBinary, offset, offset+12, 
                     bankAccount1.isEmpty() ? "(empty)" : bankAccount1);
            offset += 13;
            
            // Bank information 2
            addField(fields, "Bank sequence - 2", String.valueOf(offset), 
                     "Unsigned Integer", "1", bytes, fullBinary, offset, offset, 
                     bytes[offset] & 0xFF);
            offset += 1;
            
            addField(fields, "Bank number - 2", String.format("%d-%d", offset, offset+3), 
                     "Unsigned Integer", "4", bytes, fullBinary, offset, offset+3, 
                     readUInt32LE(bytes, offset));
            offset += 4;
            
            addField(fields, "Branch number - 2", String.format("%d-%d", offset, offset+3), 
                     "Unsigned Integer", "4", bytes, fullBinary, offset, offset+3, 
                     readUInt32LE(bytes, offset));
            offset += 4;
            
            String bankAccount2 = readString(bytes, offset, 13);
            addField(fields, "Bank account number - 2", String.format("%d-%d", offset, offset+12), 
                     "String", "13", bytes, fullBinary, offset, offset+12, 
                     bankAccount2.isEmpty() ? "(empty)" : bankAccount2);
            offset += 13;
            
            addField(fields, "Restricted", String.valueOf(offset), 
                     "Unsigned Integer", "1", bytes, fullBinary, offset, offset, 
                     bytes[offset] & 0xFF);
            offset += 1;
            
            String appRef = readString(bytes, offset, 13);
            addField(fields, "Online application ref no.", String.format("%d-%d", offset, offset+12), 
                     "String", "13", bytes, fullBinary, offset, offset+12, 
                     appRef.isEmpty() ? "(empty)" : appRef);
            offset += 13;
            
            addField(fields, "eWallet Only Indicator", String.valueOf(offset), 
                     "Unsigned Integer", "1", bytes, fullBinary, offset, offset, 
                     bytes[offset] & 0xFF);
            offset += 1;
            
            String crmRef = readString(bytes, offset, 41);
            addField(fields, "CRM reference no.", String.format("%d-%d", offset, offset+40), 
                     "String", "41", bytes, fullBinary, offset, offset+40, 
                     crmRef.isEmpty() ? "(empty)" : crmRef);
        }
    }
    
    /**
     * Parse message 2659 body (BCS-RT Reply Account Info)
     */
    private void parseMessage2659Body(List<AcpParsedField> fields, byte[] bytes, 
                                       String fullBinary, int offset, int bodyEnd) {
        // TODO: Implement message 2659 body parsing
        // For now, use generic parsing
        parseGenericBody(fields, bytes, fullBinary, offset, bodyEnd);
    }
    
    /**
     * Generic body parsing for unknown message types
     */
    private void parseGenericBody(List<AcpParsedField> fields, byte[] bytes, 
                                   String fullBinary, int offset, int bodyEnd) {
        int fieldNum = 0;
        while (offset + 3 < bodyEnd) {
            addField(fields, "Body field " + fieldNum, String.format("%d-%d", offset, offset + 3), 
                     "Unsigned Integer", "4", bytes, fullBinary, offset, offset + 3, 
                     readUInt32LE(bytes, offset));
            offset += 4;
            fieldNum++;
        }
        
        // Handle remaining bytes
        while (offset < bodyEnd) {
            addField(fields, "Body byte " + offset, String.valueOf(offset), 
                     "Unsigned Integer", "1", bytes, fullBinary, offset, offset, 
                     bytes[offset] & 0xFF);
            offset++;
        }
    }
    
    // Utility methods
    
    private void addField(List<AcpParsedField> fields, String fieldName, String bytePosition,
                          String dataType, String size, byte[] bytes, String fullBinary,
                          int startByte, int endByte, Object value) {
        String msgData = extractBinaryForBytes(fullBinary, startByte, endByte);
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
