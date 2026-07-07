package com.bnp.subscription;

import com.bnp.common.enums.Enums.BillingCycle;
import com.bnp.common.enums.Enums.NotificationType;
import com.bnp.common.enums.Enums.PaymentStatus;
import com.bnp.common.enums.Enums.SubscriptionStatus;
import com.bnp.common.enums.Role;
import com.bnp.common.exception.ResourceNotFoundException;
import com.bnp.notification.NotificationService;
import com.bnp.user.User;
import com.bnp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public List<Plan> activePlans() {
        return planRepository.findByActiveTrue();
    }

    public Subscription current(Long userId) {
        return subscriptionRepository
                .findFirstByUserIdAndStatusOrderByEndDateDesc(userId, SubscriptionStatus.ACTIVE)
                .orElse(null);
    }

    public List<Subscription> history(Long userId) {
        return subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Payment> payments(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /** Subscribe to a plan. Payment is mocked as successful (real gateway = Phase 4). */
    @Transactional
    public Subscription subscribe(Long userId, Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", planId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plusMonths(monthsFor(plan.getBillingCycle()));

        // Redeem any free months the user earned through accepted invites
        int freeMonths = user.getFreeMonthsEarned() == null ? 0 : user.getFreeMonthsEarned();
        if (freeMonths > 0) {
            end = end.plusMonths(freeMonths);
            user.setFreeMonthsEarned(0);
        }

        Subscription subscription = subscriptionRepository.save(Subscription.builder()
                .userId(userId)
                .planId(planId)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(now)
                .endDate(end)
                .autoRenew(true)
                .build());

        paymentRepository.save(Payment.builder()
                .userId(userId)
                .subscriptionId(subscription.getId())
                .planId(planId)
                .amount(plan.getPrice())
                .status(PaymentStatus.SUCCESS)
                .gateway("MOCK")
                .reference("PAY-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase())
                .paidAt(now)
                .build());

        // upgrade to premium (keep admins as-is)
        if (user.getRole() == Role.FREE_USER) {
            user.setRole(Role.PREMIUM_USER);
            userRepository.save(user);
        }

        String notifMsg = "You're now on the " + plan.getName() + " plan"
                + (freeMonths > 0 ? " +" + freeMonths + " free month(s) applied." : ".");
        notificationService.notify(userId, NotificationType.SUBSCRIPTION,
                "Subscription active", notifMsg, "/pages/membership/subscription.html");

        notificationService.notifyAdmins(NotificationType.SUBSCRIPTION,
                "New subscription purchased",
                user.getFullName() + " subscribed to the " + plan.getName() + " plan.",
                "/pages/admin/membership-management.html", userId);

        return subscription;
    }

    @Transactional
    public void cancel(Long userId) {
        Subscription current = current(userId);
        if (current != null) {
            current.setStatus(SubscriptionStatus.CANCELLED);
            current.setAutoRenew(false);
            subscriptionRepository.save(current);

            User user = userRepository.findById(userId).orElse(null);
            Plan plan = planRepository.findById(current.getPlanId()).orElse(null);
            notificationService.notifyAdmins(NotificationType.SUBSCRIPTION,
                    "Subscription cancelled",
                    (user != null ? user.getFullName() : "A member") + " cancelled the "
                            + (plan != null ? plan.getName() : "") + " subscription.",
                    "/pages/admin/membership-management.html", userId);
        }
    }

    /** Seed the default pricing plans on first startup (called by DataSeeder). */
    @Transactional
    public void seedDefaultPlans() {
        if (planRepository.count() > 0) return;
        planRepository.save(Plan.builder()
                .name("Free")
                .tagline("Get started and explore the network")
                .price(new java.math.BigDecimal("0"))
                .billingCycle(BillingCycle.MONTHLY)
                .features("Basic profile\nLimited search\nReceive referrals")
                .popular(false)
                .active(true)
                .build());
        planRepository.save(Plan.builder()
                .name("Premium")
                .tagline("Unlock chat, the map and premium tools")
                .price(new java.math.BigDecimal("499"))
                .billingCycle(BillingCycle.MONTHLY)
                .features("Everything in Free\nNearby professionals map\nUnlimited chat\nContact details visible\nLeaderboard ranking")
                .popular(true)
                .active(true)
                .build());
        planRepository.save(Plan.builder()
                .name("Premium Annual")
                .tagline("Best value — 2 months free")
                .price(new java.math.BigDecimal("4999"))
                .billingCycle(BillingCycle.YEARLY)
                .features("Everything in Premium\nPriority support\nFeatured profile badge")
                .popular(false)
                .active(true)
                .build());
    }

    @Transactional
    public Plan updatePlanStatus(Long planId, boolean active) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", planId));
        plan.setActive(active);
        return planRepository.save(plan);
    }

    // ---------- Aggregates for admin analytics ----------
    public List<Subscription> allSubscriptions() {
        return subscriptionRepository.findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }

    public long activeSubscriptionCount() {
        return subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
    }

    public java.math.BigDecimal totalRevenue() {
        return paymentRepository.totalRevenue();
    }

    private int monthsFor(BillingCycle cycle) {
        return switch (cycle) {
            case MONTHLY -> 1;
            case QUARTERLY -> 3;
            case YEARLY -> 12;
        };
    }
}
