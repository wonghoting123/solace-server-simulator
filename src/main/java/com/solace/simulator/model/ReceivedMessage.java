package com.solace.simulator.model;

import java.time.LocalDateTime;
import java.util.Map;

public class ReceivedMessage {
    private String destination;
    private String messageType; // "TEXT" or "BYTE"
    private String content;
    private String hexContent;
    private Map<String, String> headers;
    private LocalDateTime timestamp;

    public ReceivedMessage() {
        this.timestamp = LocalDateTime.now();
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getHexContent() {
        return hexContent;
    }

    public void setHexContent(String hexContent) {
        this.hexContent = hexContent;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
