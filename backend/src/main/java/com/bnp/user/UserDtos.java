package com.bnp.user;

/** Read/update payloads for user + profile. */
public class UserDtos {

    public record UserResponse(
            Long id,
            String fullName,
            String email,
            String phone,
            String role,
            String status,
            String profession,
            String headline,
            String bio,
            String avatarUrl,
            String city,
            String state,
            Double latitude,
            Double longitude,
            Integer points,
            Double avgRating,
            Integer reviewCount,
            Integer freeMonthsEarned,
            boolean premium
    ) {
        public static UserResponse from(User u) {
            return new UserResponse(
                    u.getId(), u.getFullName(), u.getEmail(), u.getPhone(),
                    u.getRole().name(), u.getStatus().name(), u.getProfession(),
                    u.getHeadline(), u.getBio(), u.getAvatarUrl(), u.getCity(), u.getState(),
                    u.getLatitude(), u.getLongitude(), u.getPoints(), u.getAvgRating(),
                    u.getReviewCount(), u.getFreeMonthsEarned(), u.isPremium());
        }
    }

    public record UpdateProfileRequest(
            String fullName,
            String phone,
            String profession,
            String headline,
            String bio,
            String avatarUrl,
            String city,
            String state,
            Double latitude,
            Double longitude
    ) {}

    /** Lighter card used in search / nearby / leaderboard / map lists. */
    public record UserCard(
            Long id, String fullName, String profession, String avatarUrl,
            String city, String state, Double latitude, Double longitude,
            Integer points, Double avgRating, boolean premium,
            String role, String status
    ) {
        public static UserCard from(User u) {
            return new UserCard(u.getId(), u.getFullName(), u.getProfession(), u.getAvatarUrl(),
                    u.getCity(), u.getState(), u.getLatitude(), u.getLongitude(),
                    u.getPoints(), u.getAvgRating(), u.isPremium(),
                    u.getRole().name(), u.getStatus().name());
        }
    }
}
