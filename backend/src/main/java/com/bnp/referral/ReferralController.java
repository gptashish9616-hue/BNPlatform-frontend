package com.bnp.referral;

import com.bnp.common.ApiResponse;
import com.bnp.common.enums.Enums.ReferralStatus;
import com.bnp.common.exception.BadRequestException;
import com.bnp.security.CurrentUser;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/referrals")
@RequiredArgsConstructor
public class ReferralController {

    private final ReferralService referralService;
    private final CurrentUser currentUser;

    public record CreateReferralRequest(
            @NotNull Long receiverId,
            String clientName,
            String clientContact,
            String category,
            String description
    ) {}

    private void requirePremium() {
        if (!currentUser.get().isPremium()) {
            throw new BadRequestException("Business referrals are available to premium members only");
        }
    }

    @PostMapping
    public ApiResponse<Referral> create(@RequestBody CreateReferralRequest req) {
        requirePremium();
        return ApiResponse.ok("Referral created", referralService.create(
                currentUser.id(), req.receiverId(), req.clientName(),
                req.clientContact(), req.category(), req.description()));
    }

    @GetMapping("/given")
    public ApiResponse<List<Referral>> given() {
        requirePremium();
        return ApiResponse.ok(referralService.given(currentUser.id()));
    }

    @GetMapping("/received")
    public ApiResponse<List<Referral>> received() {
        requirePremium();
        return ApiResponse.ok(referralService.received(currentUser.id()));
    }

    @GetMapping("/{id}")
    public ApiResponse<Referral> details(@PathVariable Long id) {
        requirePremium();
        return ApiResponse.ok(referralService.get(id, currentUser.id()));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Referral> updateStatus(@PathVariable Long id, @RequestParam ReferralStatus status) {
        requirePremium();
        return ApiResponse.ok("Status updated", referralService.updateStatus(currentUser.id(), id, status));
    }
}
