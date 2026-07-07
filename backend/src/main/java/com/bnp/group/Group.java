package com.bnp.group;

import com.bnp.common.BaseEntity;
import com.bnp.common.enums.Enums.GroupStatus;
import com.bnp.common.enums.Enums.GroupType;
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

/** City or category community group managed by admins. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "community_groups")
public class Group extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupType type;

    private String city;

    private String category;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GroupStatus status = GroupStatus.ACTIVE;

    @Builder.Default
    private Integer memberCount = 0;
}
