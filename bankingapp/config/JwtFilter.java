package com.shravya.bankingapp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }



    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        // 🛡️ 1. BYPASS FOR PUBLIC PATHS
        // If someone is logging in or registering, don't even look at the token!
        if (path.contains("/auth/login") || path.contains("/users/register") || path.contains("/users/verify-registration")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 🛡️ 2. TOKEN VALIDATION (For everything else)
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    String grantedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(email, null, List.of(new SimpleGrantedAuthority(grantedRole)));
                    authentication.setDetails(request);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } else {
                // If the token exists but is wrong (Signature mismatch), stop here.
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\": \"Session Expired. Please Login Again.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

}
