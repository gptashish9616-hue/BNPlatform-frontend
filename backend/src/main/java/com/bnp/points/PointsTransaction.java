package com.bnp.points;

import com.bnp.common.BaseEntity;
import com.bnp.common.enums.Enums.PointsReason;
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

/** Credibility points ledger — points are reputation only, never redeemable. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "points_transactions")
public class PointsTransaction extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    private PointsReason reason;

    private String description;

    private Integer balanceAfter;
}
