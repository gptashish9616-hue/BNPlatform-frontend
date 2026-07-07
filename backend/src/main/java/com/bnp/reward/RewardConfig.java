package com.bnp.reward;

import com.bnp.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Single-row, admin-editable rules for the invite referral reward. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reward_config")
public class RewardConfig extends BaseEntity {

    /** First N accepted invites earn a free month; beyond that, points only. */
    @Builder.Default
    private Integer freeMonthCap = 6;

    @Builder.Default
    private Integer pointsWithFreeMonth = 50;

    @Builder.Default
    private Integer pointsAfterCap = 100;
}
