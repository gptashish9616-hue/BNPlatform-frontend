package com.bnp.user;

import com.bnp.common.ApiResponse;
import com.bnp.common.enums.Enums.AccountStatus;
import com.bnp.common.enums.Role;
import com.bnp.user.UserDtos.UserCard;
import com.bnp.user.UserDtos.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Admin-only user management — guarded by /api/admin/** in SecurityConfig. */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<List<UserCard>> all() {
        return ApiResponse.ok(userService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(userService.getById(id));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<UserResponse> updateStatus(@PathVariable Long id, @RequestParam AccountStatus status) {
        return ApiResponse.ok("Status updated", userService.updateStatus(id, status));
    }

    @PutMapping("/{id}/role")
    public ApiResponse<UserResponse> updateRole(@PathVariable Long id, @RequestParam Role role) {
        return ApiResponse.ok("Role updated", userService.updateRole(id, role));
    }

    /** Wipes a test account's earned activity (referrals, points, reviews, requirements, invites, notifications). */
    @PostMapping("/{id}/reset-data")
    public ApiResponse<UserResponse> resetData(@PathVariable Long id) {
        return ApiResponse.ok("Test activity reset", userService.resetActivity(id));
    }
}
