package com.solace.simulator.model;

/**
 * Debit/Credit Transaction Request/Reply (BCS-RT)
 * Request: Message Code 2610
 * Reply: Message Code 2611
 * 
 * Used to process account debit or credit transactions
 */
public class DebitCreditTransactionMessage {
    
    private AcpMessageHeader header;
    
    // Optional message content starting at byte 52
    // Transaction-specific data
    private byte[] transactionData;
    
    public DebitCreditTransactionMessage() {
    }
    
    public DebitCreditTransactionMessage(AcpMessageHeader header) {
        this.header = header;
    }
    
    public AcpMessageHeader getHeader() {
        return header;
    }
    
    public void setHeader(AcpMessageHeader header) {
        this.header = header;
    }
    
    public byte[] getTransactionData() {
        return transactionData;
    }
    
    public void setTransactionData(byte[] transactionData) {
        this.transactionData = transactionData;
    }
    
    public static int getRequestMessageCode() {
        return 2610;
    }
    
    public static int getReplyMessageCode() {
        return 2611;
    }
    
    @Override
    public String toString() {
        return "DebitCreditTransactionMessage{" +
                "header=" + header +
                ", transactionDataLength=" + (transactionData != null ? transactionData.length : 0) +
                '}';
    }
}
