import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // /api 로 시작하는 요청만 백엔드로 전달
      // ⚠️ '/login', '/oauth2' 는 프록시하면 안 됨!
      //    React Router의 /login 페이지를 백엔드가 가로채버림
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      // OAuth2 인증 시작 요청만 백엔드로 전달
      // (버튼 클릭 시 window.location.href로 직접 이동하므로 프록시 불필요)
    },
  },
})
