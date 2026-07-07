package com.bnp.invite;

import com.bnp.common.ApiResponse;
import com.bnp.common.exception.BadRequestException;
import com.bnp.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/invites")
@RequiredArgsConstructor
public class InviteController {

    private final InviteService inviteService;
    private final CurrentUser currentUser;

    public record SendInviteRequest(
            @NotBlank @Email String email,
            String name,
            String message
    ) {}

    public record BulkInviteRequest(
            @NotEmpty List<@NotBlank @Email String> emails,
            String message
    ) {}

    private void requirePremium() {
        if (!currentUser.get().isPremium()) {
            throw new BadRequestException("Application referrals are available to premium members only");
        }
    }

    /** Send a single invite by email. */
    @PostMapping
    public ApiResponse<Invite> send(@Valid @RequestBody SendInviteRequest req) {
        requirePremium();
        Invite invite = inviteService.createInvite(currentUser.id(), req.email(), req.name(), req.message());
        return ApiResponse.ok("Invite sent", invite);
    }

    /**
     * Send invites to multiple emails in one request.
     * Skips emails that were already invited; reports a summary message.
     */
    @PostMapping("/bulk")
    public ApiResponse<List<Invite>> bulk(@Valid @RequestBody BulkInviteRequest req) {
        requirePremium();
        Long userId = currentUser.id();
        List<Invite> sent = new ArrayList<>();
        List<String> skipped = new ArrayList<>();

        for (String email : req.emails()) {
            try {
                sent.add(inviteService.createInvite(userId, email, null, req.message()));
            } catch (BadRequestException e) {
                skipped.add(email);
            }
        }

        String msg = sent.size() + " invite(s) sent"
                + (skipped.isEmpty() ? "" : "; already invited: " + String.join(", ", skipped));
        return ApiResponse.ok(msg, sent);
    }

    /** Returns all invites sent by the current user, newest first. */
    @GetMapping("/history")
    public ApiResponse<List<Invite>> history() {
        requirePremium();
        return ApiResponse.ok(inviteService.sentInvites(currentUser.id()));
    }

    /** Aggregated invite stats — avoids two separate round-trips from the client. */
    @GetMapping("/stats")
    public ApiResponse<InviteService.InviteStats> stats() {
        requirePremium();
        return ApiResponse.ok(inviteService.getStats(currentUser.id()));
    }

    /** Returns the caller's personal invite code (embedded in their shareable referral link). */
    @GetMapping("/my-code")
    public ApiResponse<String> myCode() {
        requirePremium();
        return ApiResponse.ok(inviteService.getMyCode(currentUser.id()));
    }
}
