package com.weolbu.assignment.config;
import lombok.extern.slf4j.Slf4j;
import com.weolbu.assignment.service.AuthService;
import com.weolbu.assignment.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String jwt = null;
        Long userId = null;

        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
                Claims claims = jwtUtil.parseToken(jwt);

                // 토큰이 Access Token인지 확인
                if ("access".equals(claims.get("tokenType", String.class))) {
                    userId = Long.valueOf(claims.getSubject());
                }
            }

            // 인증 정보 설정
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                validateAndAuthenticateUser(jwt, userId, request);
            }

        } catch (ExpiredJwtException e) {
            // Access Token 만료 시, 예외를 발생시키지 않고 재발급 API로 요청 전달
            // Access Token 만료된 경우 요청 속성에 표시
            request.setAttribute("expired", true);
            request.setAttribute("expiredToken", jwt);

        } catch (Exception e) {
            handleException(response, HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.");
            return;
        }

        // 다음 필터로 진행
        chain.doFilter(request, response);
    }

    private void validateAndAuthenticateUser(String jwt, Long userId, HttpServletRequest request) {
        if (jwtUtil.validateAccessToken(jwt)) {
            UserDetails userDetails = authService.loadUserById(userId);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
    }

    private void handleException(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write("{\"message\": \"" + message + "\"}");
    }
}