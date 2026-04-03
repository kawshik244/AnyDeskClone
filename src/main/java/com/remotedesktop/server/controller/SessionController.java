package com.remotedesktop.server.controller;

import com.remotedesktop.server.model.Session;
import com.remotedesktop.server.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    // Host calls this to start sharing → gets a session code back
    @PostMapping("/create")
    public ResponseEntity<Session> createSession(@RequestParam String hostId) {
        Session session = sessionService.createSession(hostId);
        return ResponseEntity.ok(session);
    }

    // Viewer calls this to check if a code exists
    @GetMapping("/{code}")
    public ResponseEntity<Session> getSession(@PathVariable String code) {
        Session session = sessionService.getSession(code);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(session);
    }

    // Viewer calls this to join a session
    @PostMapping("/{code}/join")
    public ResponseEntity<Session> joinSession(
            @PathVariable String code,
            @RequestParam String viewerId) {
        Session session = sessionService.joinSession(code, viewerId);
        return ResponseEntity.ok(session);
    }

    // Host calls this to stop sharing
    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteSession(@PathVariable String code) {
        sessionService.deleteSession(code);
        return ResponseEntity.noContent().build();
    }
}