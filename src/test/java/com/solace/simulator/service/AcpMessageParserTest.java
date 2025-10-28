package com.solace.simulator.service;

import com.solace.simulator.model.AcpMessage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ACP Message Parser
 */
class AcpMessageParserTest {
    
    private final AcpMessageParser parser = new AcpMessageParser();
    
    @Test
    void testParseSimpleHexString() {
        // Test with the example from the issue: F30A should be 2803 in little-endian
        String hexInput = "F30A";
        
        AcpMessage result = parser.parseHexToAcpMessage(hexInput);
        
        assertNotNull(result);
        assertEquals("F30A", result.getRawHex());
        assertEquals("1111001100001010", result.getBinaryString());
        
        // First field should be 2803 (0x0AF3 in little-endian)
        assertEquals(2803, result.getFields().get("Field_0_1"));
        assertEquals("0x0AF3", result.getFields().get("Field_0_1_Hex"));
    }
    
    @Test
    void testParseHexStringWithSpaces() {
        String hexInput = "F3 0A 29 14";
        
        AcpMessage result = parser.parseHexToAcpMessage(hexInput);
        
        assertNotNull(result);
        assertEquals("F30A2914", result.getRawHex());
        
        // First 2 bytes: F30A = 2803 in little-endian
        assertEquals(2803, result.getFields().get("Field_0_1"));
        
        // Next 2 bytes: 2914 = 5161 in little-endian
        assertEquals(5161, result.getFields().get("Field_2_3"));
    }
    
    @Test
    void testParseLongHexString() {
        // Use the example from the issue
        String hexInput = "F30A291408000101000000000000000000002A5AB300000000001209E8070C0C375E0700000000314730332020202059B301002204000001EBE50400FEFF7F0000000058";
        
        AcpMessage result = parser.parseHexToAcpMessage(hexInput);
        
        assertNotNull(result);
        assertEquals(hexInput, result.getRawHex());
        
        // Verify first field
        assertEquals(2803, result.getFields().get("Field_0_1"));
        assertEquals("0x0AF3", result.getFields().get("Field_0_1_Hex"));
        
        // Verify total bytes (68 bytes = 136 hex characters / 2)
        assertEquals(68, result.getFields().get("Total_Bytes"));
    }
    
    @Test
    void testParseOddLengthHexString() {
        String hexInput = "F30"; // Odd length
        
        assertThrows(IllegalArgumentException.class, () -> {
            parser.parseHexToAcpMessage(hexInput);
        });
    }
    
    @Test
    void testParseInvalidHexCharacters() {
        String hexInput = "F3XY";
        
        assertThrows(IllegalArgumentException.class, () -> {
            parser.parseHexToAcpMessage(hexInput);
        });
    }
    
    @Test
    void testLittleEndianConversion() {
        // Test specific little-endian conversion
        // Hex input "0AF3" represents bytes [0x0A, 0xF3] in memory order
        // In little-endian, these bytes are read with least significant byte first
        // So: 0x0A (LSB) | 0xF3 (MSB) << 8 = 0xF30A = 62218 decimal
        String hexInput = "0AF3";
        
        AcpMessage result = parser.parseHexToAcpMessage(hexInput);
        
        // In little-endian, bytes [0x0A, 0xF3] should be read as 0xF30A = 62218
        assertEquals(62218, result.getFields().get("Field_0_1"));
    }
}
