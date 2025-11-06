package com.solace.simulator.model;

/**
 * Enumeration of ACP Reply Codes
 * Used in the reply code field of message headers
 */
public enum AcpReplyCode {
    
    SUCCESS(0, "Success"),
    INVALID_MESSAGE_FORMAT(1, "Invalid message format"),
    TRANSACTION_NOT_RECEIVED(2, "Transaction not received"),
    OLD_TRANSACTION(3, "Old transaction"),
    REVERSE_CANNOT_BE_DONE(4, "Reverse cannot be done"),
    
    // Additional error codes can be added here as needed
    UNKNOWN_ERROR(9999, "Unknown error");
    
    private final int code;
    private final String description;
    
    AcpReplyCode(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Get reply code enum from integer code
     * @param code the reply code
     * @return the corresponding enum, or UNKNOWN_ERROR if not found
     */
    public static AcpReplyCode fromCode(int code) {
        for (AcpReplyCode replyCode : values()) {
            if (replyCode.code == code) {
                return replyCode;
            }
        }
        return UNKNOWN_ERROR;
    }
    
    /**
     * Check if this reply code indicates success
     * @return true if this is a success code
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }
    
    /**
     * Check if this reply code indicates an error
     * @return true if this is an error code
     */
    public boolean isError() {
        return this != SUCCESS;
    }
    
    @Override
    public String toString() {
        return code + " - " + description;
    }
}
