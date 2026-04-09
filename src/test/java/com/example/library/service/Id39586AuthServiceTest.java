package com.example.library.service;

import com.example.library.dto.request.Id39586LoginRequest;
import com.example.library.dto.request.Id39586RegisterRequest;
import com.example.library.entity.User;
import com.example.library.exception.Id39586DuplicateResourceException;
import com.example.library.repository.Id39586UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class Id39586AuthServiceTest {

    @Autowired
    private Id39586AuthService authService;

    @Autowired
    private Id39586UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Id39586RegisterRequest validRegisterRequest;
    private Id39586LoginRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        validRegisterRequest = new Id39586RegisterRequest();
        validRegisterRequest.setName("John Doe");
        validRegisterRequest.setEmail("john@example.com");
        validRegisterRequest.setPassword("password123");
        validRegisterRequest.setRole("USER");

        validLoginRequest = new Id39586LoginRequest();
        validLoginRequest.setEmail("john@example.com");
        validLoginRequest.setPassword("password123");
    }

    // ================= SUCCESS CASES =================


    @Test
    void register_Success_ShouldReturnToken() {
        String token = authService.register(validRegisterRequest);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        User savedUser = userRepository.findByEmail("john@example.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("John Doe");
        assertThat(savedUser.getEmail()).isEqualTo("john@example.com");
        assertThat(savedUser.getRole()).isEqualTo("USER");
    }


    @Test
    void login_Success_ShouldReturnToken() throws Exception {
        authService.register(validRegisterRequest);

        String token = authService.login(validLoginRequest.getEmail(), validLoginRequest.getPassword());

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }


    @Test
    void register_WithAdminRole_Success() {
        validRegisterRequest.setRole("ADMIN");

        String token = authService.register(validRegisterRequest);

        assertThat(token).isNotNull();
        User savedUser = userRepository.findByEmail("john@example.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getRole()).isEqualTo("ADMIN");
    }


    @Test
    void register_WithoutRole_DefaultsToUser() {
        validRegisterRequest.setRole(null);

        String token = authService.register(validRegisterRequest);

        assertThat(token).isNotNull();
        User savedUser = userRepository.findByEmail("john@example.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getRole()).isEqualTo("USER");
    }

    // ================= ERROR CASES - REGISTRATION =================


    @Test
    void register_Fail_WhenEmailAlreadyExists() {
        authService.register(validRegisterRequest);

        assertThatThrownBy(() -> authService.register(validRegisterRequest))
                .isInstanceOf(Id39586DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }


    @Test
    void register_Fail_WhenNameIsEmpty() {
        Id39586RegisterRequest invalidRequest = new Id39586RegisterRequest();
        invalidRequest.setName("");
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("password123");

        assertThatThrownBy(() -> authService.register(invalidRequest))
                .isInstanceOf(Exception.class);
    }

    @Test
    void register_Fail_WhenNameIsNull() {
        Id39586RegisterRequest invalidRequest = new Id39586RegisterRequest();
        invalidRequest.setName(null);
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("password123");

        assertThatThrownBy(() -> authService.register(invalidRequest))
                .isInstanceOf(Exception.class);
    }


    @Test
    void register_Fail_WhenEmailIsInvalid() {
        Id39586RegisterRequest invalidRequest = new Id39586RegisterRequest();
        invalidRequest.setName("Test User");
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("password123");

        assertThatThrownBy(() -> authService.register(invalidRequest))
                .isInstanceOf(Exception.class);
    }


    @Test
    void register_Fail_WhenEmailIsNull() {
        Id39586RegisterRequest invalidRequest = new Id39586RegisterRequest();
        invalidRequest.setName("Test User");
        invalidRequest.setEmail(null);
        invalidRequest.setPassword("password123");

        assertThatThrownBy(() -> authService.register(invalidRequest))
                .isInstanceOf(Exception.class);
    }


    @Test
    void register_Fail_WhenPasswordTooShort() {
        Id39586RegisterRequest invalidRequest = new Id39586RegisterRequest();
        invalidRequest.setName("Test User");
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("123");

        assertThatThrownBy(() -> authService.register(invalidRequest))
                .isInstanceOf(Exception.class);
    }


    @Test
    void register_Fail_WhenPasswordIsNull() {
        Id39586RegisterRequest invalidRequest = new Id39586RegisterRequest();
        invalidRequest.setName("Test User");
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword(null);

        assertThatThrownBy(() -> authService.register(invalidRequest))
                .isInstanceOf(Exception.class);
    }

    // ================= ERROR CASES - LOGIN =================


    @Test
    void login_Fail_WhenUserNotFound() {
        assertThatThrownBy(() -> authService.login("nonexistent@example.com", "password123"))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("User not found");
    }


    @Test
    void login_Fail_WhenPasswordIsWrong() {
        authService.register(validRegisterRequest);

        assertThatThrownBy(() -> authService.login("john@example.com", "wrongpassword"))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Invalid password");
    }


    @Test
    void login_Fail_WhenEmailIsEmpty() {
        assertThatThrownBy(() -> authService.login("", "password123"))
                .isInstanceOf(Exception.class);
    }


    @Test
    void login_Fail_WhenEmailIsNull() {
        assertThatThrownBy(() -> authService.login(null, "password123"))
                .isInstanceOf(Exception.class);
    }


    @Test
    void login_Fail_WhenPasswordIsEmpty() {
        authService.register(validRegisterRequest);

        assertThatThrownBy(() -> authService.login("john@example.com", ""))
                .isInstanceOf(Exception.class);
    }


    @Test
    void login_Fail_WhenPasswordIsNull() {
        authService.register(validRegisterRequest);

        assertThatThrownBy(() -> authService.login("john@example.com", null))
                .isInstanceOf(Exception.class);
    }

    // ================= SEQUENCE / FLOW CASES =================


    @Test
    void fullAuthFlow_Success() throws Exception {
        // 1. Registration
        String registerToken = authService.register(validRegisterRequest);
        assertThat(registerToken).isNotNull();
        System.out.println("1. Registration successful");

        // 2. Checked DB
        User user = userRepository.findByEmail("john@example.com").orElse(null);
        assertThat(user).isNotNull();
        System.out.println("2. User found in database");

        // 3. Login
        String loginToken = authService.login("john@example.com", "password123");
        assertThat(loginToken).isNotNull();
        System.out.println("3. Login successful");

        // 4. Check password
        assertThat(passwordEncoder.matches("password123", user.getPassword())).isTrue();
        System.out.println("4. Password encrypted correctly");
    }


    @Test
    void authFlow_FailThenSuccess() throws Exception {
        // 1. Non-existent user
        assertThatThrownBy(() -> authService.login("new@example.com", "password123"))
                .isInstanceOf(Exception.class);
        System.out.println("1. Login with non-existent user - FAILED (expected)");

        // 2. Successful register
        authService.register(validRegisterRequest);
        System.out.println("2. Registration - SUCCESS");

        // 3. Login after registration
        String token = authService.login("john@example.com", "password123");
        assertThat(token).isNotNull();
        System.out.println("3. Login after registration - SUCCESS");
    }


    @Test
    void authFlow_WrongPasswordThenCorrect() throws Exception {
        authService.register(validRegisterRequest);

        // 1. Incorrect password - error
        assertThatThrownBy(() -> authService.login("john@example.com", "wrongpassword"))
                .isInstanceOf(Exception.class);
        System.out.println("1. Login with wrong password - FAILED (expected)");

        // 2. Successful password
        String token = authService.login("john@example.com", "password123");
        assertThat(token).isNotNull();
        System.out.println("2. Login with correct password - SUCCESS");
    }


    @Test
    void register_DuplicateEmail_Fail() {
        authService.register(validRegisterRequest);
        System.out.println("1. First registration - SUCCESS");

        assertThatThrownBy(() -> authService.register(validRegisterRequest))
                .isInstanceOf(Id39586DuplicateResourceException.class);
        System.out.println("2. Second registration with same email - FAILED (expected)");
    }


    @Test
    void register_MultipleDifferentUsers_Success() {
        // First user
        authService.register(validRegisterRequest);

        // Second user with different email
        Id39586RegisterRequest secondRequest = new Id39586RegisterRequest();
        secondRequest.setName("Jane Smith");
        secondRequest.setEmail("jane@example.com");
        secondRequest.setPassword("password456");
        secondRequest.setRole("USER");

        String secondToken = authService.register(secondRequest);

        assertThat(secondToken).isNotNull();
        assertThat(userRepository.findByEmail("john@example.com")).isPresent();
        assertThat(userRepository.findByEmail("jane@example.com")).isPresent();
    }


    @Test
    void password_IsEncrypted_BeforeSaving() {
        String rawPassword = validRegisterRequest.getPassword();

        authService.register(validRegisterRequest);

        User savedUser = userRepository.findByEmail("john@example.com").orElse(null);
        assertThat(savedUser).isNotNull();

        // Password in DB is not the raw password
        assertThat(savedUser.getPassword()).isNotEqualTo(rawPassword);

        // But password encoder can verify it
        assertThat(passwordEncoder.matches(rawPassword, savedUser.getPassword())).isTrue();
    }
}