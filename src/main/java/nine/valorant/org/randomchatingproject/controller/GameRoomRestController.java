package nine.valorant.org.randomchatingproject.controller;

import nine.valorant.org.randomchatingproject.entity.ChatMessage;
import nine.valorant.org.randomchatingproject.entity.GameRoom;
import nine.valorant.org.randomchatingproject.service.GameRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class GameRoomRestController {

    private final GameRoomService gameRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 새 게임방 생성
     */
    @PostMapping("/create")
    public ResponseEntity<?> createRoom(@RequestBody CreateRoomRequest request,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "로그인이 필요합니다."));
            }

            String username = userDetails.getUsername();

            if (request.getGameName() == null || request.getGameName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "게임 이름을 입력해주세요."));
            }

            GameRoom room = gameRoomService.createRoom(request.getGameName().trim(), username);

            // 전체 로비에 새 방 생성 알림
            messagingTemplate.convertAndSend("/topic/rooms",
                    ChatMessage.builder()
                            .type(ChatMessage.MessageType.ROOM_CREATED)
                            .content("새로운 방이 생성되었습니다: " + room.getGameName())
                            .sender(room.getCreator())
                            .roomId(room.getRoomId())
                            .build());

            Map<String, Object> response = new HashMap<>();
            response.put("roomId", room.getRoomId());
            response.put("gameName", room.getGameName());
            response.put("creator", room.getCreator());
            response.put("currentPlayers", room.getCurrentPlayers());
            response.put("maxPlayers", room.getMaxPlayers());

            log.info("새 방 생성: {} (ID: {}, 방장: {})", room.getGameName(), room.getRoomId(), username);
            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("방 생성 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "방 생성 중 오류가 발생했습니다."));
        }
    }

    /**
     * 활성 방 목록 조회
     */
    @GetMapping("/list")
    public ResponseEntity<List<GameRoom>> getRoomList() {
        try {
            List<GameRoom> rooms = gameRoomService.getAllActiveRooms();
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            log.error("방 목록 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 방 상세 조회
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<?> getRoom(@PathVariable String roomId) {
        try {
            Optional<GameRoom> room = gameRoomService.getRoom(roomId);

            if (room.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(room.get());
        } catch (Exception e) {
            log.error("방 조회 중 오류 발생: {}", roomId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 내가 참가한 방 목록 조회
     */
    @GetMapping("/my-rooms")
    public ResponseEntity<List<GameRoom>> getMyRooms(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(401).build();
            }

            String username = userDetails.getUsername();
            List<GameRoom> myRooms = gameRoomService.getUserActiveRooms(username);
            return ResponseEntity.ok(myRooms);
        } catch (Exception e) {
            log.error("내 방 목록 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 방 삭제
     */
    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable String roomId,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "로그인이 필요합니다."));
            }

            String username = userDetails.getUsername();
            boolean deleted = gameRoomService.deleteRoom(roomId, username);

            if (deleted) {
                messagingTemplate.convertAndSend("/topic/rooms",
                        ChatMessage.builder()
                                .type(ChatMessage.MessageType.ROOM_UPDATED)
                                .content("방이 삭제되었습니다")
                                .sender("SYSTEM")
                                .build());

                return ResponseEntity.ok(Map.of("message", "방이 성공적으로 삭제되었습니다."));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "방을 삭제할 권한이 없거나 방이 존재하지 않습니다."));
            }
        } catch (Exception e) {
            log.error("방 삭제 중 오류 발생: {}", roomId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "방 삭제 중 오류가 발생했습니다."));
        }
    }

    /**
     * 방 통계 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getRoomStatistics() {
        try {
            Map<String, Object> stats = gameRoomService.getRoomStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("통계 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 방 생성 요청 DTO
     */
    public static class CreateRoomRequest {
        private String gameName;

        public CreateRoomRequest() {}

        public String getGameName() { return gameName; }
        public void setGameName(String gameName) { this.gameName = gameName; }
    }
}