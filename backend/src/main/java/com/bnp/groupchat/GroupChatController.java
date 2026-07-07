package com.bnp.groupchat;

import com.bnp.common.ApiResponse;
import com.bnp.common.enums.Enums.GroupStatus;
import com.bnp.common.exception.BadRequestException;
import com.bnp.group.Group;
import com.bnp.group.GroupRepository;
import com.bnp.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/group-chat")
@RequiredArgsConstructor
public class GroupChatController {

    private final GroupChatService groupChatService;
    private final GroupRepository groupRepository;
    private final CurrentUser currentUser;

    public record SendMessageRequest(String content) {}

    private void requirePremium() {
        if (!currentUser.get().isPremium()) {
            throw new BadRequestException("Group chat is available to premium members only");
        }
    }

    /** Active community groups (city/category) that can be joined for group communication. */
    @GetMapping("/groups")
    public ApiResponse<List<Group>> browsableGroups() {
        requirePremium();
        return ApiResponse.ok(groupRepository.findByStatus(GroupStatus.ACTIVE));
    }

    /** Groups the current user has already joined. */
    @GetMapping("/my-groups")
    public ApiResponse<List<Group>> myGroups() {
        requirePremium();
        return ApiResponse.ok(groupChatService.myGroups(currentUser.id()));
    }

    @PostMapping("/groups/{groupId}/join")
    public ApiResponse<Void> join(@PathVariable Long groupId) {
        requirePremium();
        groupChatService.join(currentUser.id(), groupId);
        return ApiResponse.ok("Joined group", null);
    }

    @PostMapping("/groups/{groupId}/leave")
    public ApiResponse<Void> leave(@PathVariable Long groupId) {
        requirePremium();
        groupChatService.leave(currentUser.id(), groupId);
        return ApiResponse.ok("Left group", null);
    }

    @GetMapping("/groups/{groupId}/messages")
    public ApiResponse<List<GroupMessage>> messages(@PathVariable Long groupId) {
        requirePremium();
        return ApiResponse.ok(groupChatService.messages(currentUser.id(), groupId));
    }

    @PostMapping("/groups/{groupId}/messages")
    public ApiResponse<GroupMessage> send(@PathVariable Long groupId, @RequestBody SendMessageRequest req) {
        requirePremium();
        return ApiResponse.ok("Message sent", groupChatService.send(currentUser.id(), groupId, req.content()));
    }

    @DeleteMapping("/groups/{groupId}/messages/{messageId}")
    public ApiResponse<GroupMessage> delete(@PathVariable Long groupId, @PathVariable Long messageId) {
        requirePremium();
        return ApiResponse.ok("Message deleted", groupChatService.delete(currentUser.id(), groupId, messageId));
    }
}
