package com.bnp.review;

import com.bnp.common.enums.Enums.NotificationType;
import com.bnp.common.enums.Enums.PointsReason;
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
public class ReviewService {

    private static final int REVIEW_RECEIVED_POINTS = 10;

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final PointsService pointsService;
    private final NotificationService notificationService;

    public List<Review> forUser(Long targetUserId) {
        return reviewRepository.findByTargetUserIdOrderByCreatedAtDesc(targetUserId);
    }

    @Transactional
    public Review add(Long authorId, Long targetUserId, int rating, String comment) {
        if (authorId.equals(targetUserId)) {
            throw new BadRequestException("You cannot review yourself");
        }
        if (rating < 1 || rating > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }
        if (reviewRepository.existsByTargetUserIdAndAuthorId(targetUserId, authorId)) {
            throw new BadRequestException("You have already reviewed this professional");
        }
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", targetUserId));

        Review review = reviewRepository.save(Review.builder()
                .targetUserId(targetUserId)
                .authorId(authorId)
                .rating(rating)
                .comment(comment)
                .build());

        recomputeRating(target);

        pointsService.award(targetUserId, REVIEW_RECEIVED_POINTS,
                PointsReason.REVIEW_RECEIVED, "Received a new review");
        notificationService.notify(targetUserId, NotificationType.REVIEW,
                "New review", "You received a " + rating + "-star review.",
                "/pages/profile/profile.html", authorId);

        return review;
    }

    private void recomputeRating(User target) {
        List<Review> reviews = reviewRepository.findByTargetUserIdOrderByCreatedAtDesc(target.getId());
        double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        target.setAvgRating(Math.round(avg * 10.0) / 10.0);
        target.setReviewCount(reviews.size());
        userRepository.save(target);
    }
}
