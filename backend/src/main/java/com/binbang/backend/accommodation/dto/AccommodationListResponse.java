package com.binbang.backend.accommodation.dto;

import com.binbang.backend.accommodation.entity.Accommodation;
import com.binbang.backend.accommodation.entity.AccommodationImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccommodationListResponse {

    private Long accommodationId;
    private String name;
    private Long price;
    private String regionName;      // 지역명 (예: 서울, 제주)
    private String categoryName;    // 카테고리명 (예: 펜션, 호텔)
    private String thumbnailUrl;    // 대표 이미지 URL (없으면 null)

    /**
     * Accommodation 엔티티 → DTO 변환 (정적 팩토리 메서드)
     * thumbnailUrl: sortOrder=0인 첫 번째 이미지, 없으면 null
     */
    public static AccommodationListResponse from(Accommodation accommodation) {
        // 이미지 목록에서 sortOrder가 가장 낮은(=0) 대표 이미지 URL 추출
        // images가 null일 수 있으므로 방어 처리 (JPA 초기화 타이밍 이슈)
        List<AccommodationImage> images = accommodation.getImages();
        String thumbnail = (images == null) ? null :
                images.stream()
                        .filter(img -> img.getSortOrder() == 0)
                        .findFirst()
                        .map(AccommodationImage::getImageUrl)
                        .orElse(null);

        return AccommodationListResponse.builder()
                .accommodationId(accommodation.getAccommodationId())
                .name(accommodation.getName())
                .price(accommodation.getPrice())
                .regionName(accommodation.getRegion().getName())
                .categoryName(accommodation.getCategory().getName())
                .thumbnailUrl(thumbnail)
                .build();
    }
}
