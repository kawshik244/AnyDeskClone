package com.remotedesktop.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.remotedesktop.server.model.Session;
import com.remotedesktop.server.model.SocketMessage;
import com.remotedesktop.server.service.SessionService;
import com.remotedesktop.server.service.SessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class RemoteDesktopHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(RemoteDesktopHandler.class);

    private final SessionService sessionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RemoteDesktopHandler(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    // Called when a client connects
    @Override
    public void afterConnectionEstablished(WebSocketSession socket) {
        log.info("New connection: {}", socket.getId());
    }

    // Called when a message arrives
    @Override
    protected void handleTextMessage(WebSocketSession socket, TextMessage message) throws Exception {
        SocketMessage socketMessage = objectMapper.readValue(message.getPayload(), SocketMessage.class);

        try {
            switch (socketMessage.getType()) {
                case "REGISTER_HOST" -> handleRegisterHost(socket, socketMessage);
                case "REGISTER_VIEWER" -> handleRegisterViewer(socket, socketMessage);
                case "SCREEN_DATA" -> forwardToViewer(socketMessage);
                case "INPUT_EVENT" -> forwardToHost(socketMessage);
                case "TERMINATE_SESSION" -> terminateSession(socketMessage);
                default -> sendMessage(socket, "ERROR", "Unknown message type: " + socketMessage.getType(), socketMessage.getSessionCode());
            }
        } catch (SessionException ex) {
            sendMessage(socket, "ERROR", ex.getMessage(), socketMessage.getSessionCode());
        }
    }

    private void handleRegisterHost(WebSocketSession socket, SocketMessage message) throws Exception {
        Session session = sessionService.attachHostSocket(message.getSessionCode(), message.getSenderId(), socket);
        log.info("Host registered for session: {}", message.getSessionCode());
        sendMessage(socket, "REGISTERED", "Host stream registered", message.getSessionCode());
        sendMessage(socket, "SESSION_STATUS", session.getStatus().name(), message.getSessionCode());
        if (session.getViewerSocket() != null && session.getViewerSocket().isOpen()) {
            sendMessage(session.getViewerSocket(), "SESSION_STATUS", session.getStatus().name(), message.getSessionCode());
        }
    }

    private void handleRegisterViewer(WebSocketSession socket, SocketMessage message) throws Exception {
        Session session = sessionService.attachViewerSocket(message.getSessionCode(), message.getSenderId(), socket);
        log.info("Viewer joined session: {}", message.getSessionCode());
        sendMessage(socket, "REGISTERED", "Viewer stream registered", message.getSessionCode());
        sendMessage(socket, "SESSION_STATUS", session.getStatus().name(), message.getSessionCode());

        if (session.getHostSocket() != null && session.getHostSocket().isOpen()) {
            sendMessage(session.getHostSocket(), "VIEWER_JOINED", "A viewer connected", message.getSessionCode());
            sendMessage(session.getHostSocket(), "SESSION_STATUS", session.getStatus().name(), message.getSessionCode());
        }
    }

    private void forwardToViewer(SocketMessage message) throws Exception {
        Session session = sessionService.getSession(message.getSessionCode());

        if (session == null || session.getViewerSocket() == null || session.getStatus() != com.remotedesktop.server.model.SessionStatus.ACTIVE) {
            return;
        }

        if (session.getViewerSocket().isOpen()) {
            String json = objectMapper.writeValueAsString(message);
            session.getViewerSocket().sendMessage(new TextMessage(json));
        }
    }

    private void forwardToHost(SocketMessage message) throws Exception {
        Session session = sessionService.getSession(message.getSessionCode());

        if (session == null || session.getHostSocket() == null || session.getStatus() != com.remotedesktop.server.model.SessionStatus.ACTIVE) {
            return;
        }

        if (session.getHostSocket().isOpen()) {
            String json = objectMapper.writeValueAsString(message);
            session.getHostSocket().sendMessage(new TextMessage(json));
        }
    }

    private void terminateSession(SocketMessage message) throws Exception {
        Session session = sessionService.terminateSession(message.getSessionCode(), message.getSenderId(), "Host terminated the session");
        notifyTermination(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession socket, CloseStatus status) {
        log.info("Connection closed: {}", socket.getId());
        try {
            Session session = sessionService.handleDisconnect(socket);
            if (session == null) {
                return;
            }
            if (session.getStatus() == com.remotedesktop.server.model.SessionStatus.TERMINATED) {
                notifyTermination(session);
            } else if (session.getHostSocket() != null && session.getHostSocket().isOpen()) {
                sendMessage(session.getHostSocket(), "VIEWER_LEFT", "Viewer disconnected", session.getSessionCode());
                sendMessage(session.getHostSocket(), "SESSION_STATUS", session.getStatus().name(), session.getSessionCode());
            }
        } catch (Exception ex) {
            log.error("Failed to process disconnect cleanup", ex);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.warn("WebSocket transport error on {}: {}", session.getId(), exception.getMessage());
        session.close(CloseStatus.SERVER_ERROR);
    }

    private void notifyTermination(Session session) throws Exception {
        if (session.getHostSocket() != null && session.getHostSocket().isOpen()) {
            sendMessage(session.getHostSocket(), "SESSION_TERMINATED", session.getEndReason(), session.getSessionCode());
        }
        if (session.getViewerSocket() != null && session.getViewerSocket().isOpen()) {
            sendMessage(session.getViewerSocket(), "SESSION_TERMINATED", session.getEndReason(), session.getSessionCode());
        }
    }

    private void sendMessage(WebSocketSession socket, String type, String payload, String sessionCode) throws Exception {
        if (socket == null || !socket.isOpen()) {
            return;
        }
        SocketMessage response = new SocketMessage();
        response.setType(type);
        response.setPayload(payload);
        response.setSessionCode(sessionCode);
        String json = objectMapper.writeValueAsString(response);
        socket.sendMessage(new TextMessage(json));
    }
}
