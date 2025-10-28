package com.solace.simulator.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ACP (Application Communication Protocol) Message structure
 * This model represents the parsed fields from a hexadecimal byte message
 */
public class AcpMessage {
    private String rawHex;
    private String binaryString;
    private Map<String, Object> fields;
    
    public AcpMessage() {
        this.fields = new LinkedHashMap<>();
    }
    
    public String getRawHex() {
        return rawHex;
    }
    
    public void setRawHex(String rawHex) {
        this.rawHex = rawHex;
    }
    
    public String getBinaryString() {
        return binaryString;
    }
    
    public void setBinaryString(String binaryString) {
        this.binaryString = binaryString;
    }
    
    public Map<String, Object> getFields() {
        return fields;
    }
    
    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }
    
    public void addField(String name, Object value) {
        this.fields.put(name, value);
    }
}
