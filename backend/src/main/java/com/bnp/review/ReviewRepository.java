package com.bnp.review;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByTargetUserIdOrderByCreatedAtDesc(Long targetUserId);
    boolean existsByTargetUserIdAndAuthorId(Long targetUserId, Long authorId);
    void deleteByAuthorId(Long authorId);
}
