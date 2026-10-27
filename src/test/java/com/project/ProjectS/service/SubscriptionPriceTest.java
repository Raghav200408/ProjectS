package com.project.ProjectS.service;

import com.project.ProjectS.config.AuditLogger;
import com.project.ProjectS.entity.SubscriptionPlan;
import com.project.ProjectS.model.SubscriptionPlanRequestDTO;
import com.project.ProjectS.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SubscriptionPriceTest {
    private final SubscriptionPlanRepository plans = mock(SubscriptionPlanRepository.class);
    private final SubscriptionService service = new SubscriptionService(plans, mock(PlanCourseRepository.class),
            mock(UserSubscriptionRepository.class), mock(CourseRepository.class), mock(UserRepository.class), mock(AuditLogger.class));

    private SubscriptionPlanRequestDTO request(String price) {
        var request = new SubscriptionPlanRequestDTO();
        request.setName("Paid plan");
        if (price != null) request.setPrice(new BigDecimal(price));
        return request;
    }

    @Test void createPersistsAndReturnsDecimalPrice() {
        when(plans.save(any())).thenAnswer(invocation -> {
            SubscriptionPlan plan = invocation.getArgument(0);
            plan.setPlanId(1L);
            return plan;
        });
        var response = service.create(request("499.50"));
        assertEquals(new BigDecimal("499.50"), response.getPrice());
        verify(plans).save(argThat(plan -> new BigDecimal("499.50").equals(plan.getPrice())));
    }

    @Test void updateAndReloadRetainPriceAndOmittedPriceDoesNotEraseIt() {
        var plan = new SubscriptionPlan();
        plan.setPlanId(1L);
        when(plans.findById(1L)).thenReturn(Optional.of(plan));
        when(plans.findAll()).thenReturn(java.util.List.of(plan));
        assertEquals(new BigDecimal("1250.75"), service.update(1L, request("1250.75")).getPrice());
        service.update(1L, request(null));
        assertEquals(new BigDecimal("1250.75"), service.allPlans().get(0).getPrice());
        assertEquals(BigDecimal.ZERO, service.update(1L, request("0")).getPrice());
    }

    @Test void rejectsInvalidPricesBeforeSaving() {
        for (String price : java.util.List.of("-1", "1.001", "10000000000")) {
            assertThrows(ResponseStatusException.class, () -> service.create(request(price)));
        }
        verify(plans, never()).save(any());
    }
}
