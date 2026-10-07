package com.binbang.backend.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/*
숙소 벌크 작업 후 재색인 하려고 만들었는데
그냥 재색인 하는 기능을 가졌음
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "search.reindex", havingValue = "true")
public class AccommodationReindexRunner implements ApplicationRunner {

    private final AccommodationReindexService reindexService;

    @Override
    public void run(ApplicationArguments args) {
        log.warn("search.reindex=true → 숙소 ES 전체 재색인을 시작합니다. 완료 후 실행 인자를 반드시 제거하세요.");
        reindexService.reindexAll();
    }
}