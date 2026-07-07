package com.bnp.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c WHERE c.userOneId = :a AND c.userTwoId = :b " +
            "OR c.userOneId = :b AND c.userTwoId = :a")
    Optional<Conversation> findBetween(@Param("a") Long a, @Param("b") Long b);

    @Query("SELECT c FROM Conversation c WHERE c.userOneId = :uid OR c.userTwoId = :uid " +
            "ORDER BY c.lastMessageAt DESC")
    List<Conversation> findForUser(@Param("uid") Long uid);
}

interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
    void deleteByConversationId(Long conversationId);
}
