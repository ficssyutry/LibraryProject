package com.example.library.service;

import com.example.library.dto.request.Id39586BookCreateRequest;
import com.example.library.dto.request.Id39586BookUpdateRequest;
import com.example.library.dto.response.Id39586BookResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface Id39586BookService {
    Id39586BookResponse createBook(Id39586BookCreateRequest request);
    Id39586BookResponse getBook(Long id);
    Id39586BookResponse updateBook(Long id, Id39586BookUpdateRequest request);
    void deleteBook(Long id);
    Page<Id39586BookResponse> getBooks(String genre, String search, Pageable pageable);
    void saveBookToMongo(Id39586BookCreateRequest request);
    Page<Id39586BookResponse> getBooksFromMongo(Pageable pageable);
}