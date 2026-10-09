package com.project.ProjectS.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import org.apache.logging.log4j.ThreadContext;

@Configuration
public class RequestLoggingConfig {

    private static final Logger log = LogManager.getLogger(RequestLoggingConfig.class);

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> requestLoggingFilter() {
        OncePerRequestFilter filter = new OncePerRequestFilter() {
            @Override
            protected boolean shouldNotFilter(HttpServletRequest request) {
                String path = request.getRequestURI();
                return path.startsWith("/actuator") || "/error".equals(path);
            }

            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                long startTime = System.currentTimeMillis();
                String method = request.getMethod();
                String uri = request.getRequestURI();
                String queryString = request.getQueryString();
                String remoteAddr = request.getRemoteAddr();
                String requestId = UUID.randomUUID().toString().substring(0, 8);
                ThreadContext.put("requestId", requestId);

                log.info("REQUEST START method={} uri={} queryString={} remoteAddr={} requestId={}",
                        method, uri, queryString, remoteAddr, requestId);

                try {
                    filterChain.doFilter(request, response);
                } finally {
                    long duration = System.currentTimeMillis() - startTime;
                    log.info("REQUEST END method={} uri={} status={} durationMs={} requestId={}",
                            method, uri, response.getStatus(), duration, requestId);
                    ThreadContext.remove("requestId");
                }
            }
        };

        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Integer.MIN_VALUE);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
