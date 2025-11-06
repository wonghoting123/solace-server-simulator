package com.solace.simulator.model;

/**
 * Represents the common ACP message header structure (52 bytes)
 * All ACP messages must contain this header with mandatory fields
 */
public class AcpMessageHeader {
    
    // Bytes 0-1: Message Code (Unsigned Integer, 2 bytes)
    private int messageCode;
    
    // Byte 2: Source System Number (Integer, 1 byte)
    private int sourceSystemNumber;
    
    // Byte 3: Destination System Number (Integer, 1 byte)
    private int destinationSystemNumber;
    
    // Bytes 4-5: Reply Code (Unsigned Integer, 2 bytes)
    private int replyCode;
    
    // Bytes 6-13: Last Transaction ID (Unsigned Integer, 8 bytes)
    private long lastTransactionId;
    
    // Bytes 14-21: Message Transaction ID (Unsigned Integer, 8 bytes)
    private long messageTransactionId;
    
    // Bytes 22-25: Date (Unsigned Integer, 4 bytes)
    private long date;
    
    // Bytes 26-28: Time (Unsigned Integer, 3 bytes)
    private int time;
    
    // Bytes 29-32: Location ID (Unsigned Integer, 4 bytes)
    private long locationId;
    
    // Bytes 33-34: Position Number (Unsigned Integer, 2 bytes)
    private int positionNumber;
    
    // Bytes 35-42: Physical Terminal ID (String, 8 bytes)
    private String physicalTerminalId;
    
    // Bytes 43-46: Staff ID (Unsigned Integer, 4 bytes)
    private long staffId;
    
    // Bytes 47-50: Logical Terminal ID (Unsigned Integer, 4 bytes)
    private long logicalTerminalId;
    
    // Byte 51: Terminal Type (Unsigned Integer, 1 byte)
    private int terminalType;
    
    public AcpMessageHeader() {
    }
    
    // Getters and Setters
    
    public int getMessageCode() {
        return messageCode;
    }
    
    public void setMessageCode(int messageCode) {
        this.messageCode = messageCode;
    }
    
    public int getSourceSystemNumber() {
        return sourceSystemNumber;
    }
    
    public void setSourceSystemNumber(int sourceSystemNumber) {
        this.sourceSystemNumber = sourceSystemNumber;
    }
    
    public int getDestinationSystemNumber() {
        return destinationSystemNumber;
    }
    
    public void setDestinationSystemNumber(int destinationSystemNumber) {
        this.destinationSystemNumber = destinationSystemNumber;
    }
    
    public int getReplyCode() {
        return replyCode;
    }
    
    public void setReplyCode(int replyCode) {
        this.replyCode = replyCode;
    }
    
    public long getLastTransactionId() {
        return lastTransactionId;
    }
    
    public void setLastTransactionId(long lastTransactionId) {
        this.lastTransactionId = lastTransactionId;
    }
    
    public long getMessageTransactionId() {
        return messageTransactionId;
    }
    
    public void setMessageTransactionId(long messageTransactionId) {
        this.messageTransactionId = messageTransactionId;
    }
    
    public long getDate() {
        return date;
    }
    
    public void setDate(long date) {
        this.date = date;
    }
    
    public int getTime() {
        return time;
    }
    
    public void setTime(int time) {
        this.time = time;
    }
    
    public long getLocationId() {
        return locationId;
    }
    
    public void setLocationId(long locationId) {
        this.locationId = locationId;
    }
    
    public int getPositionNumber() {
        return positionNumber;
    }
    
    public void setPositionNumber(int positionNumber) {
        this.positionNumber = positionNumber;
    }
    
    public String getPhysicalTerminalId() {
        return physicalTerminalId;
    }
    
    public void setPhysicalTerminalId(String physicalTerminalId) {
        this.physicalTerminalId = physicalTerminalId;
    }
    
    public long getStaffId() {
        return staffId;
    }
    
    public void setStaffId(long staffId) {
        this.staffId = staffId;
    }
    
    public long getLogicalTerminalId() {
        return logicalTerminalId;
    }
    
    public void setLogicalTerminalId(long logicalTerminalId) {
        this.logicalTerminalId = logicalTerminalId;
    }
    
    public int getTerminalType() {
        return terminalType;
    }
    
    public void setTerminalType(int terminalType) {
        this.terminalType = terminalType;
    }
    
    /**
     * Validate that mandatory fields are present
     * @return true if all mandatory fields are set
     */
    public boolean isValid() {
        return messageCode > 0 
            && sourceSystemNumber >= 0
            && destinationSystemNumber >= 0
            && messageTransactionId > 0;
    }
    
    @Override
    public String toString() {
        return "AcpMessageHeader{" +
                "messageCode=" + messageCode +
                ", sourceSystemNumber=" + sourceSystemNumber +
                ", destinationSystemNumber=" + destinationSystemNumber +
                ", replyCode=" + replyCode +
                ", messageTransactionId=" + messageTransactionId +
                ", locationId=" + locationId +
                ", terminalType=" + terminalType +
                '}';
    }
}
