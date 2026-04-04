package com.example.library.service;

import com.example.library.dto.request.Id39586UserCreateRequest;
import com.example.library.dto.request.Id39586UserUpdateRequest;
import com.example.library.dto.response.Id39586UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface Id39586UserService {

    Id39586UserResponse createUser(Id39586UserCreateRequest request);

    Id39586UserResponse getUser(Long id);

    Page<Id39586UserResponse> getUsers(
            String role,
            String search,
            Pageable pageable
    );

    void deleteUser(Long id);

    Id39586UserResponse updateUser(Long id, Id39586UserUpdateRequest request);
}