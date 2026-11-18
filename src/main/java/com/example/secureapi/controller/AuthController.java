package com.example.secureapi.controller;

import com.example.secureapi.dto.LoginRequest;
import com.example.secureapi.dto.RegisterRequest;
import com.example.secureapi.dto.TokenResponse;
import com.example.secureapi.model.User;
import com.example.secureapi.repository.UserRepository;
import com.example.secureapi.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final String dummyHash;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.dummyHash = encoder.encode("dummy-password-for-timing");
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest req) {
        if (users.existsByUsername(req.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }
        users.save(new User(req.username(), encoder.encode(req.password())));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "registered"));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        Optional<User> user = users.findByUsername(req.username());
        String hash = user.map(User::getPasswordHash).orElse(dummyHash);
        boolean ok = encoder.matches(req.password(), hash) && user.isPresent();
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return new TokenResponse(jwt.generate(req.username()), "Bearer", jwt.ttlSeconds());
    }
}
