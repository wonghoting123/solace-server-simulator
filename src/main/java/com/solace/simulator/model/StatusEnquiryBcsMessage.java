package com.solace.simulator.model;

/**
 * Status Enquiry Request/Reply for BCS Transactions
 * Request: BCS→ACP (Message Code 2073)
 * Reply: ACP→BCS (Message Code 2074)
 * 
 * Used to check transaction status after timeout
 */
public class StatusEnquiryBcsMessage {
    
    private AcpMessageHeader header;
    
    // Byte 52+: Current/last processed transaction ID (8 bytes, unsigned integer)
    private long currentProcessedTransactionId;
    
    public StatusEnquiryBcsMessage() {
    }
    
    public StatusEnquiryBcsMessage(AcpMessageHeader header) {
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
        return 2073;
    }
    
    public static int getReplyMessageCode() {
        return 2074;
    }
    
    @Override
    public String toString() {
        return "StatusEnquiryBcsMessage{" +
                "header=" + header +
                ", currentProcessedTransactionId=" + currentProcessedTransactionId +
                '}';
    }
}
