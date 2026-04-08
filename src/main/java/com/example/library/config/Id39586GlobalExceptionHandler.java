package com.example.library.config;

import com.example.library.dto.response.Id39586ErrorResponse;
import com.example.library.entity.mongo.Id39586ErrorLog;
import com.example.library.exception.Id39586BusinessException;
import com.example.library.exception.Id39586DuplicateResourceException;
import com.example.library.exception.Id39586ResourceNotFoundException;
import com.example.library.service.Id39586MongoLoggingService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class Id39586GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(Id39586GlobalExceptionHandler.class);
    private final Id39586MongoLoggingService mongoLoggingService;

    public Id39586GlobalExceptionHandler(Id39586MongoLoggingService mongoLoggingService) {
        this.mongoLoggingService = mongoLoggingService;
    }

    // method collected
    private Id39586ErrorLog collectRequestInfo(HttpServletRequest request, Exception ex,
                                        int status, String errorType) {

        String userId = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            userId = auth.getName();
        }
    //Collected param
        Map<String, String> requestParams = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            requestParams.put(paramName, request.getParameter(paramName));
        }

        // collected headers
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (!"authorization".equalsIgnoreCase(headerName) &&
                        !"cookie".equalsIgnoreCase(headerName)) {
                    headers.put(headerName, request.getHeader(headerName));
                }
            }
        }

        Long startTime = (Long) request.getAttribute("startTime");
        Long executionTime = startTime != null ? System.currentTimeMillis() - startTime : null;

        return Id39586ErrorLog.builder()
                .errorId(UUID.randomUUID().toString())
                .errorType(errorType)
                .status(status)
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .method(request.getMethod())
                .timestamp(LocalDateTime.now())
                .stackTrace(getStackTraceString(ex))
                .requestParams(requestParams)
                .headers(headers)
                .clientIp(getClientIp(request))
                .userId(userId)
                .userAgent(request.getHeader("User-Agent"))
                .executionTimeMs(executionTime)
                .build();
    }

    private String getStackTraceString(Exception ex) {
        return Arrays.stream(ex.getStackTrace())
                .limit(20) // Ограничиваем количество строк
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @ExceptionHandler(Id39586ResourceNotFoundException.class)
    public ResponseEntity<Id39586ErrorResponse> handleResourceNotFound(
            Id39586ResourceNotFoundException ex, HttpServletRequest request) {

        logger.error("Resource not found: {}", ex.getMessage());

        Id39586ErrorLog errorLog = collectRequestInfo(request, ex, 404, "RESOURCE_NOT_FOUND");
        mongoLoggingService.saveErrorLog(errorLog);

        Id39586ErrorResponse error = new Id39586ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(Id39586DuplicateResourceException.class)
    public ResponseEntity<Id39586ErrorResponse> handleDuplicateResource(
            Id39586DuplicateResourceException ex, HttpServletRequest request) {

        logger.error("Duplicate resource: {}", ex.getMessage());

        Id39586ErrorLog errorLog = collectRequestInfo(request, ex, 409, "DUPLICATE_RESOURCE");
        mongoLoggingService.saveErrorLog(errorLog);

        Id39586ErrorResponse error = new Id39586ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Id39586BusinessException.class)
    public ResponseEntity<Id39586ErrorResponse> handleBusinessException(
            Id39586BusinessException ex, HttpServletRequest request) {

        logger.error("Business error: {}", ex.getMessage());

        Id39586ErrorLog errorLog = collectRequestInfo(request, ex, 400, "BUSINESS_ERROR");
        mongoLoggingService.saveErrorLog(errorLog);

        Id39586ErrorResponse error = new Id39586ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Id39586ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        logger.error("Data integrity violation: {}", ex.getMessage());

        String message = "Database constraint violation";
        if (ex.getMessage().contains("uk_email")) {
            message = "Email already exists";
        }

        Id39586ErrorLog errorLog = collectRequestInfo(request, ex, 409, "DATA_INTEGRITY_VIOLATION");
        mongoLoggingService.saveErrorLog(errorLog);

        Id39586ErrorResponse error = new Id39586ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Id39586ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<Id39586ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new Id39586ErrorResponse.ValidationError(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        Id39586ErrorLog errorLog = collectRequestInfo(request, ex, 400, "VALIDATION_ERROR");
        // Добавляем детали валидации в лог
        errorLog.setRequestParams(Map.of(
                "validationErrors",
                validationErrors.stream()
                        .map(e -> e.getField() + ": " + e.getMessage())
                        .collect(Collectors.joining(", "))
        ));
        mongoLoggingService.saveErrorLog(errorLog);

        Id39586ErrorResponse error = new Id39586ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                "Invalid input data",
                request.getRequestURI()
        );
        error.setValidationErrors(validationErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Id39586ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        logger.error("Unexpected error: ", ex);

        Id39586ErrorLog errorLog = collectRequestInfo(request, ex, 500, "UNEXPECTED_ERROR");
        mongoLoggingService.saveErrorLog(errorLog);

        Id39586ErrorResponse error = new Id39586ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}