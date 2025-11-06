package com.solace.simulator.model;

/**
 * Status Enquiry Request/Reply for ACP Transactions
 * Request: ACP→BCS (Message Code 3019)
 * Reply: BCS→ACP (Message Code 3020)
 * 
 * Used to check transaction status after timeout
 * Header requires: Transaction ID, Logical Terminal ID
 */
public class StatusEnquiryAcpMessage {
    
    private AcpMessageHeader header;
    
    // Byte 52+: Current/last processed transaction ID (8 bytes, unsigned integer)
    private long currentProcessedTransactionId;
    
    public StatusEnquiryAcpMessage() {
    }
    
    public StatusEnquiryAcpMessage(AcpMessageHeader header) {
        this.header = header;
    }
    
    public AcpMessageHeader getHeader() {
        return header;
    }
    
    public void setHeader(AcpMessageHeader header) {
        this.header = header;
    }
    
    public long getCurrentProcessedTransactionId() {
        return currentProcessedTransactionId;
    }
    
    public void setCurrentProcessedTransactionId(long currentProcessedTransactionId) {
        this.currentProcessedTransactionId = currentProcessedTransactionId;
    }
    
    public static int getRequestMessageCode() {
        return 3019;
    }
    
    public static int getReplyMessageCode() {
        return 3020;
    }
    
    @Override
    public String toString() {
        return "StatusEnquiryAcpMessage{" +
                "header=" + header +
                ", currentProcessedTransactionId=" + currentProcessedTransactionId +
                '}';
    }
}
