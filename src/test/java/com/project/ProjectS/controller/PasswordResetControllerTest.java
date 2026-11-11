package com.project.ProjectS.controller;

import com.project.ProjectS.model.ForgotPasswordRequestDTO;
import com.project.ProjectS.service.ForgotPasswordRateLimiter;
import com.project.ProjectS.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PasswordResetControllerTest {
    private final PasswordResetService service = mock(PasswordResetService.class);
    private final PasswordResetController controller = new PasswordResetController(service, new ForgotPasswordRateLimiter());

    @Test
    void forgotPasswordAlwaysReturnsTheSameGenericMessage() {
        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO();
        request.setEmail("student@example.com");
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("192.0.2.10");

        var response = controller.forgotPassword(request, httpRequest);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(PasswordResetService.GENERIC_FORGOT_RESPONSE, response.getBody().get("message"));
        verify(service).requestReset("student@example.com");
    }

    @Test
    void forgotPasswordRateLimitsWithoutRevealingAccountState() {
        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO();
        request.setEmail("student@example.com");
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("192.0.2.11");
        for (int i = 0; i < 5; i++) controller.forgotPassword(request, httpRequest);

        var error = org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> controller.forgotPassword(request, httpRequest));
        assertEquals(429, error.getStatusCode().value());
        verify(service, times(5)).requestReset("student@example.com");
    }
}
