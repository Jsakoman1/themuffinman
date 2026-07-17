package com.themuffinman.app.business.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.themuffinman.app.business.dto.BusinessBookingListResponseDTO;
import com.themuffinman.app.business.dto.BusinessBookingQueryDTO;
import com.themuffinman.app.business.dto.BusinessBookingResponseDTO;
import com.themuffinman.app.business.dto.BusinessOwnerCalendarProjectionDTO;
import com.themuffinman.app.business.dto.BusinessOwnerScheduleSummaryDTO;
import com.themuffinman.app.business.service.BusinessBookingReadService;
import com.themuffinman.app.business.service.BusinessCancelBookingUseCase;
import com.themuffinman.app.business.service.BusinessCompleteBookingUseCase;
import com.themuffinman.app.business.service.BusinessConfirmBookingUseCase;
import com.themuffinman.app.business.service.BusinessCreateBookingUseCase;
import com.themuffinman.app.business.service.BusinessNoShowBookingUseCase;
import com.themuffinman.app.business.service.BusinessOwnerCalendarReadService;
import com.themuffinman.app.business.service.BusinessOwnerScheduleReadService;
import com.themuffinman.app.business.service.BusinessRejectBookingUseCase;
import com.themuffinman.app.business.service.BusinessRescheduleBookingUseCase;
import com.themuffinman.app.common.controller.GlobalExceptionHandler;
import com.themuffinman.app.identity.model.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BusinessBookingControllerTest {

    private final BusinessCreateBookingUseCase businessCreateBookingUseCase = mock(BusinessCreateBookingUseCase.class);
    private final BusinessBookingReadService businessBookingReadService = mock(BusinessBookingReadService.class);
    private final BusinessCancelBookingUseCase businessCancelBookingUseCase = mock(BusinessCancelBookingUseCase.class);
    private final BusinessConfirmBookingUseCase businessConfirmBookingUseCase = mock(BusinessConfirmBookingUseCase.class);
    private final BusinessRejectBookingUseCase businessRejectBookingUseCase = mock(BusinessRejectBookingUseCase.class);
    private final BusinessCompleteBookingUseCase businessCompleteBookingUseCase = mock(BusinessCompleteBookingUseCase.class);
    private final BusinessNoShowBookingUseCase businessNoShowBookingUseCase = mock(BusinessNoShowBookingUseCase.class);
    private final BusinessOwnerScheduleReadService businessOwnerScheduleReadService = mock(BusinessOwnerScheduleReadService.class);
    private final BusinessOwnerCalendarReadService businessOwnerCalendarReadService = mock(BusinessOwnerCalendarReadService.class);
    private final BusinessRescheduleBookingUseCase businessRescheduleBookingUseCase = mock(BusinessRescheduleBookingUseCase.class);
    private MockMvc mockMvc;
    private AppUser currentUser;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        BusinessBookingController controller = new BusinessBookingController(
                businessCreateBookingUseCase,
                businessBookingReadService,
                businessCancelBookingUseCase,
                businessConfirmBookingUseCase,
                businessRejectBookingUseCase,
                businessCompleteBookingUseCase,
                businessNoShowBookingUseCase,
                businessOwnerScheduleReadService,
                businessOwnerCalendarReadService,
                businessRescheduleBookingUseCase
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setValidator(validator)
                .build();
        currentUser = new AppUser();
        currentUser.setId(1L);
        currentUser.setUsername("owner");
        currentUser.setEmail("owner@example.com");
        currentUser.setPasswordHash("hash");
    }

    @Test
    void getOwnerCalendarDelegatesWithOptionalRangeParams() throws Exception {
        authenticateCurrentUser();
        Instant from = Instant.parse("2026-07-08T00:00:00Z");
        Instant to = Instant.parse("2026-07-11T00:00:00Z");
        when(businessOwnerCalendarReadService.getMyCalendar(currentUser, from, to))
                .thenReturn(BusinessOwnerCalendarProjectionDTO.builder()
                        .timezone("Europe/Zurich")
                        .from(from)
                        .to(to)
                        .totalBookings(2)
                        .days(List.of())
                        .build());

        mockMvc.perform(get("/business/bookings/owner/calendar")
                        .param("from", "2026-07-08T00:00:00Z")
                        .param("to", "2026-07-11T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timezone").value("Europe/Zurich"))
                .andExpect(jsonPath("$.totalBookings").value(2));

        verify(businessOwnerCalendarReadService).getMyCalendar(currentUser, from, to);
    }

    private void authenticateCurrentUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser, "credentials", List.of())
        );
    }
}
