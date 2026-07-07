package com.bnp.groupchat;

import com.bnp.common.enums.Enums.GroupStatus;
import com.bnp.common.enums.Enums.NotificationType;
import com.bnp.common.exception.BadRequestException;
import com.bnp.common.exception.ResourceNotFoundException;
import com.bnp.group.Group;
import com.bnp.group.GroupRepository;
import com.bnp.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupChatService {

    private final GroupMemberRepository memberRepository;
    private final GroupMessageRepository messageRepository;
    private final GroupRepository groupRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    /** Community groups the user has joined and can post/read messages in. */
    public List<Group> myGroups(Long userId) {
        List<Long> groupIds = memberRepository.findByUserId(userId).stream()
                .map(GroupMember::getGroupId)
                .toList();
        return groupRepository.findAllById(groupIds);
    }

    @Transactional
    public void join(Long userId, Long groupId) {
        Group group = requireActiveGroup(groupId);
        if (memberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            return;
        }
        memberRepository.save(GroupMember.builder().groupId(groupId).userId(userId).build());
        group.setMemberCount(group.getMemberCount() + 1);
        groupRepository.save(group);
    }

    @Transactional
    public void leave(Long userId, Long groupId) {
        GroupMember membership = memberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new BadRequestException("You are not a member of this group"));
        memberRepository.delete(membership);
        groupRepository.findById(groupId).ifPresent(group -> {
            group.setMemberCount(Math.max(0, group.getMemberCount() - 1));
            groupRepository.save(group);
        });
    }

    public List<GroupMessage> messages(Long userId, Long groupId) {
        requireMember(userId, groupId);
        return messageRepository.findByGroupIdOrderByCreatedAtAsc(groupId);
    }

    @Transactional
    public GroupMessage send(Long senderId, Long groupId, String content) {
        requireMember(senderId, groupId);

        GroupMessage message = messageRepository.save(GroupMessage.builder()
                .groupId(groupId)
                .senderId(senderId)
                .content(content)
                .build());

        broadcastAndNotify(message, senderId, groupId);

        return message;
    }

    @Transactional
    public GroupMessage delete(Long userId, Long groupId, Long messageId) {
        requireMember(userId, groupId);
        GroupMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));
        if (!message.getGroupId().equals(groupId)) {
            throw new ResourceNotFoundException("Message", messageId);
        }
        if (!message.getSenderId().equals(userId)) {
            throw new BadRequestException("You can only delete your own messages");
        }

        message.setDeleted(true);
        message.setContent(null);
        messageRepository.save(message);

        messagingTemplate.convertAndSend("/topic/group-messages/" + groupId, message);

        return message;
    }

    /** Pushes the message live to everyone subscribed to the group's topic and notifies the rest. */
    private void broadcastAndNotify(GroupMessage message, Long senderId, Long groupId) {
        messagingTemplate.convertAndSend("/topic/group-messages/" + groupId, message);

        memberRepository.findByGroupId(groupId).stream()
                .map(GroupMember::getUserId)
                .filter(memberId -> !memberId.equals(senderId))
                .forEach(memberId -> notificationService.notify(memberId, NotificationType.CHAT,
                        "New group message", "You have a new message in a group you follow.",
                        "/pages/chat/group-chat.html?groupId=" + groupId, senderId));
    }

    private void requireMember(Long userId, Long groupId) {
        if (!memberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new BadRequestException("You must join this group to view or send messages");
        }
    }

    private Group requireActiveGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group", groupId));
        if (group.getStatus() != GroupStatus.ACTIVE) {
            throw new BadRequestException("This group is not currently active");
        }
        return group;
    }
}
