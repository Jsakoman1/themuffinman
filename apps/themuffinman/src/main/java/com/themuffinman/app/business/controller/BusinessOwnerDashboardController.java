package com.themuffinman.app.business.controller;

import com.themuffinman.app.business.dto.BusinessOwnerDashboardDTO;
import com.themuffinman.app.business.dto.BusinessOwnerScheduleSummaryDTO;
import com.themuffinman.app.business.service.BusinessOwnerDashboardReadService;
import com.themuffinman.app.business.service.BusinessOwnerScheduleReadService;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business/dashboard")
@RequiredArgsConstructor
public class BusinessOwnerDashboardController {

    private final BusinessOwnerDashboardReadService businessOwnerDashboardReadService;
    private final BusinessOwnerScheduleReadService businessOwnerScheduleReadService;

    @GetMapping("/me")
    public BusinessOwnerDashboardDTO getMyDashboard(
            @RequestParam(required = false) Long businessProfileId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return businessProfileId == null
                ? businessOwnerDashboardReadService.getMyDashboard(currentUser)
                : businessOwnerDashboardReadService.getMyDashboard(currentUser, businessProfileId);
    }

    @GetMapping("/me/schedule")
    public BusinessOwnerScheduleSummaryDTO getMySchedule(@AuthenticationPrincipal AppUser currentUser) {
        return businessOwnerScheduleReadService.getMyScheduleSummary(currentUser);
    }
}
