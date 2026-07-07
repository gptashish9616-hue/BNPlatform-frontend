package com.bnp.subscription;

import com.bnp.common.enums.Enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByActiveTrue();
}

interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findFirstByUserIdAndStatusOrderByEndDateDesc(Long userId, SubscriptionStatus status);
    List<Subscription> findByUserIdOrderByCreatedAtDesc(Long userId);
    long countByStatus(SubscriptionStatus status);
}

interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    @org.springframework.data.jpa.repository.Query(
            "SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = com.bnp.common.enums.Enums$PaymentStatus.SUCCESS")
    java.math.BigDecimal totalRevenue();
}
