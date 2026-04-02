package com.example.library.entity.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "error_logs")
public class Id39586ErrorLog {

    @Id
    private String id;

    @Indexed
    private String errorId;          //  ID я

    @Indexed
    private String errorType;         // type error

    private int status;               // HTTP status

    private String message;

    private String path;

    private String method;            // HTTP method(GET, POST и т.д.)

    @Indexed
    private LocalDateTime timestamp;

    private String stackTrace;

    private Map<String, String> requestParams;

    private Map<String, String> headers;

    private String clientIp;

    private String userId;

    private String userAgent;

    private Long executionTimeMs;

    // Constructors
    public Id39586ErrorLog() {}

    // Builder pattern
    public static ErrorLogBuilder builder() {
        return new ErrorLogBuilder();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getErrorId() { return errorId; }
    public void setErrorId(String errorId) { this.errorId = errorId; }

    public String getErrorType() { return errorType; }
    public void setErrorType(String errorType) { this.errorType = errorType; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getStackTrace() { return stackTrace; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }

    public Map<String, String> getRequestParams() { return requestParams; }
    public void setRequestParams(Map<String, String> requestParams) { this.requestParams = requestParams; }

    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }

    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public Long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    // Builder Pattern
    public static class ErrorLogBuilder {
        private Id39586ErrorLog errorLog = new Id39586ErrorLog();

        public ErrorLogBuilder errorId(String errorId) {
            errorLog.setErrorId(errorId);
            return this;
        }

        public ErrorLogBuilder errorType(String errorType) {
            errorLog.setErrorType(errorType);
            return this;
        }

        public ErrorLogBuilder status(int status) {
            errorLog.setStatus(status);
            return this;
        }

        public ErrorLogBuilder message(String message) {
            errorLog.setMessage(message);
            return this;
        }

        public ErrorLogBuilder path(String path) {
            errorLog.setPath(path);
            return this;
        }

        public ErrorLogBuilder method(String method) {
            errorLog.setMethod(method);
            return this;
        }

        public ErrorLogBuilder timestamp(LocalDateTime timestamp) {
            errorLog.setTimestamp(timestamp);
            return this;
        }

        public ErrorLogBuilder stackTrace(String stackTrace) {
            if (stackTrace != null && stackTrace.length() > 10000) {
                stackTrace = stackTrace.substring(0, 10000) + "... [truncated]";
            }
            errorLog.setStackTrace(stackTrace);
            return this;
        }

        public ErrorLogBuilder requestParams(Map<String, String> requestParams) {
            errorLog.setRequestParams(requestParams);
            return this;
        }

        public ErrorLogBuilder headers(Map<String, String> headers) {
            if (headers != null) {
                headers.remove("authorization");
                headers.remove("cookie");
            }
            errorLog.setHeaders(headers);
            return this;
        }

        public ErrorLogBuilder clientIp(String clientIp) {
            errorLog.setClientIp(clientIp);
            return this;
        }

        public ErrorLogBuilder userId(String userId) {
            errorLog.setUserId(userId);
            return this;
        }

        public ErrorLogBuilder userAgent(String userAgent) {
            errorLog.setUserAgent(userAgent);
            return this;
        }

        public ErrorLogBuilder executionTimeMs(Long executionTimeMs) {
            errorLog.setExecutionTimeMs(executionTimeMs);
            return this;
        }

        public Id39586ErrorLog build() {
            if (errorLog.getTimestamp() == null) {
                errorLog.setTimestamp(LocalDateTime.now());
            }
            return errorLog;
        }
    }
}