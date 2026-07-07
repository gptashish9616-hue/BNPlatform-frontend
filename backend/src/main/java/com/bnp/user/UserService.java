package com.bnp.user;

import com.bnp.common.exception.BadRequestException;
import com.bnp.common.exception.ResourceNotFoundException;
import com.bnp.invite.InviteRepository;
import com.bnp.notification.NotificationRepository;
import com.bnp.points.PointsTransactionRepository;
import com.bnp.referral.ReferralRepository;
import com.bnp.requirement.Requirement;
import com.bnp.requirement.RequirementRepository;
import com.bnp.requirement.RequirementResponseRepository;
import com.bnp.review.ReviewRepository;
import com.bnp.user.UserDtos.UpdateProfileRequest;
import com.bnp.user.UserDtos.UserCard;
import com.bnp.user.UserDtos.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ReferralRepository referralRepository;
    private final PointsTransactionRepository pointsTransactionRepository;
    private final NotificationRepository notificationRepository;
    private final ReviewRepository reviewRepository;
    private final RequirementRepository requirementRepository;
    private final RequirementResponseRepository requirementResponseRepository;
    private final InviteRepository inviteRepository;

    public UserResponse getById(Long id) {
        return UserResponse.from(find(id));
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest req) {
        User u = find(userId);
        if (req.fullName() != null) u.setFullName(req.fullName());
        if (req.phone() != null) u.setPhone(req.phone());
        if (req.profession() != null) u.setProfession(req.profession());
        if (req.headline() != null) u.setHeadline(req.headline());
        if (req.bio() != null) u.setBio(req.bio());
        if (req.avatarUrl() != null) u.setAvatarUrl(req.avatarUrl());
        if (req.city() != null) u.setCity(req.city());
        if (req.state() != null) u.setState(req.state());
        if (req.latitude() != null) u.setLatitude(req.latitude());
        if (req.longitude() != null) u.setLongitude(req.longitude());
        return UserResponse.from(userRepository.save(u));
    }

    public List<UserCard> search(String q, String city, Double lat, Double lng) {
        List<UserCard> results = userRepository.search(q, city).stream().map(UserCard::from).toList();
        if (lat != null && lng != null) {
            results = results.stream()
                    .sorted(java.util.Comparator.comparingDouble(u ->
                            u.latitude() != null && u.longitude() != null
                                    ? haversineKm(lat, lng, u.latitude(), u.longitude())
                                    : Double.MAX_VALUE))
                    .toList();
        }
        return results;
    }

    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /** Nearby professionals — premium-only feature (enforced in controller). */
    public List<UserCard> nearby(String city) {
        if (city == null || city.isBlank()) {
            throw new BadRequestException("City is required to find nearby professionals");
        }
        return userRepository.findTop50ByCityIgnoreCaseOrderByPointsDesc(city)
                .stream().map(UserCard::from).toList();
    }

    public User find(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    // ---------- Admin operations ----------
    public List<UserCard> listAll() {
        return userRepository.findAll().stream().map(UserCard::from).toList();
    }

    @Transactional
    public UserResponse updateStatus(Long userId, com.bnp.common.enums.Enums.AccountStatus status) {
        User u = find(userId);
        u.setStatus(status);
        return UserResponse.from(userRepository.save(u));
    }

    @Transactional
    public UserResponse updateRole(Long userId, com.bnp.common.enums.Role role) {
        User u = find(userId);
        u.setRole(role);
        return UserResponse.from(userRepository.save(u));
    }

    /** Wipes a user's earned activity (referrals, reviews, requirements, invites, notifications, points) — for clearing test accounts. */
    @Transactional
    public UserResponse resetActivity(Long userId) {
        User u = find(userId);

        List<Requirement> posted = requirementRepository.findByPosterIdOrderByCreatedAtDesc(userId);
        if (!posted.isEmpty()) {
            requirementResponseRepository.deleteByRequirementIdIn(posted.stream().map(Requirement::getId).toList());
        }
        requirementResponseRepository.deleteByResponderId(userId);
        requirementRepository.deleteByPosterId(userId);

        referralRepository.deleteByReferrerId(userId);
        reviewRepository.deleteByAuthorId(userId);
        inviteRepository.deleteByInviterId(userId);
        notificationRepository.deleteByUserId(userId);
        pointsTransactionRepository.deleteByUserId(userId);

        u.setPoints(0);
        return UserResponse.from(userRepository.save(u));
    }
}
