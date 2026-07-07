package com.bnp.requirement;

import com.bnp.common.ApiResponse;
import com.bnp.common.enums.Enums.RequirementStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Admin-only requirement management — guarded by /api/admin/** in SecurityConfig. */
@RestController
@RequestMapping("/api/admin/requirements")
@RequiredArgsConstructor
public class AdminRequirementController {

    private final RequirementRepository requirementRepository;
    private final RequirementService requirementService;

    @GetMapping
    public ApiResponse<List<Requirement>> all() {
        return ApiResponse.ok(requirementRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @GetMapping("/{id}/responses")
    public ApiResponse<List<RequirementResponse>> responses(@PathVariable Long id) {
        return ApiResponse.ok(requirementService.responses(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<Requirement> update(@PathVariable Long id, @RequestBody Requirement patch) {
        return ApiResponse.ok("Requirement updated", requirementService.adminUpdate(id, patch));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Requirement> updateStatus(@PathVariable Long id, @RequestParam RequirementStatus status) {
        return ApiResponse.ok("Status updated", requirementService.adminUpdateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requirementService.adminDelete(id);
        return ApiResponse.ok("Requirement deleted", null);
    }
}
