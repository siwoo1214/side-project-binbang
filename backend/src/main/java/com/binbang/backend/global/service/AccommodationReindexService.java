package com.binbang.backend.global.service;

import com.binbang.backend.accommodation.document.AccommodationDocument;
import com.binbang.backend.accommodation.entity.Accommodation;
import com.binbang.backend.accommodation.entity.AccommodationFacility;
import com.binbang.backend.accommodation.repository.AccommodationFacilityRepository;
import com.binbang.backend.accommodation.repository.AccommodationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AccommodationReindexService {

    private static final int PAGE_SIZE = 1000;

    private final AccommodationRepository accommodationRepository;
    private final AccommodationFacilityRepository facilityRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final TransactionTemplate readOnlyTx;

    public AccommodationReindexService(AccommodationRepository accommodationRepository,
                                       AccommodationFacilityRepository facilityRepository,
                                       ElasticsearchOperations elasticsearchOperations,
                                       PlatformTransactionManager transactionManager) {
        this.accommodationRepository = accommodationRepository;
        this.facilityRepository = facilityRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.readOnlyTx = new TransactionTemplate(transactionManager);
        this.readOnlyTx.setReadOnly(true);
    }

    public long reindexAll() {
        long startedAt = System.currentTimeMillis();

        IndexOperations indexOps = elasticsearchOperations.indexOps(AccommodationDocument.class);
        if (indexOps.exists()) {
            indexOps.delete();
        }
        indexOps.createWithMapping();

        long lastId = 0L;
        long total = 0L;
        int pages = 0;

        while (true) {
            final long cursor = lastId;
            List<AccommodationDocument> documents = readOnlyTx.execute(status -> loadPage(cursor));

            if (documents == null || documents.isEmpty()) {
                break;
            }

            elasticsearchOperations.save(documents);

            total += documents.size();
            pages++;
            lastId = documents.get(documents.size() - 1).getAccommodationId();
            log.info("재색인 진행: page={}, 누적={}, lastId={}", pages, total, lastId);
        }

        indexOps.refresh();

        log.info("재색인 완료: 총 {}건, {}페이지, {}ms", total, pages, System.currentTimeMillis() - startedAt);
        return total;
    }

    private List<AccommodationDocument> loadPage(long lastId) {
        List<Accommodation> accommodations = accommodationRepository
                .findByAccommodationIdGreaterThanOrderByAccommodationIdAsc(lastId, PageRequest.of(0, PAGE_SIZE));

        if (accommodations.isEmpty()) {
            return List.of();
        }

        List<Long> ids = accommodations.stream()
                .map(Accommodation::getAccommodationId)
                .toList();

        Map<Long, AccommodationFacility> facilityMap = facilityRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(AccommodationFacility::getAccommodationId, Function.identity()));

        return accommodations.stream()
                .map(a -> AccommodationDocument.from(a, facilityMap.get(a.getAccommodationId())))
                .toList();
    }
}
