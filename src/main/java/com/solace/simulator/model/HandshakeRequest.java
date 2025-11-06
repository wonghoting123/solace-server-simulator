package com.solace.simulator.model;

/**
 * Request model for starting a handshake simulation
 */
public class HandshakeRequest {
    private String sourceSystem; // e.g., "BCS_AGP1"
    private String businessDate; // Format: YYYY-MM-DD

    public HandshakeRequest() {
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(String businessDate) {
        this.businessDate = businessDate;
    }
}
