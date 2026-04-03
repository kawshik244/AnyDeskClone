package com.remotedesktop.server.model;

import lombok.Data;

@Data
public class SocketMessage {

    private String type;        // "REGISTER_HOST", "REGISTER_VIEWER", "SCREEN_DATA", "INPUT_EVENT"
    private String sessionCode; // which session this belongs to
    private String senderId;    // who is sending this message
    private String payload;     // the actual data (screen bytes, input event, etc.)
}