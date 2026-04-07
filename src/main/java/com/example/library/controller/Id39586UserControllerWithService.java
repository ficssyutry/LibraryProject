package com.example.library.controller;

import com.example.library.dto.request.Id39586UserCreateRequest;
import com.example.library.dto.request.Id39586UserUpdateRequest;
import com.example.library.dto.response.Id39586UserResponse;
import com.example.library.service.Id39586UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class Id39586UserControllerWithService {

    private final Id39586UserService userService;

    public Id39586UserControllerWithService(Id39586UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Id39586UserResponse> createUser(@Valid @RequestBody Id39586UserCreateRequest request) {
        Id39586UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Id39586UserResponse> getUser(@PathVariable Long id) {
        Id39586UserResponse response = userService.getUser(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<Id39586UserResponse>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<Id39586UserResponse> users = userService.getUsers(role, search, pageable);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Id39586UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody Id39586UserUpdateRequest request) {

        Id39586UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}