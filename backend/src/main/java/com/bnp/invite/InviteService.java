package com.bnp.invite;

import com.bnp.auth.UserRegisteredEvent;
import com.bnp.common.enums.Enums.InviteStatus;
import com.bnp.common.enums.Enums.NotificationType;
import com.bnp.common.enums.Enums.PointsReason;
import com.bnp.common.exception.BadRequestException;
import com.bnp.notification.NotificationService;
import com.bnp.points.PointsService;
import com.bnp.reward.RewardConfig;
import com.bnp.reward.RewardConfigService;
import com.bnp.user.User;
import com.bnp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InviteService {

    private final InviteRepository inviteRepository;
    private final UserRepository userRepository;
    private final PointsService pointsService;
    private final NotificationService notificationService;
    private final RewardConfigService rewardConfigService;

    @Transactional
    public Invite createInvite(Long inviterId, String inviteeEmail, String inviteeName, String message) {
        if (inviteRepository.existsByInviterIdAndInviteeEmailIgnoreCase(inviterId, inviteeEmail)) {
            throw new BadRequestException("You have already sent an invite to " + inviteeEmail);
        }
        Invite invite = Invite.builder()
                .inviterId(inviterId)
                .inviteeEmail(inviteeEmail)
                .inviteeName(inviteeName)
                .message(message)
                .code(generateCode())
                .status(InviteStatus.SENT)
                .build();
        return inviteRepository.save(invite);
    }

    public List<Invite> sentInvites(Long inviterId) {
        return inviteRepository.findByInviterIdOrderByCreatedAtDesc(inviterId);
    }

    public long invitesCount(Long inviterId) {
        return inviteRepository.countByInviterId(inviterId);
    }

    public InviteStats getStats(Long userId) {
        List<Invite> all = inviteRepository.findByInviterIdOrderByCreatedAtDesc(userId);
        long accepted = all.stream().filter(i -> i.getStatus() == InviteStatus.ACCEPTED).count();
        int freeMonths = userRepository.findById(userId)
                .map(u -> u.getFreeMonthsEarned() == null ? 0 : u.getFreeMonthsEarned())
                .orElse(0);
        return new InviteStats(all.size(), accepted, freeMonths);
    }

    /**
     * Returns the caller's personal invite code, generating and persisting one
     * lazily if the account pre-dates the invite code feature.
     */
    @Transactional
    public String getMyCode(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;
        if (user.getInviteCode() == null || user.getInviteCode().isBlank()) {
            user.setInviteCode(generateCode());
            userRepository.save(user);
        }
        return user.getInviteCode();
    }

    /**
     * Fired when a new user registers. Rewards the inviter if a valid invite
     * code was supplied, handling both email-specific codes and personal codes.
     */
    @EventListener
    @Transactional
    public void onUserRegistered(UserRegisteredEvent event) {
        if (event.referralCode() == null || event.referralCode().isBlank()) return;

        // 1. Email-specific invite code (row in the invites table)
        var emailInvite = inviteRepository.findByCode(event.referralCode());
        if (emailInvite.isPresent()) {
            Invite invite = emailInvite.get();
            if (invite.getStatus() == InviteStatus.ACCEPTED) return;

            invite.setStatus(InviteStatus.ACCEPTED);
            invite.setAcceptedUserId(event.userId());
            inviteRepository.save(invite);

            userRepository.findById(invite.getInviterId())
                    .ifPresent(inviter -> rewardInviter(inviter, event.email()));
            return;
        }

        // 2. Personal invite code stored on the User entity
        userRepository.findByInviteCode(event.referralCode()).ifPresent(inviter -> {
            if (inviter.getId().equals(event.userId())) return; // cannot invite yourself
            rewardInviter(inviter, event.email());
        });
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private void rewardInviter(User inviter, String inviteeEmail) {
        RewardConfig config = rewardConfigService.getConfig();
        int earned = inviter.getFreeMonthsEarned() == null ? 0 : inviter.getFreeMonthsEarned();

        if (earned < config.getFreeMonthCap()) {
            // First N accepted invites → free month + bonus points
            inviter.setFreeMonthsEarned(earned + 1);
            userRepository.save(inviter);

            pointsService.award(inviter.getId(), config.getPointsWithFreeMonth(),
                    PointsReason.INVITE_ACCEPTED, "Invite accepted by " + inviteeEmail);

            notificationService.notify(inviter.getId(), NotificationType.REFERRAL,
                    "Your invite was accepted!",
                    "You earned 1 free month and " + config.getPointsWithFreeMonth() + " points.",
                    "/pages/invites/invite-history.html");
        } else {
            // Beyond the cap → credibility points only
            pointsService.award(inviter.getId(), config.getPointsAfterCap(),
                    PointsReason.INVITE_ACCEPTED, "Invite accepted by " + inviteeEmail);

            notificationService.notify(inviter.getId(), NotificationType.REFERRAL,
                    "Your invite was accepted!",
                    "You earned " + config.getPointsAfterCap() + " credibility points.",
                    "/pages/invites/invite-history.html");
        }
    }

    private String generateCode() {
        return "BNP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public record InviteStats(long sent, long accepted, int freeMonthsEarned) {}
}
