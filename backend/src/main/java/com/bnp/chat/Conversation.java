package com.bnp.chat;

import com.bnp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "conversations")
public class Conversation extends BaseEntity {

    @Column(nullable = false)
    private Long userOneId;

    @Column(nullable = false)
    private Long userTwoId;

    private String lastMessage;
    private LocalDateTime lastMessageAt;
}
