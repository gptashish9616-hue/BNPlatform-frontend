package com.bnp.notification;

import com.bnp.common.BaseEntity;
import com.bnp.common.enums.Enums.NotificationChannel;
import com.bnp.common.enums.Enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @Column(nullable = false)
    private Long userId;          // recipient

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NotificationType type = NotificationType.SYSTEM;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NotificationChannel channel = NotificationChannel.IN_APP;

    private String title;
    private String message;
    private String link;
    private Long fromUserId;   // who triggered this notification (null = system)

    @Builder.Default
    private Boolean readFlag = false;
}
