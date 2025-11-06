package com.solace.simulator.service;

import com.solace.simulator.model.*;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service for parsing complete ACP messages including header and optional content
 * based on message code to map fields to their corresponding positions
 */
@Service
public class AcpMessageHeaderParser {
    
    /**
     * Parse complete ACP message from hex string
     * Extracts header and maps optional content based on message code
     * 
     * @param hexString The hexadecimal string to parse
     * @return AcpMessage with header and fields mapped by message code
     */
    public AcpMessage parseCompleteMessage(String hexString) {
        AcpMessage acpMessage = new AcpMessage();
        
        // Clean and convert hex string to bytes
        String cleanHex = hexString.replaceAll("\\s+", "").toUpperCase();
        acpMessage.setRawHex(cleanHex);
        
        byte[] bytes = hexStringToByteArray(cleanHex);
        
        // Convert to binary string
        String binaryString = bytesToBinaryString(bytes);
        acpMessage.setBinaryString(binaryString);
        
        // Parse message header (first 52 bytes)
        AcpMessageHeader header = parseHeader(bytes);
        
        // Store header fields in the fields map
        Map<String, Object> fields = new LinkedHashMap<>();
        addHeaderFields(fields, header);
        
        // Parse optional content based on message code (starting at byte 52)
        if (bytes.length > 52) {
            parseOptionalContent(fields, bytes, header.getMessageCode());
        }
        
        acpMessage.setFields(fields);
        
        return acpMessage;
    }
    
    /**
     * Parse the message header (bytes 0-51)
     */
    private AcpMessageHeader parseHeader(byte[] bytes) {
        if (bytes.length < 52) {
            throw new IllegalArgumentException("Message too short - minimum 52 bytes required for header");
        }
        
        AcpMessageHeader header = new AcpMessageHeader();
        
        // Byte 0-1: Message Code (little-endian)
        header.setMessageCode(readUInt16LE(bytes, 0));
        
        // Byte 2: Source System Number
        header.setSourceSystemNumber(bytes[2] & 0xFF);
        
        // Byte 3: Destination System Number
        header.setDestinationSystemNumber(bytes[3] & 0xFF);
        
        // Byte 4-5: Reply Code (little-endian)
        header.setReplyCode(readUInt16LE(bytes, 4));
        
        // Byte 6-13: Last Transaction ID (little-endian)
        header.setLastTransactionId(readUInt64LE(bytes, 6));
        
        // Byte 14-21: Message Transaction ID (little-endian)
        header.setMessageTransactionId(readUInt64LE(bytes, 14));
        
        // Byte 22-25: Date (little-endian)
        header.setDate(readUInt32LE(bytes, 22));
        
        // Byte 26-28: Time (little-endian, 3 bytes)
        header.setTime(readUInt24LE(bytes, 26));
        
        // Byte 29-32: Location ID (little-endian)
        header.setLocationId(readUInt32LE(bytes, 29));
        
        // Byte 33-34: Position Number (little-endian)
        header.setPositionNumber(readUInt16LE(bytes, 33));
        
        // Byte 35-42: Physical Terminal ID (string, 8 bytes)
        header.setPhysicalTerminalId(readString(bytes, 35, 8));
        
        // Byte 43-46: Staff ID (little-endian)
        header.setStaffId(readUInt32LE(bytes, 43));
        
        // Byte 47-50: Logical Terminal ID (little-endian)
        header.setLogicalTerminalId(readUInt32LE(bytes, 47));
        
        // Byte 51: Terminal Type
        header.setTerminalType(bytes[51] & 0xFF);
        
        return header;
    }
    
    /**
     * Add header fields to the fields map with descriptive names
     */
    private void addHeaderFields(Map<String, Object> fields, AcpMessageHeader header) {
        fields.put("Message_Code", header.getMessageCode());
        fields.put("Message_Code_Hex", String.format("0x%04X", header.getMessageCode()));
        fields.put("Message_Type", AcpMessageCode.fromCode(header.getMessageCode()).getDescription());
        
        fields.put("Source_System_Number", header.getSourceSystemNumber());
        fields.put("Destination_System_Number", header.getDestinationSystemNumber());
        
        fields.put("Reply_Code", header.getReplyCode());
        fields.put("Reply_Code_Description", AcpReplyCode.fromCode(header.getReplyCode()).getDescription());
        
        fields.put("Last_Transaction_ID", header.getLastTransactionId());
        fields.put("Message_Transaction_ID", header.getMessageTransactionId());
        
        fields.put("Date", header.getDate());
        fields.put("Time", header.getTime());
        
        fields.put("Location_ID", header.getLocationId());
        fields.put("Position_Number", header.getPositionNumber());
        fields.put("Physical_Terminal_ID", header.getPhysicalTerminalId());
        
        fields.put("Staff_ID", header.getStaffId());
        fields.put("Logical_Terminal_ID", header.getLogicalTerminalId());
        fields.put("Terminal_Type", header.getTerminalType());
    }
    
