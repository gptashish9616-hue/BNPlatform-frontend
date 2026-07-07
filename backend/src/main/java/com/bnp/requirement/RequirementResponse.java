package com.bnp.requirement;

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

/** A professional's response/offer to a posted requirement. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "requirement_responses")
public class RequirementResponse extends BaseEntity {

    @Column(nullable = false)
    private Long requirementId;

    @Column(nullable = false)
    private Long responderId;

    @Lob
    private String message;

    private String quote;

    @Builder.Default
    private Boolean accepted = false;
}
