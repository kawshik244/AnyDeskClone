package com.remotedesktop.server.controller;

import com.remotedesktop.server.model.Session;
import com.remotedesktop.server.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/create")
    public ResponseEntity<Session> createSession(Authentication authentication) {
        Session session = sessionService.createSession(authentication.getName());
        return ResponseEntity.ok(session);
    }

    @GetMapping("/{code}")
    public ResponseEntity<Session> getSession(@PathVariable String code) {
        Session session = sessionService.getSession(code);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(session);
    }

    @PostMapping("/{code}/join")
    public ResponseEntity<Session> joinSession(@PathVariable String code, Authentication authentication) {
        Session session = sessionService.joinSession(code, authentication.getName());
        return ResponseEntity.ok(session);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Session> terminateSession(@PathVariable String code, Authentication authentication) {
        Session session = sessionService.terminateSession(code, authentication.getName(), "Host terminated the session");
        return ResponseEntity.ok(session);
    }
}
