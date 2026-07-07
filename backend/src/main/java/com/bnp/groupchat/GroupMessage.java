package com.bnp.groupchat;

import com.bnp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A single message posted to a community group's shared thread. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "group_messages")
public class GroupMessage extends BaseEntity {

    @Column(nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private Long senderId;

    @Lob
    private String content;

    @Builder.Default
    private Boolean deleted = false;
}
