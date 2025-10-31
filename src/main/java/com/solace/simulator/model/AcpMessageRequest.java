package com.solace.simulator.model;

/**
 * Request model for parsing ACP messages from hexadecimal input
 */
public class AcpMessageRequest {
    private String hexString;
    
    public AcpMessageRequest() {
    }
    
    public AcpMessageRequest(String hexString) {
        this.hexString = hexString;
    }
    
    public String getHexString() {
        return hexString;
    }
    
    public void setHexString(String hexString) {
        this.hexString = hexString;
    }
}
