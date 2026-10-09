package com.project.ProjectS.security.filter;

import com.project.ProjectS.config.AuditLogger;
import com.project.ProjectS.security.jwt.JwtUtil;
import com.project.ProjectS.security.service.CustomUserDetailsService;

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
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService, AuditLogger auditLogger) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.auditLogger = auditLogger;
    }


    private static final Logger logger = LogManager.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final AuditLogger auditLogger;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

                logger.debug("=================================");
                logger.debug("REQUEST: {} {}", request.getMethod(), request.getRequestURI());


        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

                    logger.debug("JWT NOT FOUND");
            auditLogger.log("TOKEN_VALIDATION", request.getRemoteUser(), "REQUEST", null, "MISSING", request.getRequestURI());

            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {

            String email = jwtUtil.extractEmail(token);

                        logger.debug("JWT EMAIL: {}", email);
            auditLogger.log("TOKEN_VALIDATION", email, "JWT", null, "ATTEMPT", request.getRequestURI());

            if (email != null &&
                    SecurityContextHolder.getContext()
                            .getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(email);

                logger.debug("AUTHORITIES: {}", userDetails.getAuthorities());

                if (!userDetails.isEnabled()) {
                    SecurityContextHolder.clearContext();
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User account is inactive");
                    return;
                }

                if (jwtUtil.isTokenValid(token)) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authentication);

                    logger.info("JWT AUTHENTICATION SUCCESS");
                    auditLogger.log("TOKEN_VALIDATION", email, "JWT", null, "SUCCESS", request.getRequestURI());
                } else {

                    logger.warn("JWT INVALID");
                    auditLogger.log("TOKEN_VALIDATION", email, "JWT", null, "INVALID", request.getRequestURI());
                }
            }

        } catch (Exception e) {

            logger.error("JWT AUTHENTICATION ERROR: {}", e.getMessage(), e);
            auditLogger.log("TOKEN_VALIDATION", null, "JWT", null, "ERROR", e.getMessage());

                        SecurityContextHolder.clearContext();
        }

        logger.debug("=================================");

        filterChain.doFilter(request, response);
    }
}
