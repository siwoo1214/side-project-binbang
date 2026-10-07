-- ============================================================
-- E-1 대량 데이터 삽입 (결정적 생성: n번째 행의 내용은 n에만 의존)
-- 실행: psql -v start=1 -v end=10000 -f bulk-insert-accommodation.sql
-- 경계 ID: 514 (삽입 전 MAX(accommodation_id))
-- ============================================================
\set ON_ERROR_STOP on
SET client_encoding = 'UTF8';

BEGIN;

-- 행 번호 n과 필드 이름(salt)으로 0 ~ k-1 사이의 고정된 값을 만든다
CREATE FUNCTION pg_temp.pick(n int, salt text, k int) RETURNS int
LANGUAGE sql IMMUTABLE AS $$
    SELECT (abs(hashtext(n::text || ':' || salt)::bigint) % k)::int
$$;

CREATE TEMP TABLE bulk_src ON COMMIT DROP AS
SELECT nextval(pg_get_serial_sequence('accommodation', 'accommodation_id')) AS accommodation_id,
       s.*
FROM (
    WITH base AS (
        SELECT n,
               18 + pg_temp.pick(n, 'region', 228) AS region_id,
               1 + pg_temp.pick(n, 'category', 7) AS category_id
        FROM generate_series(:start, :end) AS n
    ),
    named AS (
        SELECT b.n,
               b.region_id,
               b.category_id,
               p.name AS sido,
               r.name AS sgg,
               (ARRAY['조용한','깨끗한','넓은','바다뷰','도심속','가성비','신축','반려동물동반',
                      '가족여행','커플여행','감성','프라이빗','숲속','루프탑','리버뷰','오션뷰'])
                   [1 + pg_temp.pick(b.n, 'modifier', 16)] AS modifier,
               CASE b.category_id
                   WHEN 1 THEN (ARRAY['호텔','모텔','부티크호텔'])[1 + pg_temp.pick(b.n, 'type', 3)]
                   WHEN 2 THEN (ARRAY['펜션','풀빌라 펜션','독채 펜션'])[1 + pg_temp.pick(b.n, 'type', 3)]
                   WHEN 3 THEN (ARRAY['게스트하우스','호스텔'])[1 + pg_temp.pick(b.n, 'type', 2)]
                   WHEN 4 THEN (ARRAY['한옥스테이','전통 한옥'])[1 + pg_temp.pick(b.n, 'type', 2)]
                   WHEN 5 THEN (ARRAY['글램핑','캠핑장','카라반'])[1 + pg_temp.pick(b.n, 'type', 3)]
                   WHEN 6 THEN (ARRAY['레지던스','오피스텔'])[1 + pg_temp.pick(b.n, 'type', 2)]
                   ELSE        (ARRAY['독채','목조주택','시골 스테이'])[1 + pg_temp.pick(b.n, 'type', 3)]
               END AS type_word
        FROM base b
        JOIN region r ON r.region_id = b.region_id
        JOIN region p ON p.region_id = r.parent_id
    )
    SELECT t.n,
           t.region_id,
           t.category_id,
           t.modifier || ' ' || t.sgg || ' ' || t.type_word AS name,
           concat_ws(' ',
               CASE pg_temp.pick(t.n, 'intro', 4)
                   WHEN 0 THEN t.sgg || '에 자리한 ' || t.type_word || '입니다.'
                   WHEN 1 THEN t.sido || ' ' || t.sgg || ' 중심가에서 가까운 숙소입니다.'
                   WHEN 2 THEN '조용한 동네에 있는 ' || t.type_word || '입니다.'
                   ELSE        '여행 동선이 편리한 위치의 ' || t.type_word || '입니다.'
               END,
               (ARRAY['객실이 넓고 채광이 좋습니다.','침구와 수건을 매일 교체합니다.',
                      '근처에 맛집과 카페가 많습니다.','대중교통으로 쉽게 오갈 수 있습니다.',
                      '셀프 체크인으로 편하게 입실할 수 있습니다.','공용 라운지에서 커피를 제공합니다.',
                      '최근 인테리어를 새로 단장했습니다.','조식은 신청 시 제공됩니다.',
                      '장기 숙박 시 할인이 적용됩니다.','방음이 잘 되어 조용히 쉴 수 있습니다.',
                      '주변에 산책하기 좋은 공원이 있습니다.','객실마다 스마트 TV가 있습니다.'])
                   [1 + pg_temp.pick(t.n, 'feature1', 12)],
               CASE WHEN pg_temp.pick(t.n, 'fcount', 3) >= 1 THEN
                   (ARRAY['객실이 넓고 채광이 좋습니다.','침구와 수건을 매일 교체합니다.',
                          '근처에 맛집과 카페가 많습니다.','대중교통으로 쉽게 오갈 수 있습니다.',
                          '셀프 체크인으로 편하게 입실할 수 있습니다.','공용 라운지에서 커피를 제공합니다.',
                          '최근 인테리어를 새로 단장했습니다.','조식은 신청 시 제공됩니다.',
                          '장기 숙박 시 할인이 적용됩니다.','방음이 잘 되어 조용히 쉴 수 있습니다.',
                          '주변에 산책하기 좋은 공원이 있습니다.','객실마다 스마트 TV가 있습니다.'])
                       [1 + pg_temp.pick(t.n, 'feature2', 12)]
               END,
               CASE WHEN pg_temp.pick(t.n, 'fcount', 3) = 2 THEN
                   (ARRAY['객실이 넓고 채광이 좋습니다.','침구와 수건을 매일 교체합니다.',
                          '근처에 맛집과 카페가 많습니다.','대중교통으로 쉽게 오갈 수 있습니다.',
                          '셀프 체크인으로 편하게 입실할 수 있습니다.','공용 라운지에서 커피를 제공합니다.',
                          '최근 인테리어를 새로 단장했습니다.','조식은 신청 시 제공됩니다.',
                          '장기 숙박 시 할인이 적용됩니다.','방음이 잘 되어 조용히 쉴 수 있습니다.',
                          '주변에 산책하기 좋은 공원이 있습니다.','객실마다 스마트 TV가 있습니다.'])
                       [1 + pg_temp.pick(t.n, 'feature3', 12)]
               END,
               CASE pg_temp.pick(t.n, 'view', 12)
                   WHEN 0 THEN '객실 창밖으로 바다가 한눈에 보입니다.'
                   WHEN 1 THEN '창밖으로 숲과 계곡이 펼쳐집니다.'
                   WHEN 2 THEN '밤에는 도시 야경을 감상할 수 있습니다.'
                   WHEN 3 THEN '강변 산책로가 바로 앞에 있습니다.'
               END,
               CASE pg_temp.pick(t.n, 'closing', 4)
                   WHEN 0 THEN '편안한 휴식을 원하는 분께 추천합니다.'
                   WHEN 1 THEN '가족, 친구와 함께 머물기 좋습니다.'
               END
           ) AS description,
           t.sido || ' ' || t.sgg || ' '
               || (ARRAY['중앙로','해변로','공원로','시장길','역전로','학교길','문화로','강변로'])
                      [1 + pg_temp.pick(t.n, 'road', 8)]
               || ' ' || (1 + pg_temp.pick(t.n, 'addrno', 300)) AS address
    FROM named t
    ORDER BY t.n
) s;

