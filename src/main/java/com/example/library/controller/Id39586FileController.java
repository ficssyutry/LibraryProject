package com.example.library.controller;

import com.example.library.dto.response.Id39586FileResponse;
import com.example.library.service.Id39586FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class Id39586FileController {

    private final Id39586FileStorageService fileStorageService;

    public Id39586FileController(Id39586FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    // ================= UPLOAD =================

    @PostMapping("/upload")
    public ResponseEntity<Id39586FileResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category,
            @RequestParam(value = "entityId", required = false) String entityId,
            @RequestParam(value = "description", required = false) String description,
            HttpServletRequest request) {

        String uploadedBy = SecurityContextHolder.getContext().getAuthentication().getName();

        Id39586FileResponse response = fileStorageService.uploadFile(
                file, category, entityId, description, uploadedBy
        );

        return ResponseEntity.ok(response);
    }

    // ================= DOWNLOAD =================

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        GridFsResource resource = fileStorageService.downloadFile(fileId);

        String encodedFilename = URLEncoder.encode(
                resource.getFilename(),
                StandardCharsets.UTF_8
        ).replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.parseMediaType(resource.getContentType()))
                .body(resource);
    }

    // ================= GET INFO =================

    @GetMapping("/info/{fileId}")
    public ResponseEntity<Map<String, Object>> getFileInfo(@PathVariable String fileId) {
        Map<String, Object> info = fileStorageService.getFileInfo(fileId);
        return ResponseEntity.ok(info);
    }

    // ================= LIST BY ENTITY =================

    @GetMapping("/entity/{entityId}")
    public ResponseEntity<List<Id39586FileResponse>> getFilesByEntity(@PathVariable String entityId) {
        List<Id39586FileResponse> files = fileStorageService.getFilesByEntity(entityId);
        return ResponseEntity.ok(files);
    }

    // ================= DELETE =================

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileId) {
        fileStorageService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    // ================= PREVIEW (for picture) =================

    @GetMapping("/preview/{fileId}")
    public ResponseEntity<Resource> previewFile(@PathVariable String fileId) {
        GridFsResource resource = fileStorageService.downloadFile(fileId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(resource.getContentType()))
                .body(resource);
    }
}