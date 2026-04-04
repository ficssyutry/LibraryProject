package com.example.library.service;

import com.example.library.dto.request.Id39586BookCreateRequest;
import com.example.library.dto.request.Id39586BookUpdateRequest;
import com.example.library.dto.response.Id39586BookResponse;
import com.example.library.entity.Book;
import com.example.library.entity.mongo.Id39586BookDocument;
import com.example.library.exception.Id39586DuplicateResourceException;
import com.example.library.exception.Id39586ResourceNotFoundException;
import com.example.library.repository.Id39586BookRepository;
import com.example.library.repository.mongo.Id39586BookMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Id39586BookServiceImpl implements Id39586BookService {

    private static final Logger logger = LoggerFactory.getLogger(Id39586BookServiceImpl.class);

    private final Id39586BookRepository bookRepository;
    private final Id39586BookMongoRepository bookMongoRepository;
    private final Id39586AsyncNotificationService asyncNotificationService;

    public Id39586BookServiceImpl(Id39586BookRepository bookRepository,
                           Id39586BookMongoRepository bookMongoRepository,
                           Id39586AsyncNotificationService asyncNotificationService) {
        this.bookRepository = bookRepository;
        this.bookMongoRepository = bookMongoRepository;
        this.asyncNotificationService = asyncNotificationService;
    }

    // ================= POSTGRESQL =================

    @Override
    @Transactional
    @CacheEvict(value = "books", allEntries = true)
    public Id39586BookResponse createBook(Id39586BookCreateRequest request) {
        logger.info("Creating book with title: {}", request.getTitle());

        if (bookRepository.existsByTitle(request.getTitle())) {
            throw new Id39586DuplicateResourceException("Book with title '" + request.getTitle() + "' already exists");
        }

        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setGenre(request.getGenre());
        book.setIsbn(request.getIsbn());
        book.setPublicationYear(request.getPublicationYear());
        book.setDescription(request.getDescription());
        book.setAvailable(request.getAvailable() != null ? request.getAvailable() : true);

        Book saved = bookRepository.save(book);
        logger.info("Book created successfully with id: {}", saved.getId());

        Id39586BookResponse response = mapToResponse(saved);

        asyncNotificationService.logUserActionAsync(
                null, "CREATE_BOOK", "Book created: " + saved.getTitle()
        );

        return response;
    }

    @Override
    @Cacheable(value = "books", key = "#id")
    public Id39586BookResponse getBook(Long id) {
        logger.info("Fetching book from DATABASE (not cache): id={}", id);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new Id39586ResourceNotFoundException("Book not found with id: " + id));

        return mapToResponse(book);
    }

    @Override
    @Transactional
    @CachePut(value = "books", key = "#id")
    public Id39586BookResponse updateBook(Long id, Id39586BookUpdateRequest request) {
        logger.info("Updating book with id: {}", id);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new Id39586ResourceNotFoundException("Book not found with id: " + id));

        if (request.getTitle() != null && !request.getTitle().equals(book.getTitle())) {
            if (bookRepository.existsByTitle(request.getTitle())) {
                throw new Id39586DuplicateResourceException("Book with title '" + request.getTitle() + "' already exists");
            }
            book.setTitle(request.getTitle());
        }

        if (request.getAuthor() != null) {
            book.setAuthor(request.getAuthor());
        }

        if (request.getGenre() != null) {
            book.setGenre(request.getGenre());
        }

        if (request.getIsbn() != null) {
            book.setIsbn(request.getIsbn());
        }

        if (request.getPublicationYear() != null) {
            book.setPublicationYear(request.getPublicationYear());
        }

        if (request.getDescription() != null) {
            book.setDescription(request.getDescription());
        }

        if (request.getAvailable() != null) {
            book.setAvailable(request.getAvailable());
        }

        Book updated = bookRepository.save(book);
        logger.info("Book updated successfully with id: {}", id);

        Id39586BookResponse response = mapToResponse(updated);

        asyncNotificationService.logUserActionAsync(
                null, "UPDATE_BOOK", "Book updated: " + updated.getTitle()
        );

        return response;
    }

    @Override
    @Transactional
    @CacheEvict(value = "books", key = "#id")
    public void deleteBook(Long id) {
        logger.info("Deleting book with id: {}", id);

        if (!bookRepository.existsById(id)) {
            throw new Id39586ResourceNotFoundException("Book not found with id: " + id);
        }

        bookRepository.deleteById(id);
        logger.info("Book deleted successfully with id: {}", id);

        asyncNotificationService.logUserActionAsync(
                null, "DELETE_BOOK", "Book deleted with id: " + id
        );
    }

    @Override
    @Cacheable(value = "books", key = "'list_' + #genre + '_' + #search + '_' + #pageable.pageNumber")
    public Page<Id39586BookResponse> getBooks(String genre, String search, Pageable pageable) {
        logger.info("Fetching books from DATABASE: genre={}, search={}", genre, search);

        Specification<Book> spec = (root, query, cb) -> cb.conjunction();

        if (genre != null && !genre.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("genre"), genre));
        }

        if (search != null && !search.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("author")), "%" + search.toLowerCase() + "%")
                    ));
        }

        return bookRepository.findAll(spec, pageable)
                .map(this::mapToResponse);
    }

    // ================= MONGODB =================

    @Override
    @CacheEvict(value = "books", allEntries = true)
    public void saveBookToMongo(Id39586BookCreateRequest request) {
        logger.info("Saving book to MongoDB: {}", request.getTitle());

        Id39586BookDocument doc = new Id39586BookDocument();
        doc.setTitle(request.getTitle());
        doc.setAuthor(request.getAuthor());
        doc.setGenre(request.getGenre());
        doc.setAvailable(request.getAvailable() != null ? request.getAvailable() : true);

        bookMongoRepository.save(doc);
        logger.info("Book saved to MongoDB with id: {}", doc.getId());

        asyncNotificationService.logUserActionAsync(
                null, "SAVE_TO_MONGO", "Book saved to MongoDB: " + request.getTitle()
        );
    }

    @Override
    public Page<Id39586BookResponse> getBooksFromMongo(Pageable pageable) {
        logger.info("Fetching books from MongoDB");

        return bookMongoRepository.findAll(pageable)
                .map(this::mapToResponseMongo);
    }

    // ================= MAPPERS =================

    private Id39586BookResponse mapToResponse(Book book) {
        Id39586BookResponse response = new Id39586BookResponse();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setGenre(book.getGenre());
        response.setIsbn(book.getIsbn());
        response.setAvailable(book.getAvailable());
        response.setPublicationYear(book.getPublicationYear());
        response.setDescription(book.getDescription());
        return response;
    }

    private Id39586BookResponse mapToResponseMongo(Id39586BookDocument doc) {
        Id39586BookResponse response = new Id39586BookResponse();
        response.setTitle(doc.getTitle());
        response.setAuthor(doc.getAuthor());
        response.setGenre(doc.getGenre());
        response.setAvailable(doc.getAvailable());
        return response;
    }
}