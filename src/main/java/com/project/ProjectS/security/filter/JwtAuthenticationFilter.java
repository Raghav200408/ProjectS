package com.project.ProjectS.security.filter;

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
    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }


    private static final Logger logger = LogManager.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

                logger.debug("=================================");
                logger.debug("REQUEST: {} {}", request.getMethod(), request.getRequestURI());

                logger.debug("Authorization Header: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

                    logger.debug("JWT NOT FOUND");

            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {

            String email = jwtUtil.extractEmail(token);

                        logger.debug("JWT EMAIL: {}", email);

            if (email != null &&
                    SecurityContextHolder.getContext()
                            .getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(email);

                logger.debug("AUTHORITIES: {}", userDetails.getAuthorities());

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
                } else {

                    logger.warn("JWT INVALID");
                }
            }

        } catch (Exception e) {

            logger.error("JWT AUTHENTICATION ERROR: {}", e.getMessage(), e);

                        SecurityContextHolder.clearContext();
        }

        logger.debug("=================================");

        filterChain.doFilter(request, response);
    }
}
