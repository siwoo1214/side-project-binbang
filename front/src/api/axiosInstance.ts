import axios from 'axios';

/**
 * Axios 인스턴스 - 모든 API 호출의 기반
 *
 * [역할]
 * 1. 기본 URL 설정 (vite proxy 덕분에 /api로만 써도 됨)
 * 2. 요청 인터셉터: 모든 요청에 자동으로 JWT 토큰 첨부
 * 3. 응답 인터셉터: 401 에러 시 자동 토큰 재발급 시도
 */

const axiosInstance = axios.create({
  baseURL: `${import.meta.env.VITE_API_URL}/api`,           // vite proxy가 localhost:8080/api 로 전달
  timeout: 10000,            // 10초 타임아웃
  headers: {
    'Content-Type': 'application/json',
  },
});

// ===========================
// 요청 인터셉터
// 모든 요청이 나가기 전에 실행됨
// ===========================
axiosInstance.interceptors.request.use(
  (config) => {
    // localStorage에서 accessToken 꺼내서 헤더에 자동 첨부
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// ===========================
// 응답 인터셉터
// 서버 응답이 왔을 때 실행됨
// ===========================
axiosInstance.interceptors.response.use(
  // 정상 응답 (2xx): 그대로 통과
  (response) => response,

  // 에러 응답 처리
  async (error) => {
    const originalRequest = error.config;

    // 401 Unauthorized + 아직 재시도 안 한 경우 → 토큰 재발급 시도
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true; // 무한루프 방지 플래그

      const refreshToken = localStorage.getItem('refreshToken');

      if (refreshToken) {
        try {
          // Refresh Token으로 새 Access Token 발급
          const response = await axios.post('/api/auth/refresh', { refreshToken });
          const { accessToken } = response.data;

          // 새 토큰 저장
          localStorage.setItem('accessToken', accessToken);

          // 실패했던 원래 요청을 새 토큰으로 재시도
          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
          return axiosInstance(originalRequest);

        } catch (refreshError) {
          // Refresh Token도 만료됨 → 강제 로그아웃
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          localStorage.removeItem('memberId');
          window.location.href = '/login';
          return Promise.reject(refreshError);
        }
      } else {
        // Refresh Token 없음 → 조용히 에러만 반환
        // 강제로 /login 이동 금지!
        // 이유: 비로그인 상태에서 퍼블릭 페이지를 볼 때
        // 다른 인증 필요 요청이 401을 받으면 여기서 홈/목록 페이지까지 날아가버림
        // 로그인 유도는 UI에서 명시적으로 처리 (예: 예약 버튼 클릭 시)
        return Promise.reject(error);
      }
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;
