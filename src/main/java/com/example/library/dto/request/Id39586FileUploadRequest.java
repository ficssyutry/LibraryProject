package com.example.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class Id39586FileUploadRequest {

    @NotNull(message = "File is required")
    private MultipartFile file;

    @NotBlank(message = "Category is required")
    private String category;

    private String entityId;

    private String description;

    // Getters and Setters
    public MultipartFile getFile() { return file; }
    public void setFile(MultipartFile file) { this.file = file; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}