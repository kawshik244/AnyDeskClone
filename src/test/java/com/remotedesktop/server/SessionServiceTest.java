package com.remotedesktop.server;

import com.remotedesktop.server.model.Session;
import com.remotedesktop.server.service.SessionService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SessionServiceTest {

    private final SessionService sessionService = new SessionService();

    @Test
    void shouldCreateSessionWithCode() {
        Session session = sessionService.createSession("host-1");
        assertNotNull(session.getSessionCode());
        assertEquals("WAITING", session.getStatus());
        assertEquals("host-1", session.getHostId());
    }

    @Test
    void shouldJoinSession() {
        Session session = sessionService.createSession("host-1");
        Session joined = sessionService.joinSession(session.getSessionCode(), "viewer-1");
        assertEquals("CONNECTED", joined.getStatus());
        assertEquals("viewer-1", joined.getViewerId());
    }

    @Test
    void shouldThrowWhenSessionNotFound() {
        assertThrows(RuntimeException.class, () ->
                sessionService.joinSession("WRONG1", "viewer-1")
        );
    }
}