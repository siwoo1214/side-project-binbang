package com.binbang.backend.category.dto;

import com.binbang.backend.category.entity.Region;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegionResponse {
    private Long regionId;
    private String name;
    private Integer depth;
    private String parentName;

    public static RegionResponse from(Region region){
        String parentName;
        if(region.getParent() == null){
            parentName = null;
        }else{
            parentName = region.getParent().getName();
        }
        return RegionResponse.builder()
                .regionId(region.getRegionId())
                .name(region.getName())
                .depth(region.getDepth())
                .parentName(parentName)
                .build();
    }
}
