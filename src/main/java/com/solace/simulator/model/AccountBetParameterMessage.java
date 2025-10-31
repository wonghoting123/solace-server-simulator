package com.solace.simulator.model;

/**
 * Account Bet Parameter Request/Reply (BCS-BG)
 * Request: Message Code 2500
 * Reply: Message Code 2501
 * 
 * Used to manage betting parameters for accounts
 */
public class AccountBetParameterMessage {
    
    private AcpMessageHeader header;
    
    // Optional message content starting at byte 52
    // Specific fields depend on implementation requirements
    private byte[] optionalContent;
    
    public AccountBetParameterMessage() {
    }
    
    public AccountBetParameterMessage(AcpMessageHeader header) {
        this.header = header;
    }
    
    public AcpMessageHeader getHeader() {
        return header;
    }
    
    public void setHeader(AcpMessageHeader header) {
        this.header = header;
    }
    
    public byte[] getOptionalContent() {
        return optionalContent;
    }
    
    public void setOptionalContent(byte[] optionalContent) {
        this.optionalContent = optionalContent;
    }
    
    public static int getRequestMessageCode() {
        return 2500;
    }
    
    public static int getReplyMessageCode() {
        return 2501;
    }
    
    @Override
    public String toString() {
        return "AccountBetParameterMessage{" +
                "header=" + header +
                ", optionalContentLength=" + (optionalContent != null ? optionalContent.length : 0) +
                '}';
    }
}
