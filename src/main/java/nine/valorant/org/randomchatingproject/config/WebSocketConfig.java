package nine.valorant.org.randomchatingproject.config;

import nine.valorant.org.randomchatingproject.jwt.JwtProvider;
import nine.valorant.org.randomchatingproject.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지 브로커 설정
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    // JWT 인증 활성화 (쿠키 방식)
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    log.info("WebSocket CONNECT 요청 받음");

                    String token = null;

                    // 1순위: Authorization 헤더에서 토큰 확인 (기존 호환성)
                    String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
                    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                        token = authorizationHeader.substring(7);
                        log.info("WebSocket Authorization 헤더에서 토큰 추출됨");
                    } else {
                        // 2순위: 쿠키에서 토큰 확인 (새로운 방식)
                        // WebSocket에서는 HTTP 요청의 쿠키에 접근하기 어려우므로
                        // simpSessionAttributes나 다른 방법 사용
                        log.info("WebSocket Authorization 헤더 없음");

                        // 📝 참고: WebSocket에서 쿠키 접근은 제한적
                        // 클라이언트에서 토큰을 헤더로 보내는 것이 더 안전함
                    }

                    if (token != null) {
                        try {
                            if (jwtProvider.validateToken(token)) {
                                String username = jwtProvider.getUsername(token);
                                log.info("WebSocket JWT 토큰 검증 성공, 사용자: {}", username);

                                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                                Authentication authentication = new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities()
                                );

                                accessor.setUser(authentication);
                                log.info("WebSocket 인증 설정 완료: {}", username);
                            } else {
                                log.warn("WebSocket JWT 토큰 검증 실패");
                            }
                        } catch (Exception e) {
                            log.error("WebSocket 인증 처리 중 오류: {}", e.getMessage(), e);
                        }
                    } else {
                        log.warn("WebSocket 토큰이 없음");
                    }
                }
                return message;
            }
        });
    }
}