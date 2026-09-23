# CLAUDE.md — Backend

루트의 `/CLAUDE.md`를 먼저 읽어주세요. 이 파일은 백엔드 전용 보충 내용입니다.

---

## 빠른 참조

### 자주 쓰는 명령어
```bash
# 로컬 인프라 시작
docker-compose up -d

# 앱 실행
./gradlew bootRun

# 테스트 실행
./gradlew test

# JAR 빌드 (배포용)
./gradlew bootJar -x test
```

### 주요 설정 파일
- `src/main/resources/application.yaml` — 전체 설정 (환경변수 기반)
- `docker-compose.yml` — 로컬 DB/Redis/RabbitMQ
- `docker-compose.prod.yml` — 프로덕션 전체 스택
- `.github/workflows/deploy.yml` — CI/CD 파이프라인

---

## 도메인별 책임

| 도메인 | 책임 |
|--------|------|
| `accommodation` | 숙소 CRUD, 이미지 업로드, 다중 조건 검색 |
| `member` | 회원가입/로그인, JWT 발급, OAuth2 처리 |
| `reservation` | 예약 생성/취소/완료, 결제 연동 |
| `chat` | 채팅방 생성, 메시지 저장/조회, WebSocket 브로드캐스트 |
| `review` | 리뷰 작성 (완료된 예약에 한해 1회) |
| `wishlist` | 찜 추가/제거/목록 조회 |
| `category` | 숙소 유형 및 지역 계층 조회 |
| `address` | 주소 검색 (카카오 주소 API 연동) |
| `global` | JWT 필터, Security 설정, S3, 이메일, RabbitMQ |

---

## 현재 작업 중인 영역

- `reservation/` — ReservationCreateRequest 수정, 테스트 코드 작성 중
- `src/test/resources/` — 테스트 전용 리소스 (application-test.yaml 등)

---

## 주의사항

- `binbang-key.pem` — EC2 SSH 키, 절대 커밋하지 말 것 (`.gitignore` 확인)
- `application.yaml`의 실제 시크릿 값은 환경변수로 주입, 하드코딩 금지
- Entity를 Controller에서 직접 반환하지 말 것 — 반드시 DTO 변환
- `@Transactional`이 필요한 메서드에 누락 없이 적용할 것
