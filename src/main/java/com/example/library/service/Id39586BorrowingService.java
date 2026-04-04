package com.example.library.service;

import com.example.library.dto.request.Id39586BorrowingCreateRequest;
import com.example.library.dto.response.Id39586BorrowingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface Id39586BorrowingService {
    Id39586BorrowingResponse createBorrowing(Id39586BorrowingCreateRequest request);
    Id39586BorrowingResponse returnBook(Long id);
    Id39586BorrowingResponse getBorrowing(Long id);
    Page<Id39586BorrowingResponse> getBorrowings(Long userId, Long bookId, String status, Pageable pageable);
    void deleteBorrowing(Long id);
}