package com.solace.simulator.model;

/**
 * Online Statement Request/Reply (BCS-RT)
 * Request: Message Code 2650
 * Reply: Message Code 2651
 * 
 * Used to generate account statement
 */
public class OnlineStatementMessage {
    
    private AcpMessageHeader header;
    
    // Optional message content starting at byte 52
    // Statement request/response data
    private byte[] statementData;
    
    public OnlineStatementMessage() {
    }
    
    public OnlineStatementMessage(AcpMessageHeader header) {
        this.header = header;
    }
    
    public AcpMessageHeader getHeader() {
        return header;
    }
    
    public void setHeader(AcpMessageHeader header) {
        this.header = header;
    }
    
    public byte[] getStatementData() {
        return statementData;
    }
    
    public void setStatementData(byte[] statementData) {
        this.statementData = statementData;
    }
    
    public static int getRequestMessageCode() {
        return 2650;
    }
    
    public static int getReplyMessageCode() {
        return 2651;
    }
    
    @Override
    public String toString() {
        return "OnlineStatementMessage{" +
                "header=" + header +
                ", statementDataLength=" + (statementData != null ? statementData.length : 0) +
                '}';
    }
}
