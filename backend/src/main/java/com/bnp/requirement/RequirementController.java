package com.bnp.requirement;

import com.bnp.common.ApiResponse;
import com.bnp.common.exception.BadRequestException;
import com.bnp.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/requirements")
@RequiredArgsConstructor
public class RequirementController {

    private final RequirementService requirementService;
    private final CurrentUser currentUser;

    public record RespondRequest(String message, String quote) {}

    private void requirePremium() {
        if (!currentUser.get().isPremium()) {
            throw new BadRequestException("Requirements are available to premium members only");
        }
    }

    @PostMapping
    public ApiResponse<Requirement> create(@RequestBody Requirement requirement) {
        requirePremium();
        return ApiResponse.ok("Requirement posted", requirementService.create(currentUser.id(), requirement));
    }

    @GetMapping
    public ApiResponse<List<Requirement>> all() {
        return ApiResponse.ok(requirementService.all());
    }

    @GetMapping("/mine")
    public ApiResponse<List<Requirement>> mine() {
        requirePremium();
        return ApiResponse.ok(requirementService.mine(currentUser.id()));
    }

    @GetMapping("/{id}")
    public ApiResponse<Requirement> details(@PathVariable("id") Long id) {
        return ApiResponse.ok(requirementService.get(id));
    }

    @GetMapping("/{id}/responses")
    public ApiResponse<List<RequirementResponse>> responses(@PathVariable("id") Long id) {
        return ApiResponse.ok(requirementService.responses(id));
    }

    @PostMapping("/{id}/responses")
    public ApiResponse<RequirementResponse> respond(@PathVariable("id") Long id, @RequestBody RespondRequest req) {
        requirePremium();
        return ApiResponse.ok("Response submitted",
                requirementService.respond(currentUser.id(), id, req.message(), req.quote()));
    }

    @PutMapping("/{id}/responses/{responseId}/accept")
    public ApiResponse<Requirement> accept(@PathVariable("id") Long id, @PathVariable("responseId") Long responseId) {
        return ApiResponse.ok("Response accepted",
                requirementService.acceptResponse(currentUser.id(), id, responseId));
    }

    @PutMapping("/{id}/fulfill")
    public ApiResponse<Requirement> fulfill(@PathVariable("id") Long id) {
        return ApiResponse.ok("Requirement fulfilled",
                requirementService.markFulfilled(currentUser.id(), id));
    }
}