INSERT INTO accommodation (
    accommodation_id, member_id, category_id, region_id, name, price, description, address,
    latitude, longitude, check_in_time, check_out_time, status, created_at
)
SELECT accommodation_id,
       2,
       category_id,
       region_id,
       name,
       50000 + pg_temp.pick(n, 'price', 201) * 1000,
       description,
       address,
       33.0 + pg_temp.pick(n, 'lat', 5000) / 1000.0,
       126.0 + pg_temp.pick(n, 'lng', 3600) / 1000.0,
       TIME '15:00:00',
       TIME '11:00:00',
       'OPEN',
       TIMESTAMP '2026-10-06 00:00:00' - pg_temp.pick(n, 'created', 525600) * INTERVAL '1 minute'
FROM bulk_src
ORDER BY accommodation_id;

INSERT INTO accommodation_facility (
    accommodation_id, bedrooms, bathrooms, beds, pet_allowed, parking_available, has_bbq, has_wifi
)
SELECT accommodation_id,
       1 + pg_temp.pick(n, 'bedrooms', 3),
       1,
       1 + pg_temp.pick(n, 'beds', 3),
       pg_temp.pick(n, 'pet', 2) = 1,
       pg_temp.pick(n, 'parking', 2) = 1,
       pg_temp.pick(n, 'bbq', 2) = 1,
       true
FROM bulk_src
ORDER BY accommodation_id;

COMMIT;