package com.example.library.service;

import com.example.library.dto.request.Id39586RegisterRequest;
import com.example.library.dto.response.Id39586UserResponse;
import com.example.library.entity.User;
import com.example.library.exception.Id39586DuplicateResourceException;
import com.example.library.repository.Id39586UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Service
public class Id39586AuthService {

    private static final Logger logger = LoggerFactory.getLogger(Id39586AuthService.class);

    private final Id39586UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Id39586AsyncNotificationService asyncNotificationService;
    private final SecretKey secretKey;
    private final long expirationTime;

    public Id39586AuthService(Id39586UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       Id39586AsyncNotificationService asyncNotificationService,
                       @Value("${jwt.secret}") String secret,
                       @Value("${jwt.expiration}") long expirationTime) {  // добавляем параметр
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.asyncNotificationService = asyncNotificationService;
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        this.expirationTime = expirationTime;
        logger.info("JWT expiration time set to: {} ms ({} hours)", expirationTime, expirationTime / 3600000);
    }

    @Transactional
    public String register(Id39586RegisterRequest request) {
        logger.info("Registering new user with email: {}", request.getEmail());

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new Id39586DuplicateResourceException("User with email " + request.getEmail() + " already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : "USER");

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully with id: {}, role: {}", savedUser.getId(), savedUser.getRole());

        Id39586UserResponse userResponse = mapToUserResponse(savedUser);
        asyncNotificationService.sendWelcomeEmailAsync(userResponse);
        asyncNotificationService.logUserActionAsync(
                savedUser.getId(),
                "REGISTER",
                "New user registered with role: " + savedUser.getRole()
        );

        return generateToken(savedUser.getEmail(), savedUser.getRole());
    }

    public String login(String email, String rawPassword) throws Exception {
        logger.info("Login attempt for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("User not found"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new Exception("Invalid password");
        }

        logger.info("User logged in successfully: {}, role: {}", email, user.getRole());

        return generateToken(user.getEmail(), user.getRole());
    }

    private String generateToken(String email, String role) {
        logger.info("Generating token for email: {}, role: {}", email, role);

        Date issuedAt = new Date();
        Date expiration = new Date(System.currentTimeMillis() + expirationTime);

        logger.info("Token issued at: {}, expires at: {}", issuedAt, expiration);

        String token = Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(secretKey)
                .compact();

        logger.info("Token generated successfully");
        return token;
    }

    private Id39586UserResponse mapToUserResponse(User user) {
        Id39586UserResponse response = new Id39586UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        return response;
    }
}