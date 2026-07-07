package com.bnp.subscription;

import com.bnp.common.BaseEntity;
import com.bnp.common.enums.Enums.BillingCycle;
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

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "plans")
public class Plan extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String tagline;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BillingCycle billingCycle = BillingCycle.MONTHLY;

    /** Comma/newline separated feature list. */
    @Lob
    private String features;

    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    private Boolean popular = false;
}
