package com.example.library.dto.response;

import java.time.LocalDateTime;

public class Id39586FileResponse {
    private String fileId;
    private String fileName;
    private String fileUrl;
    private String contentType;
    private long fileSize;
    private String category;
    private String entityId;
    private LocalDateTime uploadDate;
    private String downloadUrl;

    public Id39586FileResponse() {}

    public Id39586FileResponse(String fileId, String fileName, String contentType,
                        long fileSize, String category, String entityId) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.category = category;
        this.entityId = entityId;
        this.uploadDate = LocalDateTime.now();
        this.downloadUrl = "/api/files/download/" + fileId;
    }

    // Getters and Setters
    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
}