package com.bnp.user;

import com.bnp.common.ApiResponse;
import com.bnp.common.enums.Enums.AccountStatus;
import com.bnp.common.enums.Role;
import com.bnp.user.UserDtos.UserCard;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public, unauthenticated endpoints for the marketing site (e.g. homepage members teaser). */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final UserRepository userRepository;

    private static final int DEFAULT_PAGE_SIZE = 24;
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * Public member directory — active non-admin members, newest first so a freshly
     * registered professional is discoverable right away. Paginated so the list scales
     * to any number of members (100, 10000, ...) without loading them all into one response.
     */
    @GetMapping("/members")
    public ApiResponse<MemberPage> members(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        Page<User> result = userRepository.findByRoleInAndStatus(
                List.of(Role.FREE_USER, Role.PREMIUM_USER),
                AccountStatus.ACTIVE,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt")));

        List<UserCard> cards = result.getContent().stream().map(UserCard::from).toList();
        MemberPage payload = new MemberPage(
                cards,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext());
        return ApiResponse.ok(payload);
    }

    public record MemberPage(
            List<UserCard> members,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {}
}
