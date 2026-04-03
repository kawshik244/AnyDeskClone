package com.remotedesktop.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.remotedesktop.server.model.Session;
import com.remotedesktop.server.model.SocketMessage;
import com.remotedesktop.server.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoteDesktopHandler extends TextWebSocketHandler {

    private final SessionService sessionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Called when a client connects
    @Override
    public void afterConnectionEstablished(WebSocketSession socket) {
        log.info("New connection: {}", socket.getId());
    }

    // Called when a message arrives
    @Override
    protected void handleTextMessage(WebSocketSession socket, TextMessage message) throws Exception {

        // Parse the incoming JSON message
        SocketMessage socketMessage = objectMapper.readValue(message.getPayload(), SocketMessage.class);

        switch (socketMessage.getType()) {

            case "REGISTER_HOST" -> handleRegisterHost(socket, socketMessage);

            case "REGISTER_VIEWER" -> handleRegisterViewer(socket, socketMessage);

            case "SCREEN_DATA" -> forwardToViewer(socketMessage);

            case "INPUT_EVENT" -> forwardToHost(socketMessage);

            default -> log.warn("Unknown message type: {}", socketMessage.getType());
        }
    }

    // Host registers itself to a session
    private void handleRegisterHost(WebSocketSession socket, SocketMessage message) throws Exception {
        Session session = sessionService.getSession(message.getSessionCode());

        if (session == null) {
            sendMessage(socket, "ERROR", "Session not found", null);
            return;
        }

        session.setHostSocket(socket);
        log.info("Host registered for session: {}", message.getSessionCode());
        sendMessage(socket, "REGISTERED", "You are the host", message.getSessionCode());
    }

    // Viewer registers itself to a session
    private void handleRegisterViewer(WebSocketSession socket, SocketMessage message) throws Exception {
        Session session = sessionService.getSession(message.getSessionCode());

        if (session == null) {
            sendMessage(socket, "ERROR", "Session not found", null);
            return;
        }

        session.setViewerSocket(socket);
        session.setStatus("CONNECTED");
        log.info("Viewer joined session: {}", message.getSessionCode());
        sendMessage(socket, "REGISTERED", "You are the viewer", message.getSessionCode());

        // Also notify the host that viewer has joined
        if (session.getHostSocket() != null && session.getHostSocket().isOpen()) {
            sendMessage(session.getHostSocket(), "VIEWER_JOINED", "A viewer connected", message.getSessionCode());
        }
    }

    // Forward screen data from host → viewer
    private void forwardToViewer(SocketMessage message) throws Exception {
        Session session = sessionService.getSession(message.getSessionCode());

        if (session == null || session.getViewerSocket() == null) return;

        if (session.getViewerSocket().isOpen()) {
            String json = objectMapper.writeValueAsString(message);
            session.getViewerSocket().sendMessage(new TextMessage(json));
        }
    }

    // Forward input events from viewer → host
    private void forwardToHost(SocketMessage message) throws Exception {
        Session session = sessionService.getSession(message.getSessionCode());

        if (session == null || session.getHostSocket() == null) return;

        if (session.getHostSocket().isOpen()) {
            String json = objectMapper.writeValueAsString(message);
            session.getHostSocket().sendMessage(new TextMessage(json));
        }
    }

    // Called when someone disconnects
    @Override
    public void afterConnectionClosed(WebSocketSession socket, CloseStatus status) {
        log.info("Connection closed: {}", socket.getId());
        // You can add cleanup logic here later
    }

    // Helper method to send a message back to a socket
    private void sendMessage(WebSocketSession socket, String type, String payload, String sessionCode) throws Exception {
        SocketMessage response = new SocketMessage();
        response.setType(type);
        response.setPayload(payload);
        response.setSessionCode(sessionCode);
        String json = objectMapper.writeValueAsString(response);
        socket.sendMessage(new TextMessage(json));
    }
}