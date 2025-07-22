package gift.util;

import gift.api.member.domain.MemberRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    // Header KEY 값
    public static final String AUTHORIZATION_HEADER = "Authorization";
    // 사용자 권한 값의 KEY
    public static final String AUTHORIZATION_KEY = "auth";
    // Token 식별자
    public static final String BEARER_PREFIX = "Bearer ";
    // 토큰 만료시간
    private final long TOKEN_TIME = 60 * 60 * 1000; // 60분

    @Value("${jwt.secret}")
    private String secretKey;
    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String createToken(String username, MemberRole role) {
        Date date = new Date();

        return BEARER_PREFIX +
                Jwts.builder()
                        .subject(username)
                        .claim(AUTHORIZATION_KEY, role)
                        .expiration(new Date(date.getTime() + TOKEN_TIME))
                        .issuedAt(date)
                        .signWith(this.key)
                        .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getUserInfoFromToken(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    // 1. 토큰을 쿠키에 저장하는 메서드
    public void addJwtToCookie(String token, HttpServletResponse res) {
        token = URLEncoder.encode(token, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        ResponseCookie cookie = ResponseCookie.from(AUTHORIZATION_HEADER, token)
                .path("/")
                .httpOnly(true)
                .secure(false) // HTTPS 환경에서는 true로 설정
                .sameSite("Lax") // CSRF 방어를 위한 SameSite=Lax 설정
                .build();

        res.addHeader("Set-Cookie", cookie.toString());
    }

    // 2. 쿠키에서 토큰을 가져오는 메서드
    public String getTokenFromRequest(HttpServletRequest req) {
        // Authorization 헤더에서 Bearer 토큰을 가져옵니다.
        String bearerToken = req.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken;
        }

        // 쿠키에서 토큰을 가져옵니다.
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(AUTHORIZATION_HEADER)) {
                    return URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
                }
            }
        }

        return null;
    }
}