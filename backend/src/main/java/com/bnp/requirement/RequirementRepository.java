package com.bnp.requirement;

import com.bnp.common.enums.Enums.RequirementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequirementRepository extends JpaRepository<Requirement, Long> {
    List<Requirement> findByPosterIdOrderByCreatedAtDesc(Long posterId);
    List<Requirement> findByStatusOrderByCreatedAtDesc(RequirementStatus status);
    List<Requirement> findAllByOrderByCreatedAtDesc();
    void deleteByPosterId(Long posterId);
}
