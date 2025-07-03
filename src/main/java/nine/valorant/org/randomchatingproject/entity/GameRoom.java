package nine.valorant.org.randomchatingproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "game_rooms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRoom {

    @Id
    @Column(name = "room_id", length = 36)
    private String roomId;

    @Column(name = "game_name", nullable = false, length = 100)
    private String gameName;

    @Column(name = "creator", nullable = false, length = 50)
    private String creator;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "room_participants",
            joinColumns = @JoinColumn(name = "room_id")
    )
    @Column(name = "username", length = 50)
    @Builder.Default
    private List<String> participants = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "max_players")
    @Builder.Default
    private int maxPlayers = 10;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;

    // 비즈니스 로직 메서드들
    public int getCurrentPlayers() {
        return participants != null ? participants.size() : 0;
    }

    public boolean addParticipant(String username) {
        if (participants == null) {
            participants = new ArrayList<>();
        }

        if (participants.size() < maxPlayers && !participants.contains(username)) {
            participants.add(username);
            return true;
        }
        return false;
    }

    public boolean removeParticipant(String username) {
        if (participants != null) {
            return participants.remove(username);
        }
        return false;
    }

    public boolean isFull() {
        return getCurrentPlayers() >= maxPlayers;
    }

    public boolean isEmpty() {
        return getCurrentPlayers() == 0;
    }

    public boolean isCreator(String username) {
        return creator != null && creator.equals(username);
    }
}