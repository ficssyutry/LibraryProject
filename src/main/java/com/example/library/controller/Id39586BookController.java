package com.example.library.controller;

import com.example.library.dto.request.Id39586BookCreateRequest;
import com.example.library.dto.request.Id39586BookUpdateRequest;
import com.example.library.dto.response.Id39586BookResponse;
import com.example.library.service.Id39586BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class Id39586BookController {

    private final Id39586BookService bookService;

    public Id39586BookController(Id39586BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<Id39586BookResponse> createBook(@Valid @RequestBody Id39586BookCreateRequest request) {
        Id39586BookResponse response = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Id39586BookResponse> getBook(@PathVariable Long id) {
        Id39586BookResponse response = bookService.getBook(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Id39586BookResponse> updateBook(@PathVariable Long id,
                                                   @Valid @RequestBody Id39586BookUpdateRequest request) {
        Id39586BookResponse response = bookService.updateBook(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<Id39586BookResponse>> getBooks(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Id39586BookResponse> books = bookService.getBooks(genre, search, pageable);
        return ResponseEntity.ok(books);
    }

    @PostMapping("/mongo")
    public ResponseEntity<Void> saveToMongo(@Valid @RequestBody Id39586BookCreateRequest request) {
        bookService.saveBookToMongo(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/mongo")
    public ResponseEntity<Page<Id39586BookResponse>> getFromMongo(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(bookService.getBooksFromMongo(pageable));
    }
}