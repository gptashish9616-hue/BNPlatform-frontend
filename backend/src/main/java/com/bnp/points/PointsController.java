package com.bnp.points;

import com.bnp.common.ApiResponse;
import com.bnp.security.CurrentUser;
import com.bnp.user.UserDtos.UserCard;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PointsController {

    private final PointsService pointsService;
    private final CurrentUser currentUser;

    @GetMapping("/points/history")
    public ApiResponse<List<PointsTransaction>> history() {
        return ApiResponse.ok(pointsService.history(currentUser.id()));
    }

    @GetMapping("/leaderboard")
    public ApiResponse<List<UserCard>> leaderboard(@RequestParam(required = false) String city,
                                                   @RequestParam(required = false) String state) {
        if (city != null && !city.isBlank()) return ApiResponse.ok(pointsService.cityLeaderboard(city));
        if (state != null && !state.isBlank()) return ApiResponse.ok(pointsService.stateLeaderboard(state));
        return ApiResponse.ok(pointsService.globalLeaderboard());
    }
}
