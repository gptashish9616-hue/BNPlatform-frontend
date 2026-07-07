package com.bnp.profile;

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

/** Optional company profile add-on for a user. */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "company_profiles")
public class CompanyProfile extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Long userId;

    private String companyName;
    private String designation;
    private String industry;
    private String website;

    @Lob
    private String about;

    private String logoUrl;
    private Integer foundedYear;
    private String teamSize;
}
