package com.binbang.backend.accommodation.specification;

import com.binbang.backend.accommodation.entity.Accommodation;
import com.binbang.backend.accommodation.entity.AccommodationFacility;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.function.BiFunction;

public class AccommodationSpecification {

    public static Specification<Accommodation> hasCategory(Long categoryId){
        return (root, query, criteriaBuilder) -> {
            // root = Accommodation 테이블(시작점)
            // query = 전체쿼리
            // criteriaBuilder = 조건을 만드는 도구
            if(categoryId == null){ // 카테고리아이디가 null이면 조건 없음
                return null; //조건 없음
            } //있으면  "category.categoryId = ?" 조건 생성
            return criteriaBuilder.equal(
                    root.get("category").get("categoryId"),
                    categoryId
            );
        };
    }

    private static Specification<Accommodation> facilityCondition(
            BiFunction<Root<AccommodationFacility>, CriteriaBuilder, Predicate> condition) {
        return (root, query, cb) -> {
            Subquery<Long> sub = query.subquery(Long.class);
            Root<AccommodationFacility> facility = sub.from(AccommodationFacility.class);
            sub.select(facility.get("accommodationId"))
                    .where(condition.apply(facility, cb));
            return root.get("accommodationId").in(sub);
        };
    }

    public static Specification<Accommodation> hasMinBedrooms(Integer minBedrooms) {
        if (minBedrooms == null) {
            return (root, query, cb) -> null;
        }
        return facilityCondition((f, cb) -> cb.greaterThanOrEqualTo(f.get("bedrooms"), minBedrooms));
    }

    public static Specification<Accommodation> hasMinBathrooms(Integer minBathrooms) {
        if (minBathrooms == null) {
            return (root, query, cb) -> null;
        }
        return facilityCondition((f, cb) -> cb.greaterThanOrEqualTo(f.get("bathrooms"), minBathrooms));
    }

    public static Specification<Accommodation> hasMinBeds(Integer minBeds) {
        if (minBeds == null) {
            return (root, query, cb) -> null;
        }
        return facilityCondition((f, cb) -> cb.greaterThanOrEqualTo(f.get("beds"), minBeds));
    }

    public static Specification<Accommodation> petAllowed(Boolean petAllowed) {
        if (petAllowed == null) {
            return (root, query, cb) -> null;
        }
        return facilityCondition((f, cb) -> cb.equal(f.get("petAllowed"), petAllowed));
    }

    public static Specification<Accommodation> parkingAvailable(Boolean parkingAvailable) {
        if (parkingAvailable == null) {
            return (root, query, cb) -> null;
        }
        return facilityCondition((f, cb) -> cb.equal(f.get("parkingAvailable"), parkingAvailable));
    }

    public static Specification<Accommodation> hasBbq(Boolean hasBbq) {
        if (hasBbq == null) {
            return (root, query, cb) -> null;
        }
        return facilityCondition((f, cb) -> cb.equal(f.get("hasBbq"), hasBbq));
    }

    public static Specification<Accommodation> hasWifi(Boolean hasWifi) {
        if (hasWifi == null) {
            return (root, query, cb) -> null;
        }
        return facilityCondition((f, cb) -> cb.equal(f.get("hasWifi"), hasWifi));
    }

    public static Specification<Accommodation> addressLike(String keyword){
        return (root, query, criteriaBuilder) -> {
            if (keyword == null) {
                return null;
            }
            return criteriaBuilder.like(
                    root.get("address"),
                    "%" + keyword + "%"
            );
        };
    }

    // 성능 비교용: ES의 multiMatch와 같은 역할을 LIKE로 구현 (name, address, description)
    public static Specification<Accommodation> keywordLike(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }
            String pattern = "%" + keyword + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("name"), pattern),
                    criteriaBuilder.like(root.get("address"), pattern),
                    criteriaBuilder.like(root.get("description"), pattern)
            );
        };
    }

    public static Specification<Accommodation> hasRegionIn(List<Long> regionIds){
        return (root, query,criteriaBuilder) -> {
            if(regionIds == null || regionIds.isEmpty()){
                return null;
            }
            return root.get("region").get("regionId").in(regionIds);
        };
    }
}
