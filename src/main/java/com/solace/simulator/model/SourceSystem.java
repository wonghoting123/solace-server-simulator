package com.solace.simulator.model;

/**
 * Enum representing the source systems for handshake simulation
 */
public enum SourceSystem {
    BCS_AGP1("BCS – AGP1", 31, 17, "bcs_ba/31/evt/brc_opr/adptr/proc", "hk/g/inct/ipc/01/upd/bcs_ba/brc_opr/17/31/hdl/success"),
    BCS_AGP2("BCS – AGP2", 32, 18, "bcs_ba/32/evt/brc_opr/adptr/proc", "hk/g/inct/ipc/01/upd/bcs_ba/brc_opr/18/32/hdl/success"),
    BCS_AGP3("BCS – AGP3", 51, 19, "bcs_ba/51/evt/brc_opr/adptr/proc", "hk/g/inct/ipc/01/upd/bcs_ba/brc_opr/19/51/hdl/success"),
    BCS_AGP4("BCS – AGP4", 52, 1, "bcs_ba/52/evt/brc_opr/adptr/proc", "hk/g/inct/ipc/01/upd/bcs_ba/brc_opr/1/52/hdl/success");

    private final String displayName;
    private final int systemNumber;
    private final int destinationSystemNumber;
    private final String incomingQueue;
    private final String outgoingTopic;

    SourceSystem(String displayName, int systemNumber, int destinationSystemNumber, 
                 String incomingQueue, String outgoingTopic) {
        this.displayName = displayName;
        this.systemNumber = systemNumber;
        this.destinationSystemNumber = destinationSystemNumber;
        this.incomingQueue = incomingQueue;
        this.outgoingTopic = outgoingTopic;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getSystemNumber() {
        return systemNumber;
    }

    public int getDestinationSystemNumber() {
        return destinationSystemNumber;
    }

    public String getIncomingQueue() {
        return incomingQueue;
    }

    public String getOutgoingTopic() {
        return outgoingTopic;
    }
}
