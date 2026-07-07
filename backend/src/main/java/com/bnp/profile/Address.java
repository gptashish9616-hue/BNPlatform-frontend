package com.bnp.profile;

import com.bnp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "addresses")
public class Address extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    private String label;        // Home / Office
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String country;
    private String pincode;

    @Builder.Default
    private Boolean primaryAddress = false;
}
