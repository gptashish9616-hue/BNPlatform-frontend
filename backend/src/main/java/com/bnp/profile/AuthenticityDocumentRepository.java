package com.bnp.profile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthenticityDocumentRepository extends JpaRepository<AuthenticityDocument, Long> {
    List<AuthenticityDocument> findByUserId(Long userId);
}
