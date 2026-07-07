package com.bnp.analytics;

import com.bnp.common.enums.Role;
import com.bnp.invite.InviteRepository;
import com.bnp.notification.NotificationService;
import com.bnp.referral.ReferralRepository;
import com.bnp.requirement.RequirementRepository;
import com.bnp.subscription.SubscriptionService;
import com.bnp.user.User;
import com.bnp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UserRepository userRepository;
    private final ReferralRepository referralRepository;
    private final RequirementRepository requirementRepository;
    private final InviteRepository inviteRepository;
    private final NotificationService notificationService;
    private final SubscriptionService subscriptionService;

    /** Per-user dashboard summary. */
    public Map<String, Object> userDashboard(User user) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("points", user.getPoints());
        m.put("avgRating", user.getAvgRating());
        m.put("reviewCount", user.getReviewCount());
        m.put("freeMonthsEarned", user.getFreeMonthsEarned());
        m.put("referralsGiven", referralRepository.countByReferrerId(user.getId()));
        m.put("referralsReceived", referralRepository.countByReceiverId(user.getId()));
        m.put("invitesSent", inviteRepository.countByInviterId(user.getId()));
        m.put("requirementsPosted", requirementRepository.findByPosterIdOrderByCreatedAtDesc(user.getId()).size());
        m.put("unreadNotifications", notificationService.unreadCount(user.getId()));
        m.put("premium", user.isPremium());
        return m;
    }

    /** Platform-wide dashboard summary for admins. */
    public Map<String, Object> adminDashboard() {
        Map<String, Object> m = new LinkedHashMap<>();
        long total = userRepository.count();
        m.put("totalUsers", total);
        m.put("superAdmins", userRepository.countByRole(Role.SUPER_ADMIN));
        m.put("subAdmins", userRepository.countByRole(Role.SUB_ADMIN));
        m.put("premiumUsers", userRepository.countByRole(Role.PREMIUM_USER));
        m.put("freeUsers", userRepository.countByRole(Role.FREE_USER));
        m.put("totalReferrals", referralRepository.count());
        m.put("totalRequirements", requirementRepository.count());
        m.put("totalInvites", inviteRepository.count());
        m.put("activeSubscriptions", subscriptionService.activeSubscriptionCount());
        m.put("totalRevenue", subscriptionService.totalRevenue());
        return m;
    }
}