    /**
     * Parse optional content based on message code
     * Maps fields to their specific positions according to the interface specification
     */
    private void parseOptionalContent(Map<String, Object> fields, byte[] bytes, int messageCode) {
        int offset = 52; // Optional content starts at byte 52
        int remainingBytes = bytes.length - offset;
        
        fields.put("Optional_Content_Start_Byte", offset);
        fields.put("Optional_Content_Length", remainingBytes);
        
        // Map fields based on message code
        AcpMessageCode msgCodeEnum = AcpMessageCode.fromCode(messageCode);
        
        switch (msgCodeEnum) {
            case STATUS_ENQUIRY_BCS_REQUEST:
            case STATUS_ENQUIRY_BCS_REPLY:
            case STATUS_ENQUIRY_ACP_REQUEST:
            case STATUS_ENQUIRY_ACP_REPLY:
                // Byte 52+: Current/last processed transaction ID (8 bytes)
                if (remainingBytes >= 8) {
                    long transactionId = readUInt64LE(bytes, offset);
                    fields.put("Current_Processed_Transaction_ID", transactionId);
                    fields.put("Current_Processed_Transaction_ID_Hex", String.format("0x%016X", transactionId));
                }
                break;
                
            case ACCOUNT_BET_PARAMETER_REQUEST:
            case ACCOUNT_BET_PARAMETER_REPLY:
            case TOTAL_ACCOUNT_BALANCE_REQUEST:
            case TOTAL_ACCOUNT_BALANCE_REPLY:
            case ACCOUNT_OPEN_REQUEST:
            case ACCOUNT_OPEN_REPLY:
            case DEBIT_CREDIT_TRANSACTION_REQUEST:
            case DEBIT_CREDIT_TRANSACTION_REPLY:
            case ONLINE_STATEMENT_REQUEST:
            case ONLINE_STATEMENT_REPLY:
                // For these message types, parse as generic fields
                // Specific field structures can be added as they're defined in the specification
                parseGenericOptionalContent(fields, bytes, offset, remainingBytes);
                break;
                
            default:
                // For unknown or unhandled message types, parse as generic fields
                parseGenericOptionalContent(fields, bytes, offset, remainingBytes);
                break;
        }
    }
    
    /**
     * Parse optional content as generic 2-byte fields (little-endian)
     */
    private void parseGenericOptionalContent(Map<String, Object> fields, byte[] bytes, int offset, int remainingBytes) {
        int fieldIndex = 0;
        int currentOffset = offset;
        
        while (currentOffset + 1 < bytes.length) {
            int value = readUInt16LE(bytes, currentOffset);
            String fieldName = String.format("Optional_Field_%d_Bytes_%d_%d", fieldIndex, currentOffset, currentOffset + 1);
            fields.put(fieldName, value);
            fields.put(fieldName + "_Hex", String.format("0x%04X", value));
            currentOffset += 2;
            fieldIndex++;
        }
        
        // Handle any remaining single byte
        if (currentOffset < bytes.length) {
            int value = bytes[currentOffset] & 0xFF;
            fields.put(String.format("Optional_Field_Byte_%d", currentOffset), value);
            fields.put(String.format("Optional_Field_Byte_%d_Hex", currentOffset), String.format("0x%02X", value));
        }
    }
    
    // Utility methods for reading different data types in little-endian format
    
    private int readUInt16LE(byte[] bytes, int offset) {
        if (offset + 1 >= bytes.length) {
            throw new IndexOutOfBoundsException("Not enough bytes to read UInt16 at offset " + offset);
        }
        return ((bytes[offset] & 0xFF)) | ((bytes[offset + 1] & 0xFF) << 8);
    }
    
    private int readUInt24LE(byte[] bytes, int offset) {
        if (offset + 2 >= bytes.length) {
            throw new IndexOutOfBoundsException("Not enough bytes to read UInt24 at offset " + offset);
        }
        return ((bytes[offset] & 0xFF)) | 
               ((bytes[offset + 1] & 0xFF) << 8) | 
               ((bytes[offset + 2] & 0xFF) << 16);
    }
    
    private long readUInt32LE(byte[] bytes, int offset) {
        if (offset + 3 >= bytes.length) {
            throw new IndexOutOfBoundsException("Not enough bytes to read UInt32 at offset " + offset);
        }
        return ((bytes[offset] & 0xFFL)) | 
               ((bytes[offset + 1] & 0xFFL) << 8) | 
               ((bytes[offset + 2] & 0xFFL) << 16) | 
               ((bytes[offset + 3] & 0xFFL) << 24);
    }
    
    private long readUInt64LE(byte[] bytes, int offset) {
        if (offset + 7 >= bytes.length) {
            throw new IndexOutOfBoundsException("Not enough bytes to read UInt64 at offset " + offset);
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
            throw new IndexOutOfBoundsException("Not enough bytes to read string at offset " + offset);
        }
        
        // Find the first null byte or use full length
        int actualLength = length;
        for (int i = 0; i < length; i++) {
            if (bytes[offset + i] == 0) {
                actualLength = i;
                break;
            }
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
