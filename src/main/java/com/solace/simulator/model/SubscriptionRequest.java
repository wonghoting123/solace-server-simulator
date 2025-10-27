package com.solace.simulator.model;

public class SubscriptionRequest {
    private String destination;
    private String destinationType; // "TOPIC" or "QUEUE"

    public SubscriptionRequest() {
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }
}
