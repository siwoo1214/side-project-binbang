package com.binbang.backend.accommodation.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "accommodation_facility")
@Builder
public class AccommodationFacility {

    @Id
    @Column(name = "accommodation_id")
    private Long accommodationId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "accommodation_id")
    private Accommodation accommodation;

    @Column(name = "bedrooms")
    private Integer bedrooms;

    @Column(name = "bathrooms")
    private Integer bathrooms;

    @Column(name = "beds")
    private Integer beds;

    @Column(name = "pet_allowed")
    private boolean petAllowed;

    @Column(name = "parking_available")
    private boolean parkingAvailable;

    @Column(name = "has_bbq")
    private boolean hasBbq;

    @Column(name = "has_wifi")
    private boolean hasWifi;
}
