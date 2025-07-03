package nine.valorant.org.randomchatingproject.controller;

import nine.valorant.org.randomchatingproject.entity.ChatMessage;
import nine.valorant.org.randomchatingproject.entity.GameRoom;
import nine.valorant.org.randomchatingproject.service.GameRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final GameRoomService gameRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 채팅 메시지 전송 (인증 포함)
     */
    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public ChatMessage sendMessage(@DestinationVariable String roomId,
                                   @Payload ChatMessage chatMessage,
                                   Principal principal,
                                   SimpMessageHeaderAccessor headerAccessor) {

        // 인증된 사용자 확인
        String username = getAuthenticatedUsername(principal, headerAccessor);
        if (username == null) {
            log.warn("인증되지 않은 사용자의 메시지 전송 시도 - roomId: {}", roomId);
            return createErrorMessage("인증이 필요합니다.", roomId);
        }

        // 방이 존재하는지 확인
        Optional<GameRoom> room = gameRoomService.getRoom(roomId);
        if (room.isEmpty() || !room.get().isActive()) {
            log.warn("존재하지 않거나 비활성화된 방에 메시지 전송 시도: {}", roomId);
            return createErrorMessage("방을 찾을 수 없습니다.", roomId);
        }

        // 사용자가 해당 방에 참가했는지 확인
        if (!room.get().getParticipants().contains(username)) {
            log.warn("방에 참가하지 않은 사용자 {} 의 메시지 전송 시도: {}", username, roomId);
            return createErrorMessage("방에 참가한 후 메시지를 보낼 수 있습니다.", roomId);
        }

        // 메시지 내용 검증
        String content = chatMessage.getContent();
        if (!StringUtils.hasText(content)) {
            log.warn("빈 메시지 전송 시도: {}", username);
            return null;
        }

        // 메시지 길이 제한 (300자)
        if (content.length() > 300) {
            content = content.substring(0, 300);
            log.info("메시지 길이 제한 적용: {} -> {} characters", chatMessage.getContent().length(), content.length());
        }

        // 보안을 위해 인증된 사용자명 사용 (클라이언트에서 전송된 sender 무시)
        chatMessage.setSender(username);
        chatMessage.setContent(content.trim());
        chatMessage.setRoomId(roomId);
        chatMessage.setType(ChatMessage.MessageType.CHAT);

        // 메시지 저장
        gameRoomService.saveChatMessage(chatMessage);

        log.info("방 {} 에서 {} 가 메시지 전송: {}", roomId, username,
                content.length() > 50 ? content.substring(0, 50) + "..." : content);

        return chatMessage;
    }

    /**
     * 사용자 방 참가 처리 (인증 포함)
     */
    @MessageMapping("/chat.addUser/{roomId}")
    public void addUser(@DestinationVariable String roomId,
                        @Payload ChatMessage chatMessage,
                        SimpMessageHeaderAccessor headerAccessor,
                        Principal principal) {

        // 인증된 사용자 확인
        String username = getAuthenticatedUsername(principal, headerAccessor);

        // 인증이 없는 경우 메시지에서 사용자명 가져오기 (임시 호환성)
        if (username == null && chatMessage.getSender() != null) {
            username = chatMessage.getSender().trim();
            log.warn("WebSocket 인증 없이 방 참가 시도: {} -> {}", username, roomId);

            // 운영 환경에서는 다음 줄 주석 해제하여 인증 강제
            // sendErrorToUser(username, "인증이 필요합니다.");
            // return;
        }

        if (!StringUtils.hasText(username)) {
            log.warn("사용자명이 비어있는 방 참가 시도");
            return;
        }

        username = username.trim();

        // 세션에 사용자 정보 저장
        headerAccessor.getSessionAttributes().put("username", username);
        headerAccessor.getSessionAttributes().put("roomId", roomId);

        // 방에 사용자 추가
        boolean joinSuccess = gameRoomService.joinRoom(roomId, username);

        if (joinSuccess) {
            // 참가 성공 메시지 생성
            ChatMessage joinMessage = ChatMessage.createJoinMessage(username, roomId);

            // 방 참가자들에게 알림
            messagingTemplate.convertAndSend("/topic/room/" + roomId, joinMessage);

            // 전체 로비에 방 목록 업데이트 알림
            messagingTemplate.convertAndSend("/topic/rooms",
                    ChatMessage.builder()
                            .type(ChatMessage.MessageType.ROOM_UPDATED)
                            .content("방 목록이 업데이트되었습니다")
                            .sender("SYSTEM")
                            .build());

            log.info("사용자 {} 가 방 {} 에 성공적으로 참가했습니다", username, roomId);
        } else {
            // 참가 실패 시 개별 사용자에게 에러 메시지
            sendErrorToUser(username, "방 참가에 실패했습니다. 방이 가득 찼거나 존재하지 않습니다.");
            log.warn("사용자 {} 의 방 {} 참가 실패", username, roomId);
        }
    }

    /**
     * 인증된 사용자명 추출
     */
    private String getAuthenticatedUsername(Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        // 1. Principal에서 확인
        if (principal instanceof Authentication) {
            Authentication auth = (Authentication) principal;
            if (auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
                org.springframework.security.core.userdetails.User user =
                        (org.springframework.security.core.userdetails.User) auth.getPrincipal();
                return user.getUsername();
            }
        }

        // 2. 세션에서 확인
        if (headerAccessor != null && headerAccessor.getSessionAttributes() != null) {
            return (String) headerAccessor.getSessionAttributes().get("username");
        }

        // 3. Principal 이름 직접 사용
        return principal != null ? principal.getName() : null;
    }

    /**
     * 에러 메시지 생성
     */
    private ChatMessage createErrorMessage(String content, String roomId) {
        return ChatMessage.builder()
                .type(ChatMessage.MessageType.SYSTEM)
                .content(content)
                .sender("SYSTEM")
                .roomId(roomId)
                .build();
    }

    /**
     * 특정 사용자에게 에러 메시지 전송
     */
    private void sendErrorToUser(String username, String errorMessage) {
        ChatMessage errorMsg = ChatMessage.builder()
                .type(ChatMessage.MessageType.SYSTEM)
                .content(errorMessage)
                .sender("SYSTEM")
                .build();

        messagingTemplate.convertAndSendToUser(username, "/queue/errors", errorMsg);
    }
}