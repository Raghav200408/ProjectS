package com.project.ProjectS.service;

import com.project.ProjectS.model.*;
import com.project.ProjectS.security.service.CustomUserDetails;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * Outermost advice runs after the service transaction has committed. Failed operations
 * never announce success; delivery failures cannot turn a saved result into an API error.
 * Self-invocation (bulk activation / authenticated submit wrappers) emits only once.
 */
@Aspect
@Component
@Order(0)
public class NotificationEventAspect {
    private static final Logger log = LoggerFactory.getLogger(NotificationEventAspect.class);
    private final NotificationEventDelivery delivery;

    public NotificationEventAspect(NotificationEventDelivery delivery) { this.delivery = delivery; }

    @AfterReturning(pointcut = "execution(* com.project.ProjectS.service.ExamService.submitExam(..)) || "
            + "execution(* com.project.ProjectS.service.MockExamService.submitMockExam(..)) || "
            + "execution(* com.project.ProjectS.service.SubscriptionService.activate(..)) || "
            + "execution(* com.project.ProjectS.service.SubscriptionService.activateForEmail(..)) || "
            + "execution(* com.project.ProjectS.service.SubscriptionService.bulkAssign(..))", returning = "result")
    public void completed(Object result) {
        try {
            if (result instanceof ExamSubmitResponseDTO exam) {
                delivery.send(exam.getUserId(), "RESULT_PUBLISHED", "Exam result available",
                        "Your exam result is ready to view.",
                        "/exams/" + exam.getExamId() + "/review/" + exam.getExamResultId());
            } else if (result instanceof MockExamSubmitResponseDTO exam) {
                delivery.send(exam.getUserId(), "RESULT_PUBLISHED", "Mock exam result available",
                        "Your mock exam result is ready to view.",
                        "/mock-exams/" + exam.getMockExamId() + "/review/" + exam.getMockExamResultId());
            } else if (result instanceof UserSubscriptionResponseDTO subscription) {
                activated(subscription);
            } else if (result instanceof List<?> subscriptions) {
                for (Object item : subscriptions) if (item instanceof UserSubscriptionResponseDTO subscription) activated(subscription);
            }
        } catch (RuntimeException error) {
            log.error("Could not deliver notification for completed operation", error);
        }
    }

    private void activated(UserSubscriptionResponseDTO subscription) {
        delivery.send(subscription.getUserId(), "SUBSCRIPTION_ACTIVATED", "Subscription activated",
                "Your subscription is now active. View your subscriptions for details.",
                "/course-subscription?subscriptionId=" + subscription.getSubscriptionId());
    }

    @AfterReturning(pointcut = "execution(* com.project.ProjectS.service.SubscriptionService.deactivate(..)) && args(id,..)",
            argNames = "id")
    public void deactivated(Long id) {
        try { delivery.subscriptionChanged(id, true); }
        catch (RuntimeException error) { log.error("Could not deliver subscription notification", error); }
    }

    @Around("execution(public * com.project.ProjectS.controller.*.upload*(..))")
    public Object upload(ProceedingJoinPoint operation) throws Throwable {
        Object response;
        try {
            response = operation.proceed();
        } catch (Throwable failure) {
            uploadNotification(false);
            throw failure;
        }
        boolean success = !(response instanceof ResponseEntity<?> entity) || entity.getStatusCode().is2xxSuccessful();
        Object body = response instanceof ResponseEntity<?> entity ? entity.getBody() : response;
        if (body instanceof QuestionExcelUploadResponseDTO upload) {
            success = success && upload.isSuccess() && upload.getFailedQuestions() == 0;
        }
        uploadNotification(success);
        return response;
    }

    private void uploadNotification(boolean success) {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails principal)) return;
            delivery.send(principal.getUser().getUserId(), success ? "UPLOAD_COMPLETED" : "UPLOAD_FAILED",
                    success ? "Upload completed" : "Upload needs attention",
                    success ? "Your Excel upload has completed." : "Your upload failed or contains rejected rows. Review the upload response.",
                    null);
        } catch (RuntimeException error) {
            log.error("Could not deliver upload notification", error);
        }
    }
}
