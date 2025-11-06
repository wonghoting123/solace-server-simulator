package com.solace.simulator.model;

import java.time.LocalDateTime;

/**
 * Model representing an active handshake simulation
 */
public class HandshakeSimulation {
    private String id;
    private SourceSystem sourceSystem;
    private String businessDate;
    private LocalDateTime startTime;
    private String status; // "RUNNING", "STOPPED"

    public HandshakeSimulation() {
    }

    public HandshakeSimulation(String id, SourceSystem sourceSystem, String businessDate) {
        this.id = id;
        this.sourceSystem = sourceSystem;
        this.businessDate = businessDate;
        this.startTime = LocalDateTime.now();
        this.status = "RUNNING";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SourceSystem getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(SourceSystem sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(String businessDate) {
        this.businessDate = businessDate;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
