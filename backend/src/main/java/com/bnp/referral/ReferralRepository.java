package com.bnp.referral;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReferralRepository extends JpaRepository<Referral, Long> {
    List<Referral> findByReferrerIdOrderByCreatedAtDesc(Long referrerId);
    List<Referral> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);
    long countByReferrerId(Long referrerId);
    long countByReceiverId(Long receiverId);
    void deleteByReferrerId(Long referrerId);
}
