package com.remotedesktop.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;

@Data
public class Session {

    private String sessionCode;
    private String hostId;
    private String viewerId;
    private SessionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime connectedAt;
    private LocalDateTime endedAt;
    private String endReason;

    @JsonIgnore
    private WebSocketSession hostSocket;

    @JsonIgnore
    private WebSocketSession viewerSocket;

    public Session(String sessionCode, String hostId) {
        this.sessionCode = sessionCode;
        this.hostId = hostId;
        this.viewerId = null;
        this.status = SessionStatus.WAITING_FOR_VIEWER;
        this.createdAt = LocalDateTime.now();
    }

    public boolean hasViewer() {
        return viewerId != null && !viewerId.isBlank();
    }
}
