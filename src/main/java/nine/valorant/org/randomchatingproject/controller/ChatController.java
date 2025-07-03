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
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final GameRoomService gameRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 채팅 메시지 전송 (테스트용: 인증 체크 제거)
     */
    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public ChatMessage sendMessage(@DestinationVariable String roomId,
                                   @Payload ChatMessage chatMessage) {

        // 방이 존재하는지 확인
        Optional<GameRoom> room = gameRoomService.getRoom(roomId);
        if (room.isEmpty() || !room.get().isActive()) {
            log.warn("존재하지 않거나 비활성화된 방에 메시지 전송 시도: {}", roomId);
            return null;
        }

        // 메시지 설정
        chatMessage.setRoomId(roomId);
        chatMessage.setType(ChatMessage.MessageType.CHAT);

        // 테스트용: sender가 null인 경우 기본값 설정
        if (chatMessage.getSender() == null || chatMessage.getSender().trim().isEmpty()) {
            chatMessage.setSender("TestUser");
        }

        // 메시지 저장
        gameRoomService.saveChatMessage(chatMessage);

        log.info("방 {} 에서 {} 가 메시지 전송: {}", roomId, chatMessage.getSender(), chatMessage.getContent());

        return chatMessage;
    }

    /**
     * 사용자 방 참가 처리 (임시: 인증 체크 완전 제거)
     */
    @MessageMapping("/chat.addUser/{roomId}")
    public void addUser(@DestinationVariable String roomId,
                        @Payload ChatMessage chatMessage,
                        SimpMessageHeaderAccessor headerAccessor) {

        // 🚫 임시: 기본 사용자명 설정
        String username = "TestUser";
        if (chatMessage.getSender() != null && !chatMessage.getSender().trim().isEmpty()) {
            username = chatMessage.getSender();
        }

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
            // 참가 실패 시 개별 사용자에게 알림
            ChatMessage errorMessage = ChatMessage.createSystemMessage(
                    "방 참가에 실패했습니다. 방이 가득 찼거나 존재하지 않습니다.", roomId);
            messagingTemplate.convertAndSend("/topic/room/" + roomId, errorMessage);

            log.warn("사용자 {} 의 방 {} 참가 실패", username, roomId);
        }
    }
}