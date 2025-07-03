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

import java.security.Principal;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final GameRoomService gameRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 채팅 메시지 전송
     */
    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public ChatMessage sendMessage(@DestinationVariable String roomId,
                                   @Payload ChatMessage chatMessage,
                                   Principal principal) {

        if (principal == null) {
            log.warn("인증되지 않은 사용자의 메시지 전송 시도");
            return null;
        }

        String authenticatedUsername = principal.getName();

        Optional<GameRoom> room = gameRoomService.getRoom(roomId);
        if (room.isEmpty() || !room.get().isActive()) {
            log.warn("존재하지 않거나 비활성화된 방에 메시지 전송 시도: {}", roomId);
            return null;
        }

        if (!room.get().getParticipants().contains(authenticatedUsername)) {
            log.warn("방에 참가하지 않은 사용자의 메시지 전송 시도: {} in {}", authenticatedUsername, roomId);
            return null;
        }

        chatMessage.setSender(authenticatedUsername);
        chatMessage.setRoomId(roomId);
        chatMessage.setType(ChatMessage.MessageType.CHAT);

        gameRoomService.saveChatMessage(chatMessage);

        log.info("방 {} 에서 {} 가 메시지 전송: {}", roomId, authenticatedUsername, chatMessage.getContent());

        return chatMessage;
    }

    /**
     * 사용자 방 참가 처리
     */
    @MessageMapping("/chat.addUser/{roomId}")
    public void addUser(@DestinationVariable String roomId,
                        SimpMessageHeaderAccessor headerAccessor,
                        Principal principal) {

        if (principal == null) {
            log.warn("인증되지 않은 사용자의 방 참가 시도");
            return;
        }

        String username = principal.getName();

        headerAccessor.getSessionAttributes().put("username", username);
        headerAccessor.getSessionAttributes().put("roomId", roomId);

        boolean joinSuccess = gameRoomService.joinRoom(roomId, username);

        if (joinSuccess) {
            ChatMessage joinMessage = ChatMessage.createJoinMessage(username, roomId);

            messagingTemplate.convertAndSend("/topic/room/" + roomId, joinMessage);

            messagingTemplate.convertAndSend("/topic/rooms",
                    ChatMessage.builder()
                            .type(ChatMessage.MessageType.ROOM_UPDATED)
                            .content("방 목록이 업데이트되었습니다")
                            .sender("SYSTEM")
                            .build());

            log.info("사용자 {} 가 방 {} 에 성공적으로 참가했습니다", username, roomId);
        } else {
            messagingTemplate.convertAndSendToUser(username, "/queue/errors",
                    ChatMessage.createSystemMessage("방 참가에 실패했습니다. 방이 가득 찼거나 존재하지 않습니다.", roomId));

            log.warn("사용자 {} 의 방 {} 참가 실패", username, roomId);
        }
    }
}