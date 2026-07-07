package com.bnp.notification;

import com.bnp.common.ApiResponse;
import com.bnp.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;
    private final CurrentUser currentUser;

    @GetMapping
    public ApiResponse<List<Notification>> list(@RequestParam(defaultValue = "false") boolean unreadOnly) {
        return ApiResponse.ok(service.list(currentUser.id(), unreadOnly));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount() {
        return ApiResponse.ok(service.unreadCount(currentUser.id()));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Long id) {
        service.markRead(currentUser.id(), id);
        return ApiResponse.ok("Marked as read", null);
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllRead() {
        service.markAllRead(currentUser.id());
        return ApiResponse.ok("All marked as read", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(currentUser.id(), id);
        return ApiResponse.ok("Notification deleted", null);
    }
}
