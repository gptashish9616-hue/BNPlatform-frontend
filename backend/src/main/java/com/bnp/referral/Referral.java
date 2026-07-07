package com.bnp.referral;

import com.bnp.common.BaseEntity;
import com.bnp.common.enums.Enums.ReferralStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Deal Referral — one member refers a client/lead to another professional on the platform. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "referrals")
public class Referral extends BaseEntity {

    @Column(nullable = false)
    private Long referrerId;     // member giving the referral

    @Column(nullable = false)
    private Long receiverId;     // professional receiving the referral

    private String clientName;
    private String clientContact;
    private String category;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReferralStatus status = ReferralStatus.PENDING;

    @Builder.Default
    private Integer pointsAwarded = 0;
}
