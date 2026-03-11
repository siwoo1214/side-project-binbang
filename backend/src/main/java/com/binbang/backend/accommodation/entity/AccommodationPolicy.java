package com.binbang.backend.accommodation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "accommodation_policy")
public class AccommodationPolicy {

    @Id
    @Column(name = "policy_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long policyId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", nullable = false)
    private Accommodation accommodation;

    @Column(name = "policies", columnDefinition = "TEXT")
    private String policies;
}
