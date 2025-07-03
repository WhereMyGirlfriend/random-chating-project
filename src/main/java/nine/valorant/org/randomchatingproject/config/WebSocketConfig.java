package nine.valorant.org.randomchatingproject.config;

import nine.valorant.org.randomchatingproject.jwt.JwtProvider;
import nine.valorant.org.randomchatingproject.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지 브로커 설정
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    log.debug("WebSocket 연결 시도");

                    // Authorization 헤더에서 JWT 토큰 추출
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);

                        try {
                            if (jwtProvider.validateToken(token)) {
                                String username = jwtProvider.getUsername(token);
                                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                userDetails, null, userDetails.getAuthorities());

                                accessor.setUser(authentication);

                                // 세션에 사용자 정보 저장
                                accessor.getSessionAttributes().put("username", username);

                                log.info("WebSocket 인증 성공: {}", username);
                            } else {
                                log.warn("WebSocket 연결 시 유효하지 않은 JWT 토큰");
                                // 임시로 게스트 허용 (필요시 예외 발생)
                                // throw new IllegalArgumentException("Invalid JWT token");
                            }
                        } catch (Exception e) {
                            log.error("WebSocket 인증 중 오류 발생", e);
                            // 임시로 게스트 허용 (필요시 예외 발생)
                            // throw new IllegalArgumentException("Authentication failed: " + e.getMessage());
                        }
                    } else {
                        log.debug("WebSocket 연결 시 Authorization 헤더 없음 - 게스트 허용");
                        // 토큰이 없는 경우에도 임시로 연결 허용 (테스트 환경)
                        // 운영 환경에서는 다음 줄의 주석을 해제하여 인증 강제
                        // throw new IllegalArgumentException("Authorization header is missing");
                    }
                } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand()) ||
                        StompCommand.SEND.equals(accessor.getCommand())) {
                    // 구독이나 메시지 전송 시에도 인증 확인 가능
                    if (accessor.getUser() == null) {
                        String sessionUsername = (String) accessor.getSessionAttributes().get("username");
                        if (sessionUsername == null) {
                            log.warn("인증되지 않은 사용자의 WebSocket 작업 시도");
                            // 필요시 예외 발생
                            // throw new IllegalArgumentException("User not authenticated");
                        }
                    }
                }

                return message;
            }
        });
    }
}