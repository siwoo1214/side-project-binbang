package com.binbang.backend.accommodation.entity;

import com.binbang.backend.category.entity.Category;
import com.binbang.backend.category.entity.Region;
import com.binbang.backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Table(name = "accommodation")
@Builder
public class Accommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accommodation_id")
    private Long accommodationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false)
    private Long price;

    @Lob //긴 텍스트 처리 JPA가 자동으로 인식하여 적절한 타입으로 변환해줌
    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "check_in_time", nullable = false)
    private LocalTime checkInTime;

    @Column(name = "check_out_time", nullable = false)
    private LocalTime checkOutTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private AccommodationStatus status = AccommodationStatus.OPEN;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate(){
        createdAt = LocalDateTime.now();
    }

    @OneToOne(mappedBy = "accommodation", fetch = FetchType.LAZY)
    private AccommodationFacility facility;

    // 대표 이미지 조회에 사용 (썸네일 추출용)
    @OneToMany(mappedBy = "accommodation", fetch = FetchType.LAZY)
    @Builder.Default
    private List<AccommodationImage> images = new ArrayList<>();

}
