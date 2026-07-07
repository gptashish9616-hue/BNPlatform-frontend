package com.bnp.group;

import com.bnp.common.ApiResponse;
import com.bnp.common.enums.Enums.GroupStatus;
import com.bnp.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Admin CRUD endpoints for community groups. Secured by /api/admin/** in SecurityConfig. */
@RestController
@RequestMapping("/api/admin/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupRepository groupRepository;

    @GetMapping
    public ApiResponse<List<Group>> list() {
        return ApiResponse.ok(groupRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @GetMapping("/{id}")
    public ApiResponse<Group> get(@PathVariable Long id) {
        Group g = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
        return ApiResponse.ok(g);
    }

    @PostMapping
    public ApiResponse<Group> create(@RequestBody Group request) {
        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .city(request.getCity())
                .category(request.getCategory())
                .status(GroupStatus.ACTIVE)
                .memberCount(0)
                .build();
        return ApiResponse.ok("Group created", groupRepository.save(group));
    }

    @PutMapping("/{id}")
    public ApiResponse<Group> update(@PathVariable Long id, @RequestBody Group request) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setType(request.getType());
        group.setCity(request.getCity());
        group.setCategory(request.getCategory());
        return ApiResponse.ok("Group updated", groupRepository.save(group));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Group> updateStatus(@PathVariable Long id,
                                           @RequestParam GroupStatus status) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
        group.setStatus(status);
        return ApiResponse.ok("Status updated", groupRepository.save(group));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, Long>> delete(@PathVariable Long id) {
        if (!groupRepository.existsById(id)) {
            throw new ResourceNotFoundException("Group not found: " + id);
        }
        groupRepository.deleteById(id);
        return ApiResponse.ok("Group deleted", Map.of("deletedId", id));
    }
}
