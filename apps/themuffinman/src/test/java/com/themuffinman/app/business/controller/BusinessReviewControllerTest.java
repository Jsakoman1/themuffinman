package com.themuffinman.app.business.controller;

import com.themuffinman.app.business.dto.BusinessReviewResponseDTO;
import com.themuffinman.app.business.service.BusinessReviewService;
import com.themuffinman.app.common.controller.GlobalExceptionHandler;
import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BusinessReviewControllerTest {

    private final BusinessReviewService businessReviewService = mock(BusinessReviewService.class);
    private MockMvc mockMvc;
    private AppUser currentUser;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new BusinessReviewController(businessReviewService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setValidator(validator)
                .build();
        currentUser = new AppUser();
        currentUser.setId(1L);
        currentUser.setUsername("customer");
        currentUser.setEmail("customer@example.com");
        currentUser.setPasswordHash("hash");
    }

    @Test
    void createsReviewForAuthenticatedCustomer() throws Exception {
        authenticateCurrentUser();
        when(businessReviewService.createOrUpdateReview(eq(10L), any(), eq(currentUser)))
                .thenReturn(BusinessReviewResponseDTO.builder()
                        .id(4L).bookingId(10L).reviewerUsername("customer").stars(5)
                        .serviceTitle("Haircut").createdAt(Instant.now()).updatedAt(Instant.now()).build());

        mockMvc.perform(post("/business/bookings/me/10/review")
                        .contentType("application/json")
                        .content("{\"stars\":5,\"comment\":\"Excellent\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stars").value(5))
                .andExpect(jsonPath("$.serviceTitle").value("Haircut"));

        verify(businessReviewService).createOrUpdateReview(eq(10L), any(), eq(currentUser));
    }

    private void authenticateCurrentUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser, "credentials", List.of())
        );
    }
}
