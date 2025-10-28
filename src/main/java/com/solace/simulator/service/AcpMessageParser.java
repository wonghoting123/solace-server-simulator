package com.solace.simulator.service;

import com.solace.simulator.model.AcpMessage;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Service for parsing ACP (Application Communication Protocol) messages from hexadecimal strings
 * Converts hex input to binary and maps bytes to message fields using little-endian byte order
 */
@Service
public class AcpMessageParser {
    
    /**
     * Parse a hexadecimal string into an ACP message with mapped fields
     * @param hexString Hexadecimal string (with or without spaces)
     * @return Parsed ACP message with fields
     */
    public AcpMessage parseHexToAcpMessage(String hexString) {
        AcpMessage acpMessage = new AcpMessage();
        
        // Clean the hex string (remove spaces and convert to uppercase)
        String cleanHex = hexString.replaceAll("\\s+", "").toUpperCase();
        acpMessage.setRawHex(cleanHex);
        
        // Convert to byte array
        byte[] bytes = hexStringToByteArray(cleanHex);
        
        // Convert to binary string
        String binaryString = bytesToBinaryString(bytes);
        acpMessage.setBinaryString(binaryString);
        
        // Parse fields using little-endian byte order
        parseFields(bytes, acpMessage);
        
        return acpMessage;
    }
    
    /**
     * Parse message fields from byte array
     * This is a basic implementation based on the example in the issue
     * Field structure can be customized based on actual ACP protocol specification
     */
    private void parseFields(byte[] bytes, AcpMessage acpMessage) {
        if (bytes.length < 2) {
            throw new IllegalArgumentException("Message too short - minimum 2 bytes required");
        }
        
        int offset = 0;
        
        // Example field parsing - first 2 bytes as little-endian uint16
        // Based on issue example: F30A (hex) -> 2803 (decimal) in little-endian
        if (bytes.length >= 2) {
            int field1 = readUInt16LE(bytes, offset);
            acpMessage.addField("Field_0_1", field1);
            acpMessage.addField("Field_0_1_Hex", String.format("0x%04X", field1));
            offset += 2;
        }
        
        // Parse additional fields as uint16 little-endian (2 bytes each)
        int fieldIndex = 1;
        while (offset + 1 < bytes.length) {
            int value = readUInt16LE(bytes, offset);
            acpMessage.addField("Field_" + (offset) + "_" + (offset + 1), value);
            acpMessage.addField("Field_" + (offset) + "_" + (offset + 1) + "_Hex", String.format("0x%04X", value));
            offset += 2;
            fieldIndex++;
        }
        
        // If there's a remaining byte, add it as a single byte field
        if (offset < bytes.length) {
            int value = bytes[offset] & 0xFF;
            acpMessage.addField("Field_" + offset + "_Single", value);
            acpMessage.addField("Field_" + offset + "_Single_Hex", String.format("0x%02X", value));
        }
        
        // Add metadata
        acpMessage.addField("Total_Bytes", bytes.length);
        acpMessage.addField("Total_Fields_Parsed", fieldIndex);
    }
    
    /**
     * Read an unsigned 16-bit integer in little-endian byte order
     */
    private int readUInt16LE(byte[] bytes, int offset) {
        if (offset + 1 >= bytes.length) {
            throw new IndexOutOfBoundsException("Not enough bytes to read UInt16");
        }
        // Little-endian: least significant byte first
        return ((bytes[offset] & 0xFF)) | ((bytes[offset + 1] & 0xFF) << 8);
    }
    
    /**
     * Read an unsigned 32-bit integer in little-endian byte order
     */
    private long readUInt32LE(byte[] bytes, int offset) {
        if (offset + 3 >= bytes.length) {
            throw new IndexOutOfBoundsException("Not enough bytes to read UInt32");
        }
        return ((bytes[offset] & 0xFFL)) | 
               ((bytes[offset + 1] & 0xFFL) << 8) | 
               ((bytes[offset + 2] & 0xFFL) << 16) | 
               ((bytes[offset + 3] & 0xFFL) << 24);
    }
    
    /**
     * Convert hexadecimal string to byte array
     */
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
    
    /**
     * Convert byte array to binary string representation
     */
    private String bytesToBinaryString(byte[] bytes) {
        StringBuilder binary = new StringBuilder();
        for (byte b : bytes) {
            binary.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        return binary.toString();
    }
    
    /**
     * Convert byte array to hexadecimal string with spaces
     */
    public String bytesToHexString(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) {
                hex.append(" ");
            }
            hex.append(String.format("%02X", bytes[i]));
        }
        return hex.toString();
    }
}
