package com.remotedesktop.server.model;

import lombok.Data;
import org.springframework.web.socket.WebSocketSession;
import java.time.LocalDateTime;

@Data
public class Session {

    private String sessionCode;
    private String hostId;
    private String viewerId;
    private String status;
    private LocalDateTime createdAt;

    // NEW — store the actual live WebSocket connections
    private WebSocketSession hostSocket;
    private WebSocketSession viewerSocket;

    public Session(String sessionCode, String hostId) {
        this.sessionCode = sessionCode;
        this.hostId = hostId;
        this.viewerId = null;
        this.status = "WAITING";
        this.createdAt = LocalDateTime.now();
    }
}