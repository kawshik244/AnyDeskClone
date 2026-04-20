package com.remotedesktop.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;

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

    public String getSessionCode() {
        return sessionCode;
    }

    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getViewerId() {
        return viewerId;
    }

    public void setViewerId(String viewerId) {
        this.viewerId = viewerId;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(LocalDateTime connectedAt) {
        this.connectedAt = connectedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public String getEndReason() {
        return endReason;
    }

    public void setEndReason(String endReason) {
        this.endReason = endReason;
    }

    public WebSocketSession getHostSocket() {
        return hostSocket;
    }

    public void setHostSocket(WebSocketSession hostSocket) {
        this.hostSocket = hostSocket;
    }

    public WebSocketSession getViewerSocket() {
        return viewerSocket;
    }

    public void setViewerSocket(WebSocketSession viewerSocket) {
        this.viewerSocket = viewerSocket;
    }
}
