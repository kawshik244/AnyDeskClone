package com.remotedesktop.server.model;

public class SocketMessage {

    private String type;        // "REGISTER_HOST", "REGISTER_VIEWER", "SCREEN_DATA", "INPUT_EVENT"
    private String sessionCode; // which session this belongs to
    private String senderId;    // who is sending this message
    private String payload;     // the actual data (screen bytes, input event, etc.)

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSessionCode() {
        return sessionCode;
    }

    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
