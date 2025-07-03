package nine.valorant.org.randomchatingproject.config;

import nine.valorant.org.randomchatingproject.entity.ChatMessage;
import nine.valorant.org.randomchatingproject.service.GameRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final GameRoomService gameRoomService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        log.info("새로운 WebSocket 연결 생성됨");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = null;
        String roomId = null;

        // 인증된 사용자 정보 가져오기
        if (headerAccessor.getUser() instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken auth =
                    (UsernamePasswordAuthenticationToken) headerAccessor.getUser();
            username = auth.getName();
        }

        // 세션에서 방 ID 가져오기
        if (headerAccessor.getSessionAttributes() != null) {
            roomId = (String) headerAccessor.getSessionAttributes().get("roomId");
        }

        if (username != null && roomId != null) {
            log.info("사용자 {} 가 방 {} 에서 연결을 해제했습니다", username, roomId);

            boolean removed = gameRoomService.leaveRoom(roomId, username);

            if (removed) {
                ChatMessage leaveMessage = ChatMessage.createLeaveMessage(username, roomId);
                messagingTemplate.convertAndSend("/topic/room/" + roomId, leaveMessage);

                messagingTemplate.convertAndSend("/topic/rooms",
                        ChatMessage.builder()
                                .type(ChatMessage.MessageType.ROOM_UPDATED)
                                .content("방 목록이 업데이트되었습니다")
                                .sender("SYSTEM")
                                .build());
            }
        } else if (username != null) {
            // roomId가 없는 경우 모든 방에서 나가기
            List<String> leftRooms = gameRoomService.leaveAllRooms(username);

            for (String leftRoomId : leftRooms) {
                ChatMessage leaveMessage = ChatMessage.createLeaveMessage(username, leftRoomId);
                messagingTemplate.convertAndSend("/topic/room/" + leftRoomId, leaveMessage);
            }

            if (!leftRooms.isEmpty()) {
                messagingTemplate.convertAndSend("/topic/rooms",
                        ChatMessage.builder()
                                .type(ChatMessage.MessageType.ROOM_UPDATED)
                                .content("방 목록이 업데이트되었습니다")
                                .sender("SYSTEM")
                                .build());
            }
        }
    }
}