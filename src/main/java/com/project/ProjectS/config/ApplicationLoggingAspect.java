package com.project.ProjectS.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
public class ApplicationLoggingAspect {

    private static final Logger log = LogManager.getLogger(ApplicationLoggingAspect.class);
    private static final int MAX_ARG_LENGTH = 200;

    @Around("execution(* com.project.ProjectS.controller..*(..)) || " +
            "execution(* com.project.ProjectS.service..*(..)) || " +
            "execution(* com.project.ProjectS.security..*(..)) || " +
            "execution(* com.project.ProjectS.config..*(..))")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String args = Arrays.stream(joinPoint.getArgs())
                .map(this::sanitize)
                .collect(Collectors.joining(", ", "[", "]"));

        long start = System.currentTimeMillis();
        log.info("ENTER {}.{} args={}", className, methodName, args);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            String resultSummary = result == null ? "void" : result.getClass().getSimpleName();
            log.info("EXIT {}.{} durationMs={} result={}", className, methodName, duration, resultSummary);
            return result;
        } catch (Throwable throwable) {
            long duration = System.currentTimeMillis() - start;
            log.error("ERROR {}.{} durationMs={} message={}", className, methodName, duration, throwable.getMessage(), throwable);
            throw throwable;
        }
    }

    private String sanitize(Object value) {
        if (value == null) {
            return "null";
        }

        String text = value.toString();
        String lower = text.toLowerCase();
        if (lower.contains("password") || lower.contains("secret") || lower.contains("token") || lower.contains("authorization")) {
            return "[REDACTED]";
        }

        if (text.length() > MAX_ARG_LENGTH) {
            return text.substring(0, MAX_ARG_LENGTH) + "...";
        }
        return text;
    }
}
