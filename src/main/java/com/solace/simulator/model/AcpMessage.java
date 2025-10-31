package com.solace.simulator.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ACP (Application Communication Protocol) Message structure
 * This model represents the parsed fields from a hexadecimal byte message
 */
public class AcpMessage {
    private String rawHex;
    private String binaryString;
    private Map<String, Object> fields;
    private List<AcpMessageFieldDetail> fieldDetails;
    private List<AcpParsedField> parsedFields;  // New: For table format display
    
    public AcpMessage() {
        this.fields = new LinkedHashMap<>();
        this.fieldDetails = new ArrayList<>();
        this.parsedFields = new ArrayList<>();
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
    
    public List<AcpMessageFieldDetail> getFieldDetails() {
        return fieldDetails;
    }
    
    public void setFieldDetails(List<AcpMessageFieldDetail> fieldDetails) {
        this.fieldDetails = fieldDetails;
    }
    
    public void addFieldDetail(AcpMessageFieldDetail fieldDetail) {
        this.fieldDetails.add(fieldDetail);
    }
    
    public List<AcpParsedField> getParsedFields() {
        return parsedFields;
    }
    
    public void setParsedFields(List<AcpParsedField> parsedFields) {
        this.parsedFields = parsedFields;
    }
    
    public void addParsedField(AcpParsedField parsedField) {
        this.parsedFields.add(parsedField);
    }
}
