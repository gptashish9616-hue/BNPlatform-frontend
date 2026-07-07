package com.bnp.requirement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequirementResponseRepository extends JpaRepository<RequirementResponse, Long> {
    List<RequirementResponse> findByRequirementIdOrderByCreatedAtDesc(Long requirementId);
    List<RequirementResponse> findByResponderIdOrderByCreatedAtDesc(Long responderId);
    void deleteByRequirementIdIn(List<Long> requirementIds);
    void deleteByResponderId(Long responderId);
}
