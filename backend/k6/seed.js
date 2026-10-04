import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://localhost:8080';

// TODO: 실제 로그인 계정으로 교체
const LOGIN_EMAIL = 'parksiwoo1214@naver.com';
const LOGIN_PASSWORD = 'password123!';

// 중복 없는 지역명만 선별 (data.sql 기준)
const REGIONS = [
  '강남구', '서초구', '마포구', '용산구',
  '해운대구', '수영구', '제주시', '서귀포시',
  '원주시', '춘천시', '수원시', '성남시',
];
const CATEGORY_IDS = [1, 2, 3, 4, 5, 6, 7];
const KEYWORDS = [
  '조용한', '깨끗한', '넓은', '바다뷰', '도심속',
  '가성비', '신축', '반려동물동반', '가족여행', '커플여행',
];

export const options = {
  scenarios: {
    seed: {
      executor: 'shared-iterations',
      vus: 5,
      iterations: 500,       // 총 등록 건수 (필요에 따라 조절)
      maxDuration: '10m',
    },
  },
};

export function setup() {
  const loginRes = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ email: LOGIN_EMAIL, password: LOGIN_PASSWORD }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  check(loginRes, { '로그인 성공': (r) => r.status === 200 });

  return { accessToken: loginRes.json('accessToken') };
}

export default function (data) {
  const region = REGIONS[Math.floor(Math.random() * REGIONS.length)];
  const categoryId = CATEGORY_IDS[Math.floor(Math.random() * CATEGORY_IDS.length)];
  const keyword = KEYWORDS[Math.floor(Math.random() * KEYWORDS.length)];
  const idx = `${__VU}-${__ITER}`;

  const payload = {
    name: `${keyword} ${region} 숙소 ${idx}`,
    price: 50000 + Math.floor(Math.random() * 200000),
    description: `${region}에 위치한 ${keyword} 분위기의 숙소입니다. 부하 테스트용 데이터 ${idx}.`,
    address: `${region} 테스트로 ${Math.floor(Math.random() * 999) + 1}`,
    latitude: 37.5 + Math.random(),
    longitude: 127.0 + Math.random(),
    checkInTime: '15:00:00',
    checkOutTime: '11:00:00',
    categoryId: categoryId,
    regionName: region,
    facility: {
      bedrooms: 1 + Math.floor(Math.random() * 3),
      bathrooms: 1,
      beds: 1 + Math.floor(Math.random() * 3),
      petAllowed: Math.random() > 0.5,
      parkingAvailable: Math.random() > 0.5,
      hasBbq: Math.random() > 0.5,
      hasWifi: true,
    },
    policy: {
      refundPolicy: '체크인 3일 전까지 전액 환불',
      houseRules: '실내 금연',
      petAllowed: Math.random() > 0.5,
      parkingAvailable: Math.random() > 0.5,
      maxGuests: 2 + Math.floor(Math.random() * 4),
      additionalGuestFee: 10000,
    },
  };

  const res = http.post(
    `${BASE_URL}/api/accommodation/register`,
    JSON.stringify(payload),
    {
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${data.accessToken}`,
      },
    }
  );

  check(res, { '등록 성공 (201)': (r) => r.status === 201 });

  sleep(0.1);
}