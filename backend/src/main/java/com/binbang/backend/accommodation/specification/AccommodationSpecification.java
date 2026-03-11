package com.binbang.backend.accommodation.specification;

import com.binbang.backend.accommodation.entity.Accommodation;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

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

    public static Specification<Accommodation> hasMinBedrooms(Integer minBedrooms){
        return (root, query, criteriaBuilder) -> {
          if(minBedrooms == null){
              return null;
          }
          return criteriaBuilder.greaterThanOrEqualTo(
                  root.get("facility").get("bedrooms"),
                  minBedrooms
          );
        };
    }

    public static Specification<Accommodation> hasMinBathrooms(Integer minBathrooms){
        return (root, query, criteriaBuilder) -> {
            if(minBathrooms == null){
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("facility").get("bathrooms"),
                    minBathrooms
            );
        };
    }

    public static Specification<Accommodation> hasMinBeds(Integer minBeds){
        return (root, query, criteriaBuilder) -> {
            if(minBeds == null){
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("facility").get("beds"),
                    minBeds
            );
        };
    }

    public static Specification<Accommodation> petAllowed(Boolean petAllowed) {
        return (root, query, criteriaBuilder) -> {
            if(petAllowed == null){
                return null;
            }
            return criteriaBuilder.equal(
                    root.get("facility").get("petAllowed"),
                    petAllowed
            );
        };
    }

    public static Specification<Accommodation> parkingAvailable(Boolean parkingAvailable) {
        return (root, query, criteriaBuilder) -> {
            if(parkingAvailable == null){
                return null;
            }
            return criteriaBuilder.equal(
                    root.get("facility").get("parkingAvailable"),
                    parkingAvailable
            );
        };
    }

    public static Specification<Accommodation> hasBbq(Boolean hasBbq) {
        return (root, query, criteriaBuilder) -> {
            if(hasBbq == null){
                return null;
            }
            return criteriaBuilder.equal(
                    root.get("facility").get("hasBbq"),
                    hasBbq
            );
        };
    }

    public static Specification<Accommodation> hasWifi(Boolean hasWifi) {
        return (root, query, criteriaBuilder) -> {
            if(hasWifi == null){
                return null;
            }
            return criteriaBuilder.equal(
                    root.get("facility").get("hasWifi"),
                    hasWifi
            );
        };
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
    public static Specification<Accommodation> hasRegionIn(List<Long> regionIds){
        return (root, query,criteriaBuilder) -> {
            if(regionIds == null || regionIds.isEmpty()){
                return null;
            }
            return root.get("region").get("regionId").in(regionIds);
        };
    }
}
