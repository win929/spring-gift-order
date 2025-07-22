package gift.interceptor;

import gift.api.member.domain.MemberRole;
import gift.exception.auth.AuthenticationException;
import gift.exception.auth.AuthorizationException;
import gift.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthenticationInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) {
        String token = jwtUtil.getTokenFromRequest(request);

        if (token == null || !token.startsWith(JwtUtil.BEARER_PREFIX)) {
            throw new AuthenticationException("로그인이 필요한 페이지입니다.");
        }

        String jwt = token.substring(JwtUtil.BEARER_PREFIX.length());
        if (!jwtUtil.validateToken(jwt)) {
            throw new AuthenticationException("유효하지 않은 토큰입니다.");
        }

        Claims userInfo = jwtUtil.getUserInfoFromToken(jwt);
        String userRole = userInfo.get(JwtUtil.AUTHORIZATION_KEY, String.class);
        request.setAttribute("userEmail", userInfo.getSubject());
        request.setAttribute("userRole", userRole);

        String uri = request.getRequestURI();
        if (uri.startsWith("/admin")) {
            if (!MemberRole.ADMIN.name().equals(userRole)) {
                throw new AuthorizationException("관리자 권한이 필요한 페이지입니다.");
            }
        }

        return true;
    }
}
