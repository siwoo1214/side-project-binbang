// =============================
// 인증 관련 타입 (백엔드 DTO 기반)
// =============================

// POST /api/auth/login 요청 body
export interface LoginRequest {
  email: string;
  password: string;
}

// POST /api/auth/signup 요청 body
export interface SignupRequest {
  email: string;
  password: string;
  name: string;
  phone: string;
}

// 로그인/토큰 재발급 응답 (AuthResponse.java 기반)
export interface AuthResponse {
  memberId: number;
  accessToken: string;
  refreshToken: string;
  tokenType: string;  // "Bearer"
  expiresIn: number;  // 만료 시간 (초)
}

// 프론트에서 관리하는 인증 상태
export interface AuthState {
  isAuthenticated: boolean;
  accessToken: string | null;
  refreshToken: string | null;
}
