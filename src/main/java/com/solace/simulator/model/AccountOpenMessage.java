package com.solace.simulator.model;

/**
 * Account Open Request/Reply (BCS-RT)
 * Request: Message Code 2604
 * Reply: Message Code 2659
 * 
 * Used to create a new account
 */
public class AccountOpenMessage {
    
    private AcpMessageHeader header;
    
    // Optional message content starting at byte 52
    // Account-specific fields
    private byte[] accountData;
    
    public AccountOpenMessage() {
    }
    
    public AccountOpenMessage(AcpMessageHeader header) {
        this.header = header;
    }
    
    public AcpMessageHeader getHeader() {
        return header;
    }
    
    public void setHeader(AcpMessageHeader header) {
        this.header = header;
    }
    
    public byte[] getAccountData() {
        return accountData;
    }
    
    public void setAccountData(byte[] accountData) {
        this.accountData = accountData;
    }
    
    public static int getRequestMessageCode() {
        return 2604;
    }
    
    public static int getReplyMessageCode() {
        return 2659;
    }
    
    @Override
    public String toString() {
        return "AccountOpenMessage{" +
                "header=" + header +
                ", accountDataLength=" + (accountData != null ? accountData.length : 0) +
                '}';
    }
}
