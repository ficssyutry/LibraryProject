package com.example.library.repository;

import com.example.library.entity.Borrowing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Id39586BorrowingRepository extends JpaRepository<Borrowing, Long>, JpaSpecificationExecutor<Borrowing> {
    List<Borrowing> findByUserId(Long userId);
    List<Borrowing> findByBookId(Long bookId);
    boolean existsByBookIdAndUserIdAndStatus(Long bookId, Long userId, String status);
    List<Borrowing> findByStatus(String status);
}