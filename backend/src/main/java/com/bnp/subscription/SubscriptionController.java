package com.bnp.subscription;

import com.bnp.common.ApiResponse;
import com.bnp.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final CurrentUser currentUser;

    /**
     * Public: pricing page plans.
     */
    @GetMapping("/plans")
    public ApiResponse<List<Plan>> plans() {
        return ApiResponse.ok(subscriptionService.activePlans());
    }

    @PostMapping("/subscriptions/{planId}")
    public ApiResponse<Subscription> subscribe(@PathVariable Long planId) {
        return ApiResponse.ok("Subscribed", subscriptionService.subscribe(currentUser.id(), planId));
    }

    @GetMapping("/subscriptions/current")
    public ApiResponse<Subscription> current() {
        return ApiResponse.ok(subscriptionService.current(currentUser.id()));
    }

    @GetMapping("/subscriptions/history")
    public ApiResponse<List<Subscription>> history() {
        return ApiResponse.ok(subscriptionService.history(currentUser.id()));
    }

    @PostMapping("/subscriptions/cancel")
    public ApiResponse<Void> cancel() {
        subscriptionService.cancel(currentUser.id());
        return ApiResponse.ok("Subscription cancelled", null);
    }

    @GetMapping("/payments/history")
    public ApiResponse<List<Payment>> payments() {
        return ApiResponse.ok(subscriptionService.payments(currentUser.id()));
    }
}
