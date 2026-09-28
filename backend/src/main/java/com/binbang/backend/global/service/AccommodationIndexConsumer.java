package com.binbang.backend.global.service;

import com.binbang.backend.accommodation.document.AccommodationDocument;
import com.binbang.backend.global.config.RabbitMQConfig;
import com.binbang.backend.global.dto.AccommodationIndexMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccommodationIndexConsumer {

    private final ElasticsearchOperations elasticsearchOperations;

    @RabbitListener(queues = RabbitMQConfig.ACCOMMODATION_INDEX_QUEUE)
    public void consumeIndexMessage(AccommodationIndexMessage message) {
        try {
            log.info("숙소 색인 메시지 수신: accommodationId={}", message.getAccommodationId());

            AccommodationDocument document = AccommodationDocument.builder()
                    .accommodationId(message.getAccommodationId())
                    .name(message.getName())
                    .description(message.getDescription())
                    .address(message.getAddress())
                    .categoryId(message.getCategoryId())
                    .categoryName(message.getCategoryName())
                    .regionId(message.getRegionId())
                    .regionName(message.getRegionName())
                    .price(message.getPrice())
                    .bedrooms(message.getBedrooms())
                    .bathrooms(message.getBathrooms())
                    .beds(message.getBeds())
                    .petAllowed(message.isPetAllowed())
                    .parkingAvailable(message.isParkingAvailable())
                    .hasBbq(message.isHasBbq())
                    .hasWifi(message.isHasWifi())
                    .status(message.getStatus())
                    .createdAt(message.getCreatedAt())
                    .build();

            elasticsearchOperations.save(document);

            log.info("숙소 색인 완료: accommodationId={}", message.getAccommodationId());
        } catch (Exception e) {
            log.error("숙소 색인 실패: accommodationId={}, error={}",
                    message.getAccommodationId(), e.getMessage(), e);
            throw new RuntimeException("숙소 색인 실패. 재시도합니다.", e);
        }
    }
}