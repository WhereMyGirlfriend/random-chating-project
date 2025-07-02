package nine.valorant.org.randomchatingproject.repository;

import nine.valorant.org.randomchatingproject.entity.VerifyMails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerifyMailRepository extends JpaRepository<VerifyMails, Long> {
    Optional<VerifyMails> findByCode(String code);
}
