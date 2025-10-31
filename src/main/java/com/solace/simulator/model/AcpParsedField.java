package com.solace.simulator.model;

/**
 * Represents a parsed field with full specification details from acp_message.htm
 * Includes: Data (field name), Byte Position, Data Type, Size, Msg Data (binary), Msg Data Value
 */
public class AcpParsedField {
    private String data;               // Field name (e.g., "Message code")
    private String bytePosition;       // Byte position (e.g., "0-1" or "2")
    private String dataType;           // Data type (e.g., "Unsigned Integer", "String")
    private String size;               // Size in bytes (e.g., "2", "1")
    private String msgData;            // Binary representation (e.g., "11110011 00001010")
    private String msgDataValue;       // Decoded value (e.g., "2803")
    
    public AcpParsedField() {
    }
    
    public AcpParsedField(String data, String bytePosition, String dataType, 
                          String size, String msgData, String msgDataValue) {
        this.data = data;
        this.bytePosition = bytePosition;
        this.dataType = dataType;
        this.size = size;
        this.msgData = msgData;
        this.msgDataValue = msgDataValue;
    }
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    public String getBytePosition() {
        return bytePosition;
    }
    
    public void setBytePosition(String bytePosition) {
        this.bytePosition = bytePosition;
    }
    
    public String getDataType() {
        return dataType;
    }
    
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    
    public String getSize() {
        return size;
    }
    
    public void setSize(String size) {
        this.size = size;
    }
    
    public String getMsgData() {
        return msgData;
    }
    
    public void setMsgData(String msgData) {
        this.msgData = msgData;
    }
    
    public String getMsgDataValue() {
        return msgDataValue;
    }
    
    public void setMsgDataValue(String msgDataValue) {
        this.msgDataValue = msgDataValue;
    }
    
    @Override
    public String toString() {
        return "AcpParsedField{" +
                "data='" + data + '\'' +
                ", bytePosition='" + bytePosition + '\'' +
                ", dataType='" + dataType + '\'' +
                ", size='" + size + '\'' +
                ", msgData='" + msgData + '\'' +
                ", msgDataValue='" + msgDataValue + '\'' +
                '}';
    }
}
