import axiosInstance from './axiosInstance';
import type { LoginRequest, SignupRequest, AuthResponse } from '../types/auth';

/**
 * 인증 관련 API 함수
 * 백엔드 AuthController.java 기반
 */

// POST /api/auth/signup - 회원가입
export const signup = async (data: SignupRequest): Promise<{ message: string }> => {
  const response = await axiosInstance.post('/auth/signup', data);
  return response.data;
};

// POST /api/auth/login - 로그인
export const login = async (data: LoginRequest): Promise<AuthResponse> => {
  const response = await axiosInstance.post('/auth/login', data);
  return response.data;
};

// POST /api/auth/logout - 로그아웃
export const logout = async (): Promise<void> => {
  await axiosInstance.post('/auth/logout');
};

// POST /api/auth/refresh - 토큰 재발급
export const refreshToken = async (refreshToken: string): Promise<AuthResponse> => {
  const response = await axiosInstance.post('/auth/refresh', { refreshToken });
  return response.data;
};

// GET /api/auth/me - 현재 로그인 사용자 확인 (테스트용)
export const getMe = async (): Promise<{ email: string; message: string }> => {
  const response = await axiosInstance.get('/auth/me');
  return response.data;
};
