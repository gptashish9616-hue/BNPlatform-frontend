package com.bnp.user;

import com.bnp.common.ApiResponse;
import com.bnp.common.exception.BadRequestException;
import com.bnp.security.CurrentUser;
import com.bnp.user.UserDtos.UpdateProfileRequest;
import com.bnp.user.UserDtos.UserCard;
import com.bnp.user.UserDtos.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CurrentUser currentUser;

    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.ok(UserResponse.from(currentUser.get()));
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateMe(@Valid @RequestBody UpdateProfileRequest req) {
        return ApiResponse.ok("Profile updated", userService.updateProfile(currentUser.id(), req));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(@PathVariable Long id) {
        return ApiResponse.ok(userService.getById(id));
    }

    @GetMapping("/search")
    public ApiResponse<List<UserCard>> search(@RequestParam(required = false) String q,
                                              @RequestParam(required = false) String city,
                                              @RequestParam(required = false) Double lat,
                                              @RequestParam(required = false) Double lng) {
        return ApiResponse.ok(userService.search(q, city, lat, lng));
    }

    /** Map of nearby professionals — premium users only. */
    @GetMapping("/nearby")
    public ApiResponse<List<UserCard>> nearby(@RequestParam String city) {
        if (!currentUser.get().isPremium()) {
            throw new BadRequestException("Upgrade to premium to see nearby professionals on the map");
        }
        return ApiResponse.ok(userService.nearby(city));
    }
}
