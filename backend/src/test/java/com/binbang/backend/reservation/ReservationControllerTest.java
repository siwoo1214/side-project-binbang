package com.binbang.backend.reservation;

import com.binbang.backend.accommodation.entity.Accommodation;
import com.binbang.backend.accommodation.entity.AccommodationStatus;
import com.binbang.backend.accommodation.repository.AccommodationRepository;
import com.binbang.backend.category.entity.Category;
import com.binbang.backend.category.entity.Region;
import com.binbang.backend.category.repository.CategoryRepository;
import com.binbang.backend.category.repository.RegionRepository;
import com.binbang.backend.chat.service.ChatService;
import com.binbang.backend.global.jwt.JwtTokenProvider;
import com.binbang.backend.global.service.EmailConsumer;
import com.binbang.backend.global.service.MessageProducer;
import com.binbang.backend.global.service.NotificationConsumer;
import com.binbang.backend.member.entity.Member;
import com.binbang.backend.member.entity.MemberRole;
import com.binbang.backend.member.entity.ProviderType;
import com.binbang.backend.member.repository.MemberRepository;
import com.binbang.backend.reservation.dto.request.ReservationCreateRequest;
import com.binbang.backend.reservation.repository.ReservationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ReservationController 통합 테스트
 *
 * [@WithMockUser 대신 실제 JWT 토큰을 사용하는 이유]
 * - JWT 필터가 principal을 String(email)로 세팅함
 * - @WithMockUser는 UserDetails 타입으로 세팅 → 타입 불일치 → email = null
 * - 실제 JWT 토큰을 생성해서 Authorization 헤더에 직접 넣어야 올바르게 동작
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired MemberRepository memberRepository;
    @Autowired AccommodationRepository accommodationRepository;
    @Autowired ReservationRepository reservationRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired RegionRepository regionRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;  // 테스트용 JWT 토큰 생성

    @MockBean MessageProducer messageProducer;
    @MockBean ChatService chatService;
    @MockBean EmailConsumer emailConsumer;
    @MockBean NotificationConsumer notificationConsumer;

    private Member host;
    private Member guest;
    private Accommodation accommodation;

    // 테스트용 JWT 토큰
    private String guestToken;
    private String hostToken;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        accommodationRepository.deleteAll();
        memberRepository.deleteAll();
        categoryRepository.deleteAll();
        regionRepository.deleteAll();

        host = new Member();
        host.setEmail("host@test.com");
        host.setName("테스트호스트");
        host.setPhone("010-1111-1111");
        host.setProvider(ProviderType.LOCAL);
        host.setRole(MemberRole.USER);
        host = memberRepository.save(host);

        guest = new Member();
        guest.setEmail("guest@test.com");
        guest.setName("테스트게스트");
        guest.setPhone("010-2222-2222");
        guest.setProvider(ProviderType.LOCAL);
        guest.setRole(MemberRole.USER);
        guest = memberRepository.save(guest);

        // 실제 JWT 토큰 생성 (JWT 필터가 파싱할 수 있는 진짜 토큰)
        guestToken = "Bearer " + jwtTokenProvider.createAccessToken(guest.getEmail(), guest.getRole().name());
        hostToken  = "Bearer " + jwtTokenProvider.createAccessToken(host.getEmail(), host.getRole().name());

        Category category = new Category();
        category.setName("펜션");
        category = categoryRepository.save(category);

        Region region = new Region();
        region.setName("제주도");
        region.setDepth(1);
        region = regionRepository.save(region);

        accommodation = new Accommodation();
        accommodation.setMember(host);
        accommodation.setCategory(category);
        accommodation.setRegion(region);
        accommodation.setName("테스트펜션");
        accommodation.setPrice(100_000L);
        accommodation.setDescription("테스트 숙소 설명");
        accommodation.setAddress("제주도 테스트로 123");
        accommodation.setLatitude(33.4996);
        accommodation.setLongitude(126.5312);
        accommodation.setCheckInTime(LocalTime.of(15, 0));
        accommodation.setCheckOutTime(LocalTime.of(11, 0));
        accommodation.setStatus(AccommodationStatus.OPEN);
        accommodation = accommodationRepository.save(accommodation);
    }

    @Test
    @DisplayName("[성공] 정상 예약 생성 → 201 Created")
    void createReservation_success() throws Exception {
        ReservationCreateRequest request = new ReservationCreateRequest(
                accommodation.getAccommodationId(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(4),
                2
        );

        mockMvc.perform(post("/api/reservation")
                        .header("Authorization", guestToken)  // 실제 JWT 토큰
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RESERVED"))
                .andExpect(jsonPath("$.totalPrice").value(300_000))
                .andExpect(jsonPath("$.nights").value(3))
                .andExpect(jsonPath("$.guestName").value("테스트게스트"))
                .andExpect(jsonPath("$.accommodationName").value("테스트펜션"));
    }

    @Test
    @DisplayName("[실패] 날짜 중복 예약 → 4xx 에러")
    void createReservation_fail_dateConflict() throws Exception {
        ReservationCreateRequest first = new ReservationCreateRequest(
                accommodation.getAccommodationId(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(5),
                2
        );
        mockMvc.perform(post("/api/reservation")
                .header("Authorization", guestToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(first)));

        ReservationCreateRequest second = new ReservationCreateRequest(
                accommodation.getAccommodationId(),
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(4),
                1
        );

        mockMvc.perform(post("/api/reservation")
                        .header("Authorization", guestToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("[실패] @Valid 검증 실패 - 인원 0명 → 400 Bad Request")
    void createReservation_fail_invalidGuestCount() throws Exception {
        ReservationCreateRequest request = new ReservationCreateRequest(
                accommodation.getAccommodationId(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                0  // @Min(1) 위반
        );

        mockMvc.perform(post("/api/reservation")
                        .header("Authorization", guestToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[실패] 비로그인 요청 → 401 Unauthorized")
    void createReservation_fail_unauthorized() throws Exception {
        ReservationCreateRequest request = new ReservationCreateRequest(
                accommodation.getAccommodationId(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                2
        );

        // Authorization 헤더 없음 = 비인증 요청
        mockMvc.perform(post("/api/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("[성공] 내 예약 목록 조회 → 200 OK")
    void getMyReservations_success() throws Exception {
        mockMvc.perform(get("/api/reservation/my")
                        .header("Authorization", guestToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("[성공] 내 예약 목록 상태별 조회 → 200 OK")
    void getMyReservationsByStatus_success() throws Exception {
        mockMvc.perform(get("/api/reservation/my")
                        .header("Authorization", guestToken)
                        .param("status", "RESERVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}