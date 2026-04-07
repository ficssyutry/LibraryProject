package com.example.library.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/debug")
public class Id39586DebugController {

    private static final Logger logger = LoggerFactory.getLogger(Id39586DebugController.class);

    @GetMapping("/token-info")
    public Map<String, Object> getTokenInfo(HttpServletRequest request) {
        Map<String, Object> info = new HashMap<>();

        String email = (String) request.getAttribute("email");
        String role = (String) request.getAttribute("role");
        String authHeader = request.getHeader("Authorization");

        info.put("email", email);
        info.put("role", role != null ? role : "USER (default)");
        info.put("authHeaderPresent", authHeader != null);
        info.put("authHeaderPreview", authHeader != null ? authHeader.substring(0, Math.min(50, authHeader.length())) + "..." : "null");

        logger.info("Debug info requested: email={}, role={}", email, role);

        return info;
    }

    @GetMapping("/test-auth")
    public String testAuth(HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        return "Authenticated! User: " + (email != null ? email : "unknown");
    }
}