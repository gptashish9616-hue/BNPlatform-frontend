package com.bnp.review;

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

/** A rating + review left on a professional's profile. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reviews")
public class Review extends BaseEntity {

    @Column(nullable = false)
    private Long targetUserId;    // the professional being reviewed

    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false)
    private Integer rating;        // 1..5

    @Lob
    private String comment;
}
