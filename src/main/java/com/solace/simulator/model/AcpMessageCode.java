package com.solace.simulator.model;

/**
 * Enumeration of ACP Message Codes
 * Defines all known message types and their codes
 */
public enum AcpMessageCode {
    
    // Status Enquiry Messages
    STATUS_ENQUIRY_BCS_REQUEST(2073, "Status Enquiry for BCS Transactions - Request"),
    STATUS_ENQUIRY_BCS_REPLY(2074, "Status Enquiry for BCS Transactions - Reply"),
    STATUS_ENQUIRY_ACP_REQUEST(3019, "Status Enquiry for ACP Transactions - Request"),
    STATUS_ENQUIRY_ACP_REPLY(3020, "Status Enquiry for ACP Transactions - Reply"),
    IMPLICIT_REVERSE_ACP(3022, "Implicit Reverse for ACP Transactions"),
    
    // Account Management (BCS-BG)
    ACCOUNT_BET_PARAMETER_REQUEST(2500, "Account Bet Parameter - Request"),
    ACCOUNT_BET_PARAMETER_REPLY(2501, "Account Bet Parameter - Reply"),
    TOTAL_ACCOUNT_BALANCE_REQUEST(2505, "Total Account Balance - Request"),
    TOTAL_ACCOUNT_BALANCE_REPLY(2506, "Total Account Balance - Reply"),
    
    // Real-Time Account Messages (BCS-RT)
    ACCOUNT_OPEN_REQUEST(2604, "Account Open - Request"),
    ACCOUNT_OPEN_REPLY(2659, "Account Open - Reply"),
    ACCOUNT_CLOSE_REQUEST(2606, "Account Close - Request"),
    ACCOUNT_CLOSE_REPLY(2607, "Account Close - Reply"),
    CHANGE_SECURITY_CODE_REQUEST(2608, "Change Security Code - Request"),
    CHANGE_SECURITY_CODE_REPLY(2605, "Change Security Code - Reply"),
    DEBIT_CREDIT_TRANSACTION_REQUEST(2610, "Debit/Credit Transaction - Request"),
    DEBIT_CREDIT_TRANSACTION_REPLY(2611, "Debit/Credit Transaction - Reply"),
    VALIDATE_BET_REQUEST(2612, "Validate Bet - Request"),
    VALIDATE_BET_REPLY(2613, "Validate Bet - Reply"),
    ONLINE_STATEMENT_REQUEST(2650, "Online Statement - Request"),
    ONLINE_STATEMENT_REPLY(2651, "Online Statement - Reply"),
    EXTENDED_ONLINE_STATEMENT_REQUEST(2652, "Extended Online Statement - Request"),
    EXTENDED_ONLINE_STATEMENT_REPLY(2653, "Extended Online Statement - Reply"),
    ACCOUNT_DETAILS_ENQUIRY_REQUEST(2654, "Account Details Enquiry - Request"),
    ACCOUNT_DETAILS_ENQUIRY_REPLY(2655, "Account Details Enquiry - Reply"),
    ACCOUNT_DETAILS_UPDATE_REQUEST(2656, "Account Details Update - Request"),
    ACCOUNT_DETAILS_UPDATE_REPLY(2660, "Account Details Update - Reply"),
    ACCOUNT_FUND_ANALYSIS_REQUEST(2630, "Account Fund Analysis - Request"),
    ACCOUNT_FUND_ANALYSIS_REPLY(2631, "Account Fund Analysis - Reply"),
    MONITORED_ACCOUNT_REQUEST(2632, "Monitored Account - Request"),
    MONITORED_ACCOUNT_REPLY(2633, "Monitored Account - Reply"),
    ACCOUNT_PARAMETERS_REQUEST(2634, "Account Parameters - Request"),
    ACCOUNT_PARAMETERS_REPLY(2635, "Account Parameters - Reply"),
    ACCOUNT_STATUS_REQUEST(2636, "Account Status - Request"),
    ACCOUNT_STATUS_REPLY(2637, "Account Status - Reply"),
    STATEMENT_CHARGE_REQUEST(2638, "Statement Charge - Request"),
    STATEMENT_CHARGE_REPLY(2639, "Statement Charge - Reply"),
    ACCOUNT_FB_LIMIT_REQUEST(2640, "Account FB Limit - Request"),
    ACCOUNT_FB_LIMIT_REPLY(2641, "Account FB Limit - Reply"),
    ACCOUNT_BALANCE_CLOSURE_REQUEST(2648, "Account Balance for Closure - Request"),
    ACCOUNT_BALANCE_CLOSURE_REPLY(2649, "Account Balance for Closure - Reply"),
    
    // Card Management
    IBT_CARD_ISSUE_REQUEST(2614, "IBT Card Issue - Request"),
    IBT_CARD_ISSUE_REPLY(2615, "IBT Card Issue - Reply"),
    PREPAID_CARD_ACTIVATION_REQUEST(2662, "Prepaid Card Activation - Request"),
    PREPAID_CARD_ACTIVATION_REPLY(2663, "Prepaid Card Activation - Reply"),
    
    // Terminal Management
    RELEASE_TERMINAL_REQUEST(2620, "Release Terminal - Request"),
    RELEASE_TERMINAL_REPLY(2621, "Release Terminal - Reply"),
    CHANNEL_LOCK_REQUEST(2646, "Channel Lock - Request"),
    CHANNEL_LOCK_REPLY(2647, "Channel Lock - Reply"),
    
    // Betting Operations
    ALL_UP_EXPLOSION_REQUEST(2622, "All-Up Explosion - Request"),
    ALL_UP_EXPLOSION_REPLY(2623, "All-Up Explosion - Reply"),
    EXTENDED_ALL_UP_EXPLOSION_REQUEST(2644, "Extended All-Up Explosion - Request"),
    EXTENDED_ALL_UP_EXPLOSION_REPLY(2645, "Extended All-Up Explosion - Reply"),
    CALL_TRANSFER_REQUEST(2624, "Call Transfer - Request"),
    CALL_TRANSFER_REPLY(2625, "Call Transfer - Reply"),
    
    // Trap Messages
    TRAP_MESSAGE_REQUEST(2800, "Trap Message - Request"),
    TRAP_MESSAGE_REPLY(2801, "Trap Message - Reply"),
    
    UNKNOWN(0, "Unknown Message Code");
    
    private final int code;
    private final String description;
    
    AcpMessageCode(int code, String description) {
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
     * Get message code enum from integer code
     * @param code the message code
     * @return the corresponding enum, or UNKNOWN if not found
     */
    public static AcpMessageCode fromCode(int code) {
        for (AcpMessageCode msgCode : values()) {
            if (msgCode.code == code) {
                return msgCode;
            }
        }
        return UNKNOWN;
    }
    
    /**
     * Check if this is a request message
     * @return true if this is a request message
     */
    public boolean isRequest() {
        return description.contains("Request");
    }
    
    /**
     * Check if this is a reply message
     * @return true if this is a reply message
     */
    public boolean isReply() {
        return description.contains("Reply");
    }
    
    @Override
    public String toString() {
        return code + " - " + description;
    }
}
