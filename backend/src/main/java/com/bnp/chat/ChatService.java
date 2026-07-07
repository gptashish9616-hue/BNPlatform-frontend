package com.bnp.chat;

import com.bnp.common.enums.Enums.NotificationType;
import com.bnp.common.exception.BadRequestException;
import com.bnp.common.exception.ResourceNotFoundException;
import com.bnp.notification.NotificationService;
import com.bnp.user.User;
import com.bnp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Conversation startOrGet(Long userId, Long otherUserId) {
        if (userId.equals(otherUserId)) {
            throw new BadRequestException("You cannot start a conversation with yourself");
        }
        return conversationRepository.findBetween(userId, otherUserId)
                .orElseGet(() -> conversationRepository.save(Conversation.builder()
                        .userOneId(userId)
                        .userTwoId(otherUserId)
                        .build()));
    }

    public List<Conversation> conversations(Long userId) {
        return conversationRepository.findForUser(userId);
    }

    public List<Message> messages(Long userId, Long conversationId) {
        Conversation c = requireMember(userId, conversationId);
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(c.getId());
    }

    @Transactional
    public Message send(Long senderId, Long conversationId, String content) {
        Conversation c = requireMember(senderId, conversationId);

        Message message = messageRepository.save(Message.builder()
                .conversationId(conversationId)
                .senderId(senderId)
                .content(content)
                .build());

        c.setLastMessage(content);
        c.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(c);

        Long recipientId = c.getUserOneId().equals(senderId) ? c.getUserTwoId() : c.getUserOneId();
        notificationService.notify(recipientId, NotificationType.CHAT,
                "New message", "You have a new message.", "/pages/chat/chat.html", senderId);

        broadcast(message, senderId, recipientId);

        return message;
    }

    @Transactional
    public Message delete(Long userId, Long conversationId, Long messageId) {
        Conversation c = requireMember(userId, conversationId);
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));
        if (!message.getConversationId().equals(conversationId)) {
            throw new ResourceNotFoundException("Message", messageId);
        }
        if (!message.getSenderId().equals(userId)) {
            throw new BadRequestException("You can only delete your own messages");
        }

        message.setDeleted(true);
        message.setContent(null);
        messageRepository.save(message);

        Long recipientId = c.getUserOneId().equals(userId) ? c.getUserTwoId() : c.getUserOneId();
        broadcast(message, userId, recipientId);

        return message;
    }

    @Transactional
    public void deleteConversation(Long userId, Long conversationId) {
        Conversation c = requireMember(userId, conversationId);
        Long otherId = c.getUserOneId().equals(userId) ? c.getUserTwoId() : c.getUserOneId();

        messageRepository.deleteByConversationId(conversationId);
        conversationRepository.delete(c);

        broadcastConversationDeleted(conversationId, userId, otherId);
    }

    /** Pushes the new message over WebSocket to both participants' live sessions. */
    private void broadcast(Message message, Long senderId, Long recipientId) {
        userRepository.findById(senderId).map(User::getEmail)
                .ifPresent(email -> messagingTemplate.convertAndSendToUser(email, "/queue/messages", message));
        userRepository.findById(recipientId).map(User::getEmail)
                .ifPresent(email -> messagingTemplate.convertAndSendToUser(email, "/queue/messages", message));
    }

    /** Tells both participants' live sessions that the whole chat history is gone, so open UIs can reset. */
    private void broadcastConversationDeleted(Long conversationId, Long userId, Long otherId) {
        Map<String, Object> event = Map.of("type", "CONVERSATION_DELETED", "conversationId", conversationId);
        userRepository.findById(userId).map(User::getEmail)
                .ifPresent(email -> messagingTemplate.convertAndSendToUser(email, "/queue/messages", event));
        userRepository.findById(otherId).map(User::getEmail)
                .ifPresent(email -> messagingTemplate.convertAndSendToUser(email, "/queue/messages", event));
    }

    private Conversation requireMember(Long userId, Long conversationId) {
        Conversation c = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", conversationId));
        if (!c.getUserOneId().equals(userId) && !c.getUserTwoId().equals(userId)) {
            throw new BadRequestException("You are not part of this conversation");
        }
        return c;
    }
}
