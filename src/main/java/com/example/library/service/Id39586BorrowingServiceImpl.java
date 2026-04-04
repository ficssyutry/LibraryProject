package com.example.library.service;

import com.example.library.dto.request.Id39586BorrowingCreateRequest;
import com.example.library.dto.response.Id39586BorrowingResponse;
import com.example.library.entity.Book;
import com.example.library.entity.Borrowing;
import com.example.library.entity.User;
import com.example.library.exception.Id39586BusinessException;
import com.example.library.exception.Id39586ResourceNotFoundException;
import com.example.library.repository.Id39586BookRepository;
import com.example.library.repository.Id39586BorrowingRepository;
import com.example.library.repository.Id39586UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class Id39586BorrowingServiceImpl implements Id39586BorrowingService {

    private static final Logger logger = LoggerFactory.getLogger(Id39586BorrowingServiceImpl.class);

    private final Id39586BorrowingRepository borrowingRepository;
    private final Id39586UserRepository userRepository;
    private final Id39586BookRepository bookRepository;
    private final Id39586AsyncNotificationService asyncNotificationService;

    public Id39586BorrowingServiceImpl(Id39586BorrowingRepository borrowingRepository,
                                Id39586UserRepository userRepository,
                                Id39586BookRepository bookRepository,
                                Id39586AsyncNotificationService asyncNotificationService) {
        this.borrowingRepository = borrowingRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.asyncNotificationService = asyncNotificationService;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"books", "borrowings"}, allEntries = true)
    public Id39586BorrowingResponse createBorrowing(Id39586BorrowingCreateRequest request) {
        logger.info("Creating borrowing for user: {}, book: {}", request.getUserId(), request.getBookId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new Id39586ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new Id39586ResourceNotFoundException("Book not found with id: " + request.getBookId()));

        if (!book.getAvailable()) {
            throw new Id39586BusinessException("Book '" + book.getTitle() + "' is not available for borrowing");
        }

        if (borrowingRepository.existsByBookIdAndUserIdAndStatus(book.getId(), user.getId(), "ACTIVE")) {
            throw new Id39586BusinessException("User already has an active borrowing for this book");
        }

        Borrowing borrowing = new Borrowing();
        borrowing.setUser(user);
        borrowing.setBook(book);
        borrowing.setBorrowDate(LocalDate.now());
        borrowing.setDueDate(LocalDate.now().plusDays(14));

        book.setAvailable(false);
        bookRepository.save(book);

        Borrowing saved = borrowingRepository.save(borrowing);
        logger.info("Borrowing created successfully with id: {}", saved.getId());

        Id39586BorrowingResponse response = mapToResponse(saved);

        asyncNotificationService.sendAccountUpdateNotificationAsync(
                mapUserToResponse(user), "Borrowed book: " + book.getTitle()
        );
        asyncNotificationService.logUserActionAsync(
                user.getId(), "CREATE_BORROWING", "Borrowed book: " + book.getTitle()
        );

        return response;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"books", "borrowings"}, key = "#id")
    public Id39586BorrowingResponse returnBook(Long id) {
        logger.info("Returning book for borrowing id: {}", id);

        Borrowing borrowing = borrowingRepository.findById(id)
                .orElseThrow(() -> new Id39586ResourceNotFoundException("Borrowing not found with id: " + id));

        if ("RETURNED".equals(borrowing.getStatus())) {
            throw new Id39586BusinessException("Book already returned");
        }

        Book book = borrowing.getBook();
        book.setAvailable(true);
        bookRepository.save(book);

        borrowing.setReturnDate(LocalDate.now());
        borrowing.setStatus("RETURNED");

        Borrowing saved = borrowingRepository.save(borrowing);
        logger.info("Book returned successfully for borrowing id: {}", id);

        Id39586BorrowingResponse response = mapToResponse(saved);

        asyncNotificationService.sendAccountUpdateNotificationAsync(
                mapUserToResponse(borrowing.getUser()), "Returned book: " + book.getTitle()
        );
        asyncNotificationService.logUserActionAsync(
                borrowing.getUser().getId(), "RETURN_BOOK", "Returned book: " + book.getTitle()
        );

        return response;
    }

    @Override
    @Cacheable(value = "borrowings", key = "#id")
    public Id39586BorrowingResponse getBorrowing(Long id) {
        logger.info("Fetching borrowing from DATABASE: id={}", id);

        Borrowing borrowing = borrowingRepository.findById(id)
                .orElseThrow(() -> new Id39586ResourceNotFoundException("Borrowing not found with id: " + id));

        return mapToResponse(borrowing);
    }

    @Override
    public Page<Id39586BorrowingResponse> getBorrowings(Long userId, Long bookId, String status, Pageable pageable) {
        logger.info("Fetching borrowings: userId={}, bookId={}, status={}", userId, bookId, status);

        Specification<Borrowing> spec = (root, query, cb) -> cb.conjunction();

        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));
        }

        if (bookId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("book").get("id"), bookId));
        }

        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        return borrowingRepository.findAll(spec, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "borrowings", key = "#id")
    public void deleteBorrowing(Long id) {
        logger.info("Deleting borrowing with id: {}", id);

        Borrowing borrowing = borrowingRepository.findById(id)
                .orElseThrow(() -> new Id39586ResourceNotFoundException("Borrowing not found with id: " + id));

        if (!"RETURNED".equals(borrowing.getStatus())) {
            Book book = borrowing.getBook();
            book.setAvailable(true);
            bookRepository.save(book);
        }

        borrowingRepository.deleteById(id);
        logger.info("Borrowing deleted successfully with id: {}", id);
    }

    private Id39586BorrowingResponse mapToResponse(Borrowing borrowing) {
        Id39586BorrowingResponse response = new Id39586BorrowingResponse();
        response.setId(borrowing.getId());
        response.setUserId(borrowing.getUser().getId());
        response.setUserName(borrowing.getUser().getName());
        response.setBookId(borrowing.getBook().getId());
        response.setBookTitle(borrowing.getBook().getTitle());
        response.setBookAuthor(borrowing.getBook().getAuthor());
        response.setBorrowDate(borrowing.getBorrowDate());
        response.setDueDate(borrowing.getDueDate());
        response.setReturnDate(borrowing.getReturnDate());
        response.setStatus(borrowing.getStatus());
        return response;
    }

    private com.example.library.dto.response.Id39586UserResponse mapUserToResponse(User user) {
        com.example.library.dto.response.Id39586UserResponse response =
                new com.example.library.dto.response.Id39586UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        return response;
    }
}