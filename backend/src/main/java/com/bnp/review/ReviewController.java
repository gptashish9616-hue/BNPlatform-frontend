package com.bnp.review;

import com.bnp.common.ApiResponse;
import com.bnp.security.CurrentUser;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final CurrentUser currentUser;

    public record AddReviewRequest(
            @NotNull Long targetUserId,
            @NotNull @Min(1) @Max(5) Integer rating,
            String comment
    ) {}

    @GetMapping("/user/{userId}")
    public ApiResponse<List<Review>> forUser(@PathVariable Long userId) {
        return ApiResponse.ok(reviewService.forUser(userId));
    }

    @PostMapping
    public ApiResponse<Review> add(@RequestBody AddReviewRequest req) {
        return ApiResponse.ok("Review submitted",
                reviewService.add(currentUser.id(), req.targetUserId(), req.rating(), req.comment()));
    }
}
