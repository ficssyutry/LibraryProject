package com.example.library.controller;

import com.example.library.dto.request.Id39586BorrowingCreateRequest;
import com.example.library.dto.response.Id39586BorrowingResponse;
import com.example.library.service.Id39586BorrowingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrowings")
public class Id39586BorrowingController {

    private final Id39586BorrowingService borrowingService;

    public Id39586BorrowingController(Id39586BorrowingService borrowingService) {
        this.borrowingService = borrowingService;
    }

    @PostMapping
    public ResponseEntity<Id39586BorrowingResponse> createBorrowing(@Valid @RequestBody Id39586BorrowingCreateRequest request) {
        Id39586BorrowingResponse response = borrowingService.createBorrowing(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<Id39586BorrowingResponse> returnBook(@PathVariable Long id) {
        Id39586BorrowingResponse response = borrowingService.returnBook(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Id39586BorrowingResponse> getBorrowing(@PathVariable Long id) {
        Id39586BorrowingResponse response = borrowingService.getBorrowing(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<Id39586BorrowingResponse>> getBorrowings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Id39586BorrowingResponse> borrowings = borrowingService.getBorrowings(userId, bookId, status, pageable);
        return ResponseEntity.ok(borrowings);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBorrowing(@PathVariable Long id) {
        borrowingService.deleteBorrowing(id);
        return ResponseEntity.noContent().build();
    }
}