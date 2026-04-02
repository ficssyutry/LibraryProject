package com.example.library.repository.mongo;

import com.example.library.entity.mongo.Id39586FileMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface Id39586FileMetadataRepository extends MongoRepository<Id39586FileMetadata, String> {

    Optional<Id39586FileMetadata> findByFileId(String fileId);

    Page<Id39586FileMetadata> findByCategory(String category, Pageable pageable);

    List<Id39586FileMetadata> findByEntityId(String entityId);

    Optional<Id39586FileMetadata> findByCategoryAndEntityId(String category, String entityId);

    Page<Id39586FileMetadata> findByUploadedBy(String uploadedBy, Pageable pageable);

    void deleteByFileId(String fileId);

    List<Id39586FileMetadata> findByEntityIdAndIsActiveTrue(String entityId);

    // Сложный поиск
    @Query("{ $and: [ " +
            "{ 'category': ?0 }, " +
            "{ 'uploadedBy': ?1 }, " +
            "{ 'uploadDate': { $gte: ?2 } } " +
            "] }")
    List<Id39586FileMetadata> findFilesByCategoryAndUserAfterDate(
            String category, String uploadedBy, LocalDateTime afterDate
    );
}