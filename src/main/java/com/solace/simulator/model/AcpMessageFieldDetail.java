package com.solace.simulator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a parsed ACP message field with detailed information
 * including binary representation, byte positions, and values
 */
public class AcpMessageFieldDetail {
    private String fieldName;
    private String binaryRepresentation;
    private int startByte;
    private int endByte;
    private Object value;
    private String hexValue;
    private String description;
    
    public AcpMessageFieldDetail() {
    }
    
    public AcpMessageFieldDetail(String fieldName, String binaryRepresentation, 
                                  int startByte, int endByte, Object value) {
        this.fieldName = fieldName;
        this.binaryRepresentation = binaryRepresentation;
        this.startByte = startByte;
        this.endByte = endByte;
        this.value = value;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public String getBinaryRepresentation() {
        return binaryRepresentation;
    }
    
    public void setBinaryRepresentation(String binaryRepresentation) {
        this.binaryRepresentation = binaryRepresentation;
    }
    
    public int getStartByte() {
        return startByte;
    }
    
    public void setStartByte(int startByte) {
        this.startByte = startByte;
    }
    
    public int getEndByte() {
        return endByte;
    }
    
    public void setEndByte(int endByte) {
        this.endByte = endByte;
    }
    
    public Object getValue() {
        return value;
    }
    
    public void setValue(Object value) {
        this.value = value;
    }
    
    public String getHexValue() {
        return hexValue;
    }
    
    public void setHexValue(String hexValue) {
        this.hexValue = hexValue;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Get byte range as string (e.g., "0-1" or "2")
     */
    public String getByteRange() {
        if (startByte == endByte) {
            return String.valueOf(startByte);
        }
        return startByte + "-" + endByte;
    }
    
    /**
     * Get formatted display string
     */
    public String getDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append(binaryRepresentation);
        sb.append(" - ");
        sb.append(fieldName);
        sb.append(" (bytes ");
        sb.append(getByteRange());
        sb.append(", value: ");
        sb.append(value);
        sb.append(")");
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return getDisplayString();
    }
}
