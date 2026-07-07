package com.bnp.user;

import com.bnp.common.enums.Enums.AccountStatus;
import com.bnp.common.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByResetToken(String resetToken);

    Optional<User> findByInviteCode(String inviteCode);

    List<User> findByRole(Role role);

    List<User> findByRoleIn(List<Role> roles);

    List<User> findByCityIgnoreCase(String city);

    List<User> findByStateIgnoreCase(String state);

    long countByRole(Role role);

    @Query("SELECT u FROM User u WHERE " +
            "(:q IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(u.profession) LIKE LOWER(CONCAT('%', :q, '%'))) " +
            "AND (:city IS NULL OR LOWER(u.city) = LOWER(:city))")
    List<User> search(@Param("q") String q, @Param("city") String city);

    // Nearby professionals (premium-only feature) — top by points within a city
    List<User> findTop50ByCityIgnoreCaseOrderByPointsDesc(String city);

    // Leaderboards
    List<User> findTop50ByStateIgnoreCaseOrderByPointsDesc(String state);

    List<User> findTop50ByOrderByPointsDesc();

    // Paginated public member directory — newest registrations first, scales to any
    // number of members instead of being capped at a fixed count.
    Page<User> findByRoleInAndStatus(List<Role> roles, AccountStatus status, Pageable pageable);
}
