// 숙소 검색 API 부하 테스트 — ES 경로 vs JPA 경로
// 실행 예: k6 run -e VUS=20 -e DURATION_SEC=60 search-load-test.js
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const VUS = Number(__ENV.VUS || 20);
const DURATION_SEC = Number(__ENV.DURATION_SEC || 60);
const WARMUP_SEC = 20;
const GAP_SEC = 10; // 시나리오 사이 쿨다운 (앞 시나리오의 잔여 부하가 섞이지 않도록)

// 실제 시딩 데이터에 존재하는 단어로 바꿔서 사용할 것 (결과 0건이면 측정 의미가 없음)
const KEYWORDS = (__ENV.KEYWORDS || '강남,원주,치악산,펜션,바다').split(',');

// JPA 경로에서 사용할 구조화 필터 조합 (keyword 없음)
const FILTERS = [
  'hasWifi=true',
  'petAllowed=true',
  'minBedrooms=2',
  'hasBbq=true&parkingAvailable=true',
  '', // 필터 없는 전체 목록
];

const ES_START = WARMUP_SEC + GAP_SEC;
const JPA_START = ES_START + DURATION_SEC + GAP_SEC;

export const options = {
  scenarios: {
    // 1) 워밍업: JVM JIT, 커넥션 풀, ES/PG 캐시를 데워서 첫 요청 지연이 결과에 섞이지 않게 함
    warmup: {
      executor: 'constant-vus',
      vus: 5,
      duration: `${WARMUP_SEC}s`,
      exec: 'warmup',
      tags: { path: 'warmup' },
    },
    // 2) ES 경로: keyword 있음
    es_path: {
      executor: 'constant-vus',
      vus: VUS,
      duration: `${DURATION_SEC}s`,
      startTime: `${ES_START}s`,
      exec: 'esSearch',
      tags: { path: 'es' },
    },
    // 3) JPA 경로: keyword 없음 (ES 시나리오가 끝난 뒤 따로 실행)
    jpa_path: {
      executor: 'constant-vus',
      vus: VUS,
      duration: `${DURATION_SEC}s`,
      startTime: `${JPA_START}s`,
      exec: 'jpaFilter',
      tags: { path: 'jpa' },
    },
  },
  // 태그별 임계값을 걸어두면 요약 결과에 경로별 응답시간이 따로 출력됨
  thresholds: {
    'http_req_duration{path:es}': ['p(95)<1000'],
    'http_req_duration{path:jpa}': ['p(95)<1000'],
    'http_req_failed{path:es}': ['rate<0.01'],
    'http_req_failed{path:jpa}': ['rate<0.01'],
    'checks{path:es}': ['rate>0.99'],
    // 경로별 요청 수를 요약에 따로 출력하기 위한 용도 (처리량 = count / DURATION_SEC)
    'http_reqs{path:es}': ['count>0'],
    'http_reqs{path:jpa}': ['count>0'],
  },
  summaryTrendStats: ['avg', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

function pick(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

function listUrl(query) {
  const page = 0
  const base = `${BASE_URL}/api/accommodation/list?page=${page}&size=20`;
  return query ? `${base}&${query}` : base;
}

export function warmup() {
  http.get(listUrl(`keyword=${encodeURIComponent(pick(KEYWORDS))}`));
  http.get(listUrl(pick(FILTERS)));
  sleep(0.5);
}

export function esSearch() {
  // 한글은 반드시 인코딩 (Postman에서 keyword가 빈 값으로 도착했던 문제와 같은 원인 방지)
  const keyword = encodeURIComponent(pick(KEYWORDS));
  const res = http.get(listUrl(`keyword=${keyword}`));

  check(res, {
    'es: status 200': (r) => r.status === 200,
    // 결과가 0건이면 ES가 일을 거의 안 한 것이므로 측정값이 왜곡됨 → 체크로 감지
    'es: 결과 1건 이상': (r) => {
      try {
        return r.json('content').length > 0;
      } catch (e) {
        return false;
      }
    },
  });
  sleep(0.1);
}

export function jpaFilter() {
  const res = http.get(listUrl(pick(FILTERS)));
  check(res, {
    'jpa: status 200': (r) => r.status === 200,
  });
  sleep(0.1);
}