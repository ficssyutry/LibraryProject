package com.example.library.controller;

import com.example.library.dto.request.Id39586LoginRequest;
import com.example.library.dto.request.Id39586RegisterRequest;
import com.example.library.dto.response.Id39586JwtResponse;
import com.example.library.service.Id39586AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class Id39586AuthController {

    private final Id39586AuthService authService;

    public Id39586AuthController(Id39586AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody Id39586RegisterRequest request) {
        try {
            String token = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(new Id39586JwtResponse(token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody Id39586LoginRequest request) {
        try {
            String token = authService.login(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(new Id39586JwtResponse(token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}