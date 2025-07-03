package nine.valorant.org.randomchatingproject.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    private final Key key;

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24시간

    // 시크릿 키를 application.properties에서 주입받아 Key 객체로 변환
    public JwtProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT Provider 초기화 완료");
    }

    // Access Token 생성
    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_TIME);

        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        log.info("JWT 토큰 생성: username={}, expiry={}", username, expiry);
        return token;
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            log.debug("JWT 토큰 검증 성공: username={}", claims.getSubject());
            return true;

        } catch (SecurityException | MalformedJwtException e) {
            log.warn("유효하지 않은 JWT 서명: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 JWT 토큰: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 JWT 토큰: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT 토큰 검증 중 예외 발생: {}", e.getMessage());
        }
        return false;
    }

    // 토큰에서 사용자명 추출
    public String getUsername(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            log.debug("JWT에서 사용자명 추출: {}", username);
            return username;

        } catch (Exception e) {
            log.error("JWT에서 사용자명 추출 실패: {}", e.getMessage());
            throw new RuntimeException("JWT 파싱 실패", e);
        }
    }
}