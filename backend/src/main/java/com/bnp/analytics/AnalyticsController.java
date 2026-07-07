package com.bnp.analytics;

import com.bnp.common.ApiResponse;
import com.bnp.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final CurrentUser currentUser;

    @GetMapping("/dashboard/user")
    public ApiResponse<Map<String, Object>> userDashboard() {
        return ApiResponse.ok(analyticsService.userDashboard(currentUser.get()));
    }

    /** Admin-only — guarded by /api/admin/** rule in SecurityConfig. */
    @GetMapping("/admin/dashboard")
    public ApiResponse<Map<String, Object>> adminDashboard() {
        return ApiResponse.ok(analyticsService.adminDashboard());
    }
}
