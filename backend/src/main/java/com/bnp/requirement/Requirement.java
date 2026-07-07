package com.bnp.requirement;

import com.bnp.common.BaseEntity;
import com.bnp.common.enums.Enums.RequirementStatus;
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

/** A posted business requirement. Professionals respond; the poster earns points when it is fulfilled. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "requirements")
public class Requirement extends BaseEntity {

    @Column(nullable = false)
    private Long posterId;

    private String title;
    private String category;

    @Lob
    private String description;

    private String budget;
    private String city;
    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RequirementStatus status = RequirementStatus.OPEN;

    private Long acceptedResponderId;

    @Builder.Default
    private Integer responseCount = 0;
}
