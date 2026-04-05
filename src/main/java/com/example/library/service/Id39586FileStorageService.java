package com.example.library.service;

import com.example.library.dto.response.Id39586FileResponse;
import com.example.library.entity.mongo.Id39586FileMetadata;
import com.example.library.exception.Id39586FileStorageException;
import com.example.library.repository.mongo.Id39586FileMetadataRepository;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class Id39586FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(Id39586FileStorageService.class);

    private final GridFsTemplate gridFsTemplate;
    private final GridFsOperations gridFsOperations;
    private final Id39586FileMetadataRepository fileMetadataRepository;
    private final Id39586AsyncNotificationService asyncNotificationService;

    @Value("${file.max-size:10485760}")
    private long maxFileSize;

    @Value("${file.allowed-types:image/jpeg,image/png,image/jpg,application/pdf,text/plain}")
    private List<String> allowedContentTypes;

    public Id39586FileStorageService(GridFsTemplate gridFsTemplate,
                              GridFsOperations gridFsOperations,
                              Id39586FileMetadataRepository fileMetadataRepository,
                              Id39586AsyncNotificationService asyncNotificationService) {
        this.gridFsTemplate = gridFsTemplate;
        this.gridFsOperations = gridFsOperations;
        this.fileMetadataRepository = fileMetadataRepository;
        this.asyncNotificationService = asyncNotificationService;
    }

    // ================= UPLOAD =================

    @CacheEvict(value = "files", allEntries = true)
    public Id39586FileResponse uploadFile(MultipartFile file, String category,
                                   String entityId, String description,
                                   String uploadedBy) {

        validateFile(file);

        try {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("category", category);
            metadata.put("entityId", entityId != null ? entityId : "");
            metadata.put("uploadedBy", uploadedBy);
            metadata.put("originalName", file.getOriginalFilename());
            metadata.put("description", description != null ? description : "");

            ObjectId fileId = gridFsTemplate.store(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    metadata
            );

            Id39586FileMetadata fileMetadata = new Id39586FileMetadata();
            fileMetadata.setFileId(fileId.toString());
            fileMetadata.setOriginalFileName(file.getOriginalFilename());
            fileMetadata.setStoredFileName(fileId.toString());
            fileMetadata.setContentType(file.getContentType());
            fileMetadata.setFileSize(file.getSize());
            fileMetadata.setCategory(category);
            fileMetadata.setEntityId(entityId);
            fileMetadata.setDescription(description);
            fileMetadata.setUploadedBy(uploadedBy);
            fileMetadata.setUploadDate(LocalDateTime.now());

            fileMetadataRepository.save(fileMetadata);

            logger.info("File uploaded successfully: {} with ID: {}",
                    file.getOriginalFilename(), fileId);

            asyncNotificationService.logUserActionAsync(
                    null, "FILE_UPLOAD",
                    String.format("File uploaded: %s, category: %s, size: %d bytes",
                            file.getOriginalFilename(), category, file.getSize())
            );

            return new Id39586FileResponse(
                    fileId.toString(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    category,
                    entityId
            );

        } catch (IOException e) {
            logger.error("Failed to upload file: {}", e.getMessage());
            throw new Id39586FileStorageException("Failed to upload file: " + e.getMessage());
        }
    }

    // ================= DOWNLOAD =================

    public GridFsResource downloadFile(String fileId) {
        try {
            GridFSFile gridFSFile = gridFsTemplate.findOne(
                    Query.query(Criteria.where("_id").is(new ObjectId(fileId)))
            );

            if (gridFSFile == null) {
                throw new Id39586FileStorageException("File not found with ID: " + fileId);
            }

            return gridFsOperations.getResource(gridFSFile);

        } catch (Exception e) {
            logger.error("Failed to download file: {}", e.getMessage());
            throw new Id39586FileStorageException("Failed to download file: " + e.getMessage());
        }
    }

    // ================= DELETE =================

    @CacheEvict(value = "files", key = "#fileId")
    public void deleteFile(String fileId) {
        try {
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(new ObjectId(fileId))));
            fileMetadataRepository.deleteByFileId(fileId);

            logger.info("File deleted successfully: {}", fileId);

            asyncNotificationService.logUserActionAsync(
                    null, "FILE_DELETE",
                    "File deleted: " + fileId
            );

        } catch (Exception e) {
            logger.error("Failed to delete file: {}", e.getMessage());
            throw new Id39586FileStorageException("Failed to delete file: " + e.getMessage());
        }
    }

    // ================= GET FILE INFO =================

    @Cacheable(value = "files", key = "#fileId")
    public Map<String, Object> getFileInfo(String fileId) {
        logger.info("Fetching file info from DATABASE (not cache): fileId={}", fileId);

        try {
            GridFSFile gridFSFile = gridFsTemplate.findOne(
                    Query.query(Criteria.where("_id").is(new ObjectId(fileId)))
            );

            if (gridFSFile == null) {
                throw new Id39586FileStorageException("File not found with ID: " + fileId);
            }

            Optional<Id39586FileMetadata> metadata = fileMetadataRepository.findByFileId(fileId);

            Map<String, Object> info = new HashMap<>();
            info.put("fileId", fileId);
            info.put("filename", gridFSFile.getFilename());
            info.put("contentType", gridFSFile.getMetadata().get("_contentType"));
            info.put("size", gridFSFile.getLength());
            info.put("uploadDate", gridFSFile.getUploadDate());

            if (metadata.isPresent()) {
                info.put("category", metadata.get().getCategory());
                info.put("entityId", metadata.get().getEntityId());
                info.put("uploadedBy", metadata.get().getUploadedBy());
                info.put("description", metadata.get().getDescription());
            }

            return info;

        } catch (Exception e) {
            logger.error("Failed to get file info: {}", e.getMessage());
            throw new Id39586FileStorageException("Failed to get file info: " + e.getMessage());
        }
    }

    // ================= LIST FILES =================

    public List<Id39586FileResponse> getFilesByEntity(String entityId) {
        List<Id39586FileMetadata> metadataList = fileMetadataRepository.findByEntityId(entityId);

        return metadataList.stream()
                .map(meta -> new Id39586FileResponse(
                        meta.getFileId(),
                        meta.getOriginalFileName(),
                        meta.getContentType(),
                        meta.getFileSize(),
                        meta.getCategory(),
                        meta.getEntityId()
                ))
                .toList();
    }

    // ================= VALIDATION =================

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new Id39586FileStorageException("Cannot upload empty file");
        }

        if (file.getSize() > maxFileSize) {
            throw new Id39586FileStorageException(
                    String.format("File size exceeds maximum allowed: %d bytes (max: %d)",
                            file.getSize(), maxFileSize)
            );
        }

        String contentType = file.getContentType();
        if (!allowedContentTypes.contains(contentType)) {
            throw new Id39586FileStorageException(
                    String.format("File type %s is not allowed. Allowed types: %s",
                            contentType, allowedContentTypes)
            );
        }
    }
}