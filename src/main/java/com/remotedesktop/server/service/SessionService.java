package com.remotedesktop.server.service;

import com.remotedesktop.server.model.Session;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {

    // This acts as our temporary database (in memory)
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    // Create a new session, generate a random 6-character code
    public Session createSession(String hostId) {
        String code = generateCode();
        Session session = new Session(code, hostId);
        sessions.put(code, session);
        return session;
    }

    // Find a session by its code
    public Session getSession(String sessionCode) {
        return sessions.get(sessionCode.toUpperCase());
    }

    // Viewer joins an existing session
    public Session joinSession(String sessionCode, String viewerId) {
        Session session = sessions.get(sessionCode.toUpperCase());

        if (session == null) {
            throw new RuntimeException("Session not found");
        }
        if (!session.getStatus().equals("WAITING")) {
            throw new RuntimeException("Session already has a viewer");
        }

        session.setViewerId(viewerId);
        session.setStatus("CONNECTED");
        return session;
    }

    // End and remove a session
    public void deleteSession(String sessionCode) {
        sessions.remove(sessionCode.toUpperCase());
    }

    // Generate a random 6-character alphanumeric code
    private String generateCode() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 6)
                .toUpperCase();
    }
}