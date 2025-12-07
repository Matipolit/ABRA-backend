package com.example.abra.controllers;

import com.example.abra.models.UserModel;
import com.example.abra.repositories.UserModelRepository;
import com.example.abra.security.JwtService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserModelRepository userModelRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userModelRepository.existsByLogin(request.getLogin())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Login already in use");
        }
        UserModel userModel = UserModel.builder()
            .login(request.getLogin())
            .password(passwordEncoder.encode(request.getPassword()))
            .build();
        userModelRepository.save(userModel);
        String token = jwtService.generateToken(userModel.getLogin());
        return ResponseEntity.status(HttpStatus.CREATED).body(new TokenResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword())
            );
            log.info("User '{}' logged in successfully", request.getLogin());
        } catch (AuthenticationException ex) {
            log.warn("Failed login attempt for user '{}': {}", request.getLogin(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        String token = jwtService.generateToken(request.getLogin());
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @Data
    public static class RegisterRequest {
        private String login;
        private String password;
    }

    @Data
    public static class LoginRequest {
        private String login;
        private String password;
    }

    @Data
    public static class TokenResponse {
        private final String token;
    }
}
