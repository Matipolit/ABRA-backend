package com.example.abra.controllers;

import com.example.abra.models.UserModel;
import com.example.abra.repositories.UserModelRepository;
import com.example.abra.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserModelRepository userModelRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthController controller;

    @Test
    void register_success_returnsToken() {
        AuthController.RegisterRequest req = new AuthController.RegisterRequest();
        req.setLogin("newuser");
        req.setPassword("pass");

        when(userModelRepository.existsByLogin("newuser")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(jwtService.generateToken("newuser")).thenReturn("jwt-token");

        ResponseEntity<?> response = controller.register(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthController.TokenResponse);
        verify(userModelRepository).save(any(UserModel.class));
    }

    @Test
    void register_conflict_returns409() {
        AuthController.RegisterRequest req = new AuthController.RegisterRequest();
        req.setLogin("existing");

        when(userModelRepository.existsByLogin("existing")).thenReturn(true);

        ResponseEntity<?> response = controller.register(req);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(userModelRepository, never()).save(any());
    }

    @Test
    void login_success_returnsToken() {
        AuthController.LoginRequest req = new AuthController.LoginRequest();
        req.setLogin("user");
        req.setPassword("pass");

        when(jwtService.generateToken("user")).thenReturn("jwt-token");

        ResponseEntity<?> response = controller.login(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_failure_returns401() {
        AuthController.LoginRequest req = new AuthController.LoginRequest();
        req.setLogin("user");
        req.setPassword("wrong");

        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Bad creds"));

        ResponseEntity<?> response = controller.login(req);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
