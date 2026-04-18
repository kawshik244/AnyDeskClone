package com.remotedesktop.server.service;

import com.remotedesktop.server.model.Session;
import com.remotedesktop.server.model.SessionStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public Session createSession(String hostId) {
        String code = generateUniqueCode();
        Session session = new Session(code, hostId);
        sessions.put(code, session);
        return session;
    }

    public Session getSession(String sessionCode) {
        return sessions.get(normalizeCode(sessionCode));
    }

    public Session joinSession(String sessionCode, String viewerId) {
        Session session = requireSession(sessionCode);

        if (session.getStatus() == SessionStatus.TERMINATED) {
            throw new SessionException(HttpStatus.GONE, "Session has already ended");
        }
        if (session.getHostId().equalsIgnoreCase(viewerId)) {
            throw new SessionException(HttpStatus.CONFLICT, "You cannot join your own session as viewer");
        }
        if (session.hasViewer() && !viewerId.equalsIgnoreCase(session.getViewerId())) {
            throw new SessionException(HttpStatus.CONFLICT, "Session already has a viewer");
        }

        session.setViewerId(viewerId);
        if (session.getHostSocket() == null || !session.getHostSocket().isOpen()) {
            session.setStatus(SessionStatus.WAITING_FOR_HOST);
        } else {
            markActive(session);
        }
        return session;
    }

    public Session attachHostSocket(String sessionCode, String hostId, WebSocketSession socket) {
        Session session = requireSession(sessionCode);
        if (!session.getHostId().equalsIgnoreCase(hostId)) {
            throw new SessionException(HttpStatus.FORBIDDEN, "Only the session host can register as host");
        }
        if (session.getStatus() == SessionStatus.TERMINATED) {
            throw new SessionException(HttpStatus.GONE, "Session has already ended");
        }

        session.setHostSocket(socket);
        if (session.hasViewer()) {
            markActive(session);
        } else {
            session.setStatus(SessionStatus.WAITING_FOR_VIEWER);
        }
        return session;
    }

    public Session attachViewerSocket(String sessionCode, String viewerId, WebSocketSession socket) {
        Session session = requireSession(sessionCode);
        if (!session.hasViewer() || !viewerId.equalsIgnoreCase(session.getViewerId())) {
            throw new SessionException(HttpStatus.FORBIDDEN, "Viewer must join the session before opening the remote stream");
        }
        if (session.getStatus() == SessionStatus.TERMINATED) {
            throw new SessionException(HttpStatus.GONE, "Session has already ended");
        }

        session.setViewerSocket(socket);
        if (session.getHostSocket() == null || !session.getHostSocket().isOpen()) {
            session.setStatus(SessionStatus.WAITING_FOR_HOST);
        } else {
            markActive(session);
        }
        return session;
    }

    public Session terminateSession(String sessionCode, String requester, String reason) {
        Session session = requireSession(sessionCode);
        if (!session.getHostId().equalsIgnoreCase(requester)) {
            throw new SessionException(HttpStatus.FORBIDDEN, "Only the host can terminate this session");
        }
        markTerminated(session, reason);
        return session;
    }

    public Session handleDisconnect(WebSocketSession socket) {
        for (Session session : sessions.values()) {
            if (session.getHostSocket() != null && session.getHostSocket().getId().equals(socket.getId())) {
                session.setHostSocket(null);
                markTerminated(session, "Host disconnected");
                return session;
            }
            if (session.getViewerSocket() != null && session.getViewerSocket().getId().equals(socket.getId())) {
                session.setViewerSocket(null);
                session.setViewerId(null);
                session.setConnectedAt(null);
                if (session.getStatus() != SessionStatus.TERMINATED) {
                    session.setStatus(SessionStatus.WAITING_FOR_VIEWER);
                }
                return session;
            }
        }
        return null;
    }

    private void markActive(Session session) {
        session.setStatus(SessionStatus.ACTIVE);
        if (session.getConnectedAt() == null) {
            session.setConnectedAt(java.time.LocalDateTime.now());
        }
    }

    private void markTerminated(Session session, String reason) {
        session.setStatus(SessionStatus.TERMINATED);
        session.setEndedAt(java.time.LocalDateTime.now());
        session.setEndReason(reason);
    }

    private Session requireSession(String sessionCode) {
        Session session = getSession(sessionCode);
        if (session == null) {
            throw new SessionException(HttpStatus.NOT_FOUND, "Session not found");
        }
        return session;
    }

    private String normalizeCode(String sessionCode) {
        return sessionCode == null ? null : sessionCode.toUpperCase();
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = generateCode();
        } while (sessions.containsKey(code));
        return code;
    }

    private String generateCode() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 6)
                .toUpperCase();
    }
}
