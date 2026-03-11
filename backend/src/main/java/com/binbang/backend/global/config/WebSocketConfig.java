package com.binbang.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 설정 클래스
 *
 * 역할:
 * 1. WebSocket 엔드포인트 등록
 * 2. STOMP 메시지 브로커 설정
 * 3. 클라이언트-서버 통신 경로 설정
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * STOMP 엔드포인트 등록
     *
     * 예시 :
     * - Javascript: new SockJS('http://localhost:8080/ws')
     * - 이 주소로 접속하면 WebSocket 연결 시작
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        registry.addEndpoint("/ws")  // WebSocket 엔드포인트
                .setAllowedOriginPatterns("*");  // CORS 설정 (모든 도메인 허용)
                // .withSockJS() 제거 → 순수 WebSocket 사용 (Vite + 최신 브라우저 환경)
    }

    /**
     * 메시지 브로커 설정
     *
     * 메시지 브로커 = 메시지를 받아서 구독자들에게 전달하는 중개자
     *
     * 흐름:
     * 1. 클라이언트 -> /app/xxx -> 서버의 @MessageMapping
     * 2. 서버 -> /topic/xxx -> 구독중인 클라이언트들
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry){

        // 1. 서버가 클라이언트로 메시지 보낼 때 사용하는 prefix
        // /topic: 1:N (한 명이 보내면 구독자 여러명이 받음)
        // /queue: 1:1 (특정 사용자에게만 전송)
        registry.enableSimpleBroker("/topic","/queue");

        // 2. 클라이언트가 서버로 메시지 보낼 때 사용하는 prepix
        // 클라이언트: /app/chat/send -> 서버: @MessageMapping("/chat/send")
        registry.setApplicationDestinationPrefixes("/app");
    }

}
