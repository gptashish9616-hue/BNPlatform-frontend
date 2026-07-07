package com.bnp.profile;

import com.bnp.common.BaseEntity;
import com.bnp.common.enums.Enums.DocumentStatus;
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

/** Document / certificate uploaded to the profile's authenticity section. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "authenticity_documents")
public class AuthenticityDocument extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    private String title;
    private String docType;      // certificate / license / award
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.PENDING;
}
