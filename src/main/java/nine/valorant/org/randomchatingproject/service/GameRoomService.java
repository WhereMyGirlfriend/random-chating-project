package nine.valorant.org.randomchatingproject.service;

import nine.valorant.org.randomchatingproject.entity.ChatMessage;
import nine.valorant.org.randomchatingproject.entity.GameRoom;
import nine.valorant.org.randomchatingproject.repository.ChatMessageRepository;
import nine.valorant.org.randomchatingproject.repository.GameRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GameRoomService {

    private final GameRoomRepository gameRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    private static final int MAX_ROOMS_PER_USER = 3;
    private static final int MAX_PARTICIPANTS = 20;

    /**
     * 새 게임방 생성
     */
    public GameRoom createRoom(String gameName, String creator) {
        // 사용자당 방 생성 제한 확인
        long userRoomCount = gameRoomRepository.countByCreatorAndIsActiveTrue(creator);
        if (userRoomCount >= MAX_ROOMS_PER_USER) {
            throw new IllegalStateException("사용자당 최대 " + MAX_ROOMS_PER_USER + "개의 방만 생성할 수 있습니다.");
        }

        // 새 방 생성
        GameRoom room = GameRoom.builder()
                .roomId(UUID.randomUUID().toString())
                .gameName(gameName.trim())
                .creator(creator.trim())
                .participants(new ArrayList<>(List.of(creator.trim())))
                .maxPlayers(MAX_PARTICIPANTS)
                .isActive(true)
                .build();

        GameRoom savedRoom = gameRoomRepository.save(room);

        // 방 생성 메시지 저장
        ChatMessage welcomeMessage = ChatMessage.createSystemMessage(
                "'" + gameName + "' 방이 생성되었습니다. 즐거운 게임 되세요!",
                savedRoom.getRoomId()
        );
        chatMessageRepository.save(welcomeMessage);

        log.info("새 게임방 생성: {} (방장: {}, ID: {})", gameName, creator, savedRoom.getRoomId());
        return savedRoom;
    }

    /**
     * 모든 활성 방 목록 조회
     */
    @Transactional(readOnly = true)
    public List<GameRoom> getAllActiveRooms() {
        return gameRoomRepository.findAllActiveRoomsOrderByCreatedAtDesc();
    }

    /**
     * 특정 방 조회
     */
    @Transactional(readOnly = true)
    public Optional<GameRoom> getRoom(String roomId) {
        return gameRoomRepository.findById(roomId)
                .filter(GameRoom::isActive);
    }

    /**
     * 방에 참가
     */
    public boolean joinRoom(String roomId, String username) {
        Optional<GameRoom> roomOpt = gameRoomRepository.findById(roomId);

        if (roomOpt.isEmpty() || !roomOpt.get().isActive()) {
            log.warn("존재하지 않거나 비활성화된 방에 참가 시도: {}", roomId);
            return false;
        }

        GameRoom room = roomOpt.get();

        // 이미 참가 중인지 확인
        if (room.getParticipants().contains(username)) {
            log.info("사용자 {} 는 이미 방 {} 에 참가 중입니다", username, roomId);
            return true;
        }

        // 방이 가득 찬지 확인
        if (room.isFull()) {
            log.warn("방 {} 이 가득 차서 사용자 {} 참가 실패", roomId, username);
            return false;
        }

        // 참가자 추가
        boolean success = room.addParticipant(username);
        if (success) {
            gameRoomRepository.save(room);

            // 참가 메시지 저장
            ChatMessage joinMessage = ChatMessage.createJoinMessage(username, roomId);
            chatMessageRepository.save(joinMessage);

            log.info("사용자 {} 가 방 {} 에 참가했습니다", username, roomId);
        }

        return success;
    }

    /**
     * 방에서 나가기 (방 자동 삭제 문제 해결)
     */
    public boolean leaveRoom(String roomId, String username) {
        Optional<GameRoom> roomOpt = gameRoomRepository.findById(roomId);

        if (roomOpt.isEmpty()) {
            return false;
        }

        GameRoom room = roomOpt.get();
        boolean removed = room.removeParticipant(username);

        if (removed) {
            // 방장이 나가는 경우 처리
            if (room.isCreator(username) && !room.isEmpty()) {
                // 남은 참가자 중 첫 번째 사람을 새 방장으로 지정
                String newCreator = room.getParticipants().get(0);
                room.setCreator(newCreator);
                log.info("방 {} 의 방장이 {} 에서 {} 로 변경되었습니다", roomId, username, newCreator);

                // 방장 변경 메시지 저장
                ChatMessage creatorChangeMessage = ChatMessage.createSystemMessage(
                        newCreator + "님이 새로운 방장이 되었습니다!", roomId
                );
                chatMessageRepository.save(creatorChangeMessage);
            }

            // 방이 완전히 비어있으면 비활성화 (선택사항)
            // 만약 빈 방도 유지하고 싶다면 이 부분을 주석처리하세요
            if (room.isEmpty()) {
                room.setActive(false);
                log.info("방 {} 이 비어있어 비활성화되었습니다", roomId);
            }

            gameRoomRepository.save(room);

            // 퇴장 메시지 저장
            ChatMessage leaveMessage = ChatMessage.createLeaveMessage(username, roomId);
            chatMessageRepository.save(leaveMessage);

            log.info("사용자 {} 가 방 {} 에서 나갔습니다", username, roomId);
        }

        return removed;
    }

    /**
     * 방 삭제 (방장만 가능)
     */
    public boolean deleteRoom(String roomId, String requesterUsername) {
        Optional<GameRoom> roomOpt = gameRoomRepository.findById(roomId);

        if (roomOpt.isEmpty()) {
            return false;
        }

        GameRoom room = roomOpt.get();

        if (!room.isCreator(requesterUsername)) {
            log.warn("방장이 아닌 사용자 {} 가 방 {} 삭제 시도", requesterUsername, roomId);
            return false;
        }

        // 방 비활성화
        room.setActive(false);
        gameRoomRepository.save(room);

        // 방 삭제 시스템 메시지 저장
        ChatMessage deleteMessage = ChatMessage.createSystemMessage(
                "방장에 의해 방이 삭제되었습니다.", roomId
        );
        chatMessageRepository.save(deleteMessage);

        log.info("방 {} 이 방장 {} 에 의해 삭제되었습니다", roomId, requesterUsername);
        return true;
    }

    /**
     * 사용자가 참여 중인 모든 방에서 나가기
     */
    public List<String> leaveAllRooms(String username) {
        List<GameRoom> userRooms = gameRoomRepository.findActiveRoomsByParticipant(username);
        List<String> leftRooms = new ArrayList<>();

        for (GameRoom room : userRooms) {
            if (room.removeParticipant(username)) {
                // 방장이 나가는 경우 처리
                if (room.isCreator(username) && !room.isEmpty()) {
                    String newCreator = room.getParticipants().get(0);
                    room.setCreator(newCreator);

                    ChatMessage creatorChangeMessage = ChatMessage.createSystemMessage(
                            newCreator + "님이 새로운 방장이 되었습니다!", room.getRoomId()
                    );
                    chatMessageRepository.save(creatorChangeMessage);
                }

                // 방이 비어있으면 비활성화
                if (room.isEmpty()) {
                    room.setActive(false);
                }

                gameRoomRepository.save(room);
                leftRooms.add(room.getRoomId());

                // 퇴장 메시지 저장
                ChatMessage leaveMessage = ChatMessage.createLeaveMessage(username, room.getRoomId());
                chatMessageRepository.save(leaveMessage);
            }
        }

        log.info("사용자 {} 가 {} 개의 방에서 나갔습니다", username, leftRooms.size());
        return leftRooms;
    }

    /**
     * 게임명으로 방 검색
     */
    @Transactional(readOnly = true)
    public List<GameRoom> searchRoomsByGameName(String gameName) {
        return gameRoomRepository.findActiveRoomsByGameNameContainingIgnoreCase(gameName);
    }

    /**
     * 사용자가 참가 중인 방 목록 조회
     */
    @Transactional(readOnly = true)
    public List<GameRoom> getUserActiveRooms(String username) {
        return gameRoomRepository.findActiveRoomsByParticipant(username);
    }

    /**
     * 채팅 메시지 저장
     */
    public ChatMessage saveChatMessage(ChatMessage message) {
        return chatMessageRepository.save(message);
    }

    /**
     * 방 통계 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getRoomStatistics() {
        List<GameRoom> activeRooms = getAllActiveRooms();
        int totalParticipants = activeRooms.stream()
                .mapToInt(GameRoom::getCurrentPlayers)
                .sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRooms", activeRooms.size());
        stats.put("totalParticipants", totalParticipants);
        stats.put("averageParticipants",
                activeRooms.isEmpty() ? 0 : (double) totalParticipants / activeRooms.size());
        stats.put("totalMessages", chatMessageRepository.count());

        return stats;
    }

    /**
     * 방 존재 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean roomExists(String roomId) {
        return gameRoomRepository.existsByRoomIdAndIsActiveTrue(roomId);
    }
}