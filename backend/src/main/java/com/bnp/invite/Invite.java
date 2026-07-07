package com.bnp.invite;

import com.bnp.common.BaseEntity;
import com.bnp.common.enums.Enums.InviteStatus;
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

/** App Referral — a user invites a peer; when the invitee registers the inviter earns a free month. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "invites")
public class Invite extends BaseEntity {

    @Column(nullable = false)
    private Long inviterId;

    private String inviteeEmail;
    private String inviteeName;
    private String message;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private InviteStatus status = InviteStatus.SENT;

    private Long acceptedUserId;
}
