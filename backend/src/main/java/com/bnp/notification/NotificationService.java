package com.bnp.notification;

import com.bnp.common.enums.Enums.NotificationType;
import com.bnp.common.enums.Role;
import com.bnp.common.exception.ResourceNotFoundException;
import com.bnp.user.User;
import com.bnp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;
    private final NotificationDispatcher dispatcher;
    private final UserRepository userRepository;

    /**
     * Push a notification: the in-app copy is saved synchronously (so the unread
     * count is immediately correct), then Email/SMS/WhatsApp delivery is fanned
     * out asynchronously so the calling request never blocks on it.
     */
    @Transactional
    public Notification notify(Long userId, NotificationType type, String title, String message, String link) {
        return notify(userId, type, title, message, link, null);
    }

    @Transactional
    public Notification notify(Long userId, NotificationType type, String title, String message, String link, Long fromUserId) {
        Notification n = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .link(link)
                .fromUserId(fromUserId)
                .build();
        n = repository.save(n);

        // fire-and-forget multi-channel delivery (runs off the request thread)
        dispatcher.dispatch(n);
        return n;
    }

    /** Broadcast an in-app notification to every SUPER_ADMIN / SUB_ADMIN. */
    @Transactional
    public void notifyAdmins(NotificationType type, String title, String message, String link) {
        notifyAdmins(type, title, message, link, null);
    }

    @Transactional
    public void notifyAdmins(NotificationType type, String title, String message, String link, Long fromUserId) {
        List<User> admins = userRepository.findByRoleIn(List.of(Role.SUPER_ADMIN, Role.SUB_ADMIN));
        admins.forEach(admin -> notify(admin.getId(), type, title, message, link, fromUserId));
    }

    public List<Notification> list(Long userId, boolean unreadOnly) {
        return unreadOnly
                ? repository.findByUserIdAndReadFlagFalseOrderByCreatedAtDesc(userId)
                : repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long unreadCount(Long userId) {
        return repository.countByUserIdAndReadFlagFalse(userId);
    }

    @Transactional
    public void markRead(Long userId, Long id) {
        Notification n = repository.findById(id)
                .filter(x -> x.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        n.setReadFlag(true);
        repository.save(n);
    }

    @Transactional
    public void markAllRead(Long userId) {
        List<Notification> list = repository.findByUserIdAndReadFlagFalseOrderByCreatedAtDesc(userId);
        list.forEach(n -> n.setReadFlag(true));
        repository.saveAll(list);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Notification n = repository.findById(id)
                .filter(x -> x.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        repository.delete(n);
    }
}
