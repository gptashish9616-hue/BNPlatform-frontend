package com.bnp.reward;

import com.bnp.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Admin-editable referral reward rules (guarded by /api/admin/** in SecurityConfig). */
@RestController
@RequestMapping("/api/admin/reward-config")
@RequiredArgsConstructor
public class AdminRewardController {

    private final RewardConfigService rewardConfigService;

    @GetMapping
    public ApiResponse<RewardConfig> get() {
        return ApiResponse.ok(rewardConfigService.getConfig());
    }

    @PutMapping
    public ApiResponse<RewardConfig> update(@RequestBody RewardConfig body) {
        return ApiResponse.ok("Reward rules updated", rewardConfigService.updateConfig(body));
    }
}
