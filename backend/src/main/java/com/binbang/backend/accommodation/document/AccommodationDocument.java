package com.binbang.backend.accommodation.document;

import com.binbang.backend.accommodation.entity.Accommodation;
import com.binbang.backend.accommodation.entity.AccommodationFacility;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Document(indexName = "accommodation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccommodationDocument {

    @Id
    private Long accommodationId;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String name;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String description;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String address;

    @Field(type = FieldType.Keyword)
    private Long categoryId;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String categoryName;

    @Field(type = FieldType.Keyword)
    private Long regionId;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String regionName;

    @Field(type = FieldType.Long)
    private Long price;

    @Field(type = FieldType.Integer)
    private Integer bedrooms;

    @Field(type = FieldType.Integer)
    private Integer bathrooms;

    @Field(type = FieldType.Integer)
    private Integer beds;

    @Field(type = FieldType.Boolean)
    private boolean petAllowed;

    @Field(type = FieldType.Boolean)
    private boolean parkingAvailable;

    @Field(type = FieldType.Boolean)
    private boolean hasBbq;

    @Field(type = FieldType.Boolean)
    private boolean hasWifi;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime createdAt;

    // 숙소 상세를 포함해서 하나 만들기
    public static AccommodationDocument from(Accommodation accommodation, AccommodationFacility facility) {
        AccommodationDocumentBuilder builder = AccommodationDocument.builder()
                .accommodationId(accommodation.getAccommodationId())
                .name(accommodation.getName())
                .description(accommodation.getDescription())
                .address(accommodation.getAddress())
                .categoryId(accommodation.getCategory().getCategoryId())
                .categoryName(accommodation.getCategory().getName())
                .regionId(accommodation.getRegion().getRegionId())
                .regionName(accommodation.getRegion().getName())
                .price(accommodation.getPrice())
                .status(accommodation.getStatus().name())
                .createdAt(accommodation.getCreatedAt());

        if (facility != null) {
            builder.bedrooms(facility.getBedrooms())
                    .bathrooms(facility.getBathrooms())
                    .beds(facility.getBeds())
                    .petAllowed(facility.isPetAllowed())
                    .parkingAvailable(facility.isParkingAvailable())
                    .hasBbq(facility.isHasBbq())
                    .hasWifi(facility.isHasWifi());
        }
        return builder.build();
    }
}