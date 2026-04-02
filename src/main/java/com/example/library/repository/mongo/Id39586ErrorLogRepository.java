package com.example.library.repository;

import com.example.library.entity.mongo.Id39586ErrorLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface Id39586ErrorLogRepository extends MongoRepository<Id39586ErrorLog, String> {

    // time error
    Page<Id39586ErrorLog> findByErrorType(String errorType, Pageable pageable);

    // search timemark
    List<Id39586ErrorLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    // Search status
    Page<Id39586ErrorLog> findByStatus(int status, Pageable pageable);

    // Search path
    Page<Id39586ErrorLog> findByPathContaining(String path, Pageable pageable);

    // Search IP client
    Page<Id39586ErrorLog> findByClientIp(String clientIp, Pageable pageable);

    @Query(value = "{ 'timestamp': { $gte: ?0 } }",
            count = true,
            fields = "{ 'errorType': 1 }")
    long countByErrorTypeAfter(LocalDateTime timestamp);

    @Query("{ $and: [ " +
            "{ 'errorType': ?0 }, " +
            "{ 'status': ?1 }, " +
            "{ 'timestamp': { $gte: ?2 } } " +
            "] }")
    List<Id39586ErrorLog> findErrorsByTypeAndStatusAndAfterDate(
            String errorType,
            int status,
            LocalDateTime afterDate
    );
}