package com.remotedesktop.server.auth;

import com.remotedesktop.server.jwt.JwtUtil;
import com.remotedesktop.server.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    // In-memory user store — same idea as sessions, we add a real DB later
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public AuthService(JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(AuthRequest request) {
        if (users.containsKey(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Never store plain text password — always hash it
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        users.put(request.getUsername(), new User(request.getUsername(), hashedPassword));

        String token = jwtUtil.generateToken(request.getUsername());
        return new AuthResponse(token, request.getUsername());
    }

    public AuthResponse login(AuthRequest request) {
        User user = users.get(request.getUsername());

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Compare plain password against stored hash
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }

        String token = jwtUtil.generateToken(request.getUsername());
        return new AuthResponse(token, request.getUsername());
    }
}
