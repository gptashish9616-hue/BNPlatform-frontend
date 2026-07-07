package com.bnp.chat;

import com.bnp.common.ApiResponse;
import com.bnp.common.exception.BadRequestException;
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
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final CurrentUser currentUser;

    public record SendMessageRequest(String content) {}

    private void requirePremium() {
        if (!currentUser.get().isPremium()) {
            throw new BadRequestException("Chat is available to premium members only");
        }
    }

    @PostMapping("/conversations/{otherUserId}")
    public ApiResponse<Conversation> start(@PathVariable Long otherUserId) {
        requirePremium();
        return ApiResponse.ok(chatService.startOrGet(currentUser.id(), otherUserId));
    }

    @GetMapping("/conversations")
    public ApiResponse<List<Conversation>> conversations() {
        requirePremium();
        return ApiResponse.ok(chatService.conversations(currentUser.id()));
    }

    @GetMapping("/conversations/{id}/messages")
    public ApiResponse<List<Message>> messages(@PathVariable Long id) {
        requirePremium();
        return ApiResponse.ok(chatService.messages(currentUser.id(), id));
    }

    @PostMapping("/conversations/{id}/messages")
    public ApiResponse<Message> send(@PathVariable Long id, @RequestBody SendMessageRequest req) {
        requirePremium();
        return ApiResponse.ok("Message sent", chatService.send(currentUser.id(), id, req.content()));
    }

    @DeleteMapping("/conversations/{id}/messages/{messageId}")
    public ApiResponse<Message> delete(@PathVariable Long id, @PathVariable Long messageId) {
        requirePremium();
        return ApiResponse.ok("Message deleted", chatService.delete(currentUser.id(), id, messageId));
    }

    @DeleteMapping("/conversations/{id}")
    public ApiResponse<Void> deleteConversation(@PathVariable Long id) {
        requirePremium();
        chatService.deleteConversation(currentUser.id(), id);
        return ApiResponse.ok("Chat deleted", null);
    }
}
