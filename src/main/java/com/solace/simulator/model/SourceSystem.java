package com.solace.simulator.model;

/**
 * Enum representing the source systems for handshake simulation
 */
public enum SourceSystem {
    BCS_AGP1_BACKGROUND("BCS – AGP1(background)", 31, 17, "bcs_ba/31/evt/brc_opr/adptr/proc", "hk/g/inct/ipc/01/upd/bcs_ba/brc_opr/17/31/hdl/success"),
    BCS_AGP2_BACKGROUND("BCS – AGP2(background)", 32, 18, "bcs_ba/32/evt/brc_opr/adptr/proc", "hk/g/inct/ipc/01/upd/bcs_ba/brc_opr/18/32/hdl/success"),
    BCS_AGP3_BACKGROUND("BCS – AGP3(background)", 51, 19, "bcs_ba/51/evt/brc_opr/adptr/proc", "hk/g/inct/ipc/01/upd/bcs_ba/brc_opr/19/51/hdl/success"),
    BCS_AGP4_BACKGROUND("BCS – AGP4(background)", 52, 1, "bcs_ba/52/evt/brc_opr/adptr/proc", "hk/g/inct/ipc/01/upd/bcs_ba/brc_opr/1/52/hdl/success"),
    BCS_AGP1_REALTIME("BCS – AGP1(real-time)", 12, 17, "bcs_ba/12/evt/agcy_opr/adptr/res", "hk/g/inct/ipc/01/upd/bcs_ba/agcy_opr/12/17/init"),
    BCS_AGP2_REALTIME("BCS – AGP2(real-time)", 12, 18, "bcs_ba/12/evt/agcy_opr/adptr/res", "hk/g/inct/ipc/01/upd/bcs_ba/agcy_opr/12/18/init"),
    BCS_AGP3_REALTIME("BCS – AGP3(real-time)", 12, 19, "bcs_ba/12/evt/agcy_opr/adptr/res", "hk/g/inct/ipc/01/upd/bcs_ba/agcy_opr/12/19/init"),
    BCS_AGP4_REALTIME("BCS – AGP4(real-time)", 12, 1, "bcs_ba/12/evt/agcy_opr/adptr/res", "hk/g/inct/ipc/01/upd/bcs_ba/agcy_opr/12/1/init");

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
    
    /**
     * Find the outgoing topic based on source and destination system numbers from received message
     */
    public static String findOutgoingTopic(int sourceSystemNumber, int destinationSystemNumber) {
        for (SourceSystem system : values()) {
            // Match by destination (BCS) system number and source (AGP) system number
            if (system.getSystemNumber() == destinationSystemNumber && 
                system.getDestinationSystemNumber() == sourceSystemNumber) {
                return system.getOutgoingTopic();
            }
        }
        return null;
    }
}
