package com.bnp.admin;

import com.bnp.common.ApiResponse;
import com.bnp.referral.Referral;
import com.bnp.referral.ReferralRepository;
import com.bnp.subscription.Plan;
import com.bnp.subscription.Subscription;
import com.bnp.subscription.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Admin-only platform-wide listings (guarded by /api/admin/** in SecurityConfig). */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ReferralRepository referralRepository;
    private final SubscriptionService subscriptionService;

    @GetMapping("/referrals")
    public ApiResponse<List<Referral>> referrals() {
        return ApiResponse.ok(referralRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @GetMapping("/subscriptions")
    public ApiResponse<List<Subscription>> subscriptions() {
        return ApiResponse.ok(subscriptionService.allSubscriptions());
    }

    @PutMapping("/plans/{id}/status")
    public ApiResponse<Plan> updatePlanStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean active = Boolean.TRUE.equals(body.get("active"));
        Plan plan = subscriptionService.updatePlanStatus(id, active);
        return ApiResponse.ok("Plan " + (active ? "enabled" : "disabled"), plan);
    }
}
