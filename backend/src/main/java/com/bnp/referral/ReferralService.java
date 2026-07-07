package com.bnp.referral;

import com.bnp.common.enums.Enums.NotificationType;
import com.bnp.common.enums.Enums.PointsReason;
import com.bnp.common.enums.Enums.ReferralStatus;
import com.bnp.common.exception.BadRequestException;
import com.bnp.common.exception.ResourceNotFoundException;
import com.bnp.notification.NotificationService;
import com.bnp.points.PointsService;
import com.bnp.user.User;
import com.bnp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReferralService {

    private static final int REFERRAL_COMPLETED_POINTS = 100;

    private final ReferralRepository referralRepository;
    private final UserRepository userRepository;
    private final PointsService pointsService;
    private final NotificationService notificationService;

    @Transactional
    public Referral create(Long referrerId, Long receiverId, String clientName,
                           String clientContact, String category, String description) {
        if (referrerId.equals(receiverId)) {
            throw new BadRequestException("You cannot refer a client to yourself");
        }
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver", receiverId));

        Referral referral = Referral.builder()
                .referrerId(referrerId)
                .receiverId(receiverId)
                .clientName(clientName)
                .clientContact(clientContact)
                .category(category)
                .description(description)
                .status(ReferralStatus.PENDING)
                .build();
        referral = referralRepository.save(referral);

        notificationService.notify(receiver.getId(), NotificationType.REFERRAL,
                "New referral received",
                "You received a new business referral.",
                "/pages/referrals/received-referrals.html",
                referrerId);

        User referrer = userRepository.findById(referrerId).orElse(null);
        notificationService.notifyAdmins(NotificationType.REFERRAL,
                "New referral submitted",
                (referrer != null ? referrer.getFullName() : "A member") + " referred a client to "
                        + receiver.getFullName() + (category != null && !category.isBlank() ? " (" + category + ")" : "") + ".",
                "/pages/admin/referrals.html", referrerId);

        return referral;
    }

    public List<Referral> given(Long referrerId) {
        return referralRepository.findByReferrerIdOrderByCreatedAtDesc(referrerId);
    }

    public List<Referral> received(Long receiverId) {
        return referralRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId);
    }

    public Referral get(Long id, Long userId) {
        Referral r = referralRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Referral", id));
        if (!r.getReferrerId().equals(userId) && !r.getReceiverId().equals(userId)) {
            throw new BadRequestException("You don't have access to this referral");
        }
        return r;
    }

    /** Receiver updates status. Completing the deal awards the referrer credibility points. */
    @Transactional
    public Referral updateStatus(Long receiverId, Long referralId, ReferralStatus status) {
        Referral r = referralRepository.findById(referralId)
                .orElseThrow(() -> new ResourceNotFoundException("Referral", referralId));
        if (!r.getReceiverId().equals(receiverId)) {
            throw new BadRequestException("Only the referral receiver can update its status");
        }

        r.setStatus(status);

        if (status == ReferralStatus.COMPLETED && r.getPointsAwarded() == 0) {
            r.setPointsAwarded(REFERRAL_COMPLETED_POINTS);
            pointsService.award(r.getReferrerId(), REFERRAL_COMPLETED_POINTS,
                    PointsReason.REFERRAL_COMPLETED, "Referral #" + r.getId() + " completed");
            notificationService.notify(r.getReferrerId(), NotificationType.POINTS,
                    "Referral completed",
                    "Your referral was completed. You earned " + REFERRAL_COMPLETED_POINTS + " points.",
                    "/pages/referrals/given-referrals.html");
        }

        return referralRepository.save(r);
    }
}
