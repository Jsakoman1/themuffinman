package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.location.dto.ExactLocationVisibilityScopeOptionDTO;
import com.themuffinman.app.location.dto.LocationModeOptionDTO;
import com.themuffinman.app.location.dto.QuestLocationVisibilityOptionDTO;
import com.themuffinman.app.location.model.ExactLocationVisibilityScope;
import com.themuffinman.app.location.model.QuestLocationVisibility;
import com.themuffinman.app.location.model.UserLocationMode;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.service.LocationGeoService;
import com.themuffinman.app.workmarket.dto.AppUserRoleOptionDTO;
import com.themuffinman.app.workmarket.dto.QuestSearchDefaultsDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationStatusFilterOptionDTO;
import com.themuffinman.app.workmarket.dto.QuestAudienceFilterOptionDTO;
import com.themuffinman.app.workmarket.dto.QuestAudienceOptionDTO;
import com.themuffinman.app.workmarket.dto.QuestSortOptionDTO;
import com.themuffinman.app.workmarket.dto.QuestStatusFilterOptionDTO;
import com.themuffinman.app.workmarket.dto.QuestStatusOptionDTO;
import com.themuffinman.app.workmarket.dto.WorkmarketOptionsDTO;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestAudience;
import com.themuffinman.app.workmarket.model.QuestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkmarketOptionsService {
    private final LocationGeoService locationGeoService;

    public WorkmarketOptionsDTO getOptions(AppUser currentUser) {
        boolean hasViewerLocation = currentUser != null
                && currentUser.getLocationMode() != null
                && currentUser.getLocationMode() != UserLocationMode.OFF
                && locationGeoService.hasCoordinates(currentUser.getLocationLatitude(), currentUser.getLocationLongitude());
        return WorkmarketOptionsDTO.builder()
                .appUserRoles(List.of(
                        AppUserRoleOptionDTO.builder().value(AppUserRole.USER).label("User").build(),
                        AppUserRoleOptionDTO.builder().value(AppUserRole.ADMIN).label("Admin").build()
                ))
                .questStatusFilters(List.of(
                        QuestStatusFilterOptionDTO.builder().value("ALL").label("All").build(),
                        QuestStatusFilterOptionDTO.builder().value(QuestStatus.OPEN.name()).label("Open").build(),
                        QuestStatusFilterOptionDTO.builder().value(QuestStatus.ASSIGNED.name()).label("Assigned").build(),
                        QuestStatusFilterOptionDTO.builder().value(QuestStatus.WAITING_CONFIRMATION.name()).label("Waiting confirmation").build(),
                        QuestStatusFilterOptionDTO.builder().value(QuestStatus.IN_PROGRESS.name()).label("In progress").build(),
                        QuestStatusFilterOptionDTO.builder().value(QuestStatus.COMPLETED.name()).label("Completed").build(),
                        QuestStatusFilterOptionDTO.builder().value(QuestStatus.CANCELLED.name()).label("Cancelled").build()
                ))
                .questApplicationStatusFilters(List.of(
                        QuestApplicationStatusFilterOptionDTO.builder().value("ALL").label("All").build(),
                        QuestApplicationStatusFilterOptionDTO.builder().value(QuestApplicationStatus.PENDING.name()).label("Pending").build(),
                        QuestApplicationStatusFilterOptionDTO.builder().value(QuestApplicationStatus.APPROVED.name()).label("Approved").build(),
                        QuestApplicationStatusFilterOptionDTO.builder().value(QuestApplicationStatus.DECLINED.name()).label("Declined").build(),
                        QuestApplicationStatusFilterOptionDTO.builder().value(QuestApplicationStatus.WITHDRAWN.name()).label("Withdrawn").build()
                ))
                .questStatuses(List.of(
                        QuestStatusOptionDTO.builder().value(QuestStatus.OPEN).label("Open").build(),
                        QuestStatusOptionDTO.builder().value(QuestStatus.ASSIGNED).label("Assigned").build(),
                        QuestStatusOptionDTO.builder().value(QuestStatus.WAITING_CONFIRMATION).label("Waiting confirmation").build(),
                        QuestStatusOptionDTO.builder().value(QuestStatus.IN_PROGRESS).label("In progress").build(),
                        QuestStatusOptionDTO.builder().value(QuestStatus.COMPLETED).label("Completed").build(),
                        QuestStatusOptionDTO.builder().value(QuestStatus.CANCELLED).label("Cancelled").build()
                ))
                .questAudiences(List.of(
                        QuestAudienceOptionDTO.builder()
                                .value(QuestAudience.CIRCLES)
                                .label("Circles")
                                .description("Visible to selected circles")
                                .build(),
                        QuestAudienceOptionDTO.builder()
                                .value(QuestAudience.EVERYONE)
                                .label("Everyone")
                                .description("Visible to everyone on the platform")
                                .build()
                ))
                .questAudienceFilters(List.of(
                        QuestAudienceFilterOptionDTO.builder().value("ALL").label("All").build(),
                        QuestAudienceFilterOptionDTO.builder().value(QuestAudience.CIRCLES.name()).label("Circles").build(),
                        QuestAudienceFilterOptionDTO.builder().value(QuestAudience.EVERYONE.name()).label("Everyone").build()
                ))
                .questSortOptions(List.of(
                        QuestSortOptionDTO.builder().value("recommended").label("Best match").build(),
                        QuestSortOptionDTO.builder().value("newest").label("Soonest").build(),
                        QuestSortOptionDTO.builder().value("highest").label("Highest award").build()
                ))
                .questSearchDefaults(QuestSearchDefaultsDTO.builder()
                        .defaultSort("recommended")
                        .defaultRadiusKm(10)
                        .radiusOptionsKm(List.of(5, 10, 20, 30))
                        .hasViewerLocation(hasViewerLocation)
                        .nearbyDefaultEnabled(hasViewerLocation)
                        .build())
                .locationModes(List.of(
                        LocationModeOptionDTO.builder().value(UserLocationMode.OFF).label("Off").description("Do not use my location").build(),
                        LocationModeOptionDTO.builder().value(UserLocationMode.APPROXIMATE).label("Approximate area").description("Use your area for nearby job search").build(),
                        LocationModeOptionDTO.builder().value(UserLocationMode.EXACT).label("Exact address").description("Use your exact address when you allow it").build()
                ))
                .exactLocationVisibilityScopes(List.of(
                        ExactLocationVisibilityScopeOptionDTO.builder().value(ExactLocationVisibilityScope.NOBODY).label("Nobody").description("Keep exact address private").build(),
                        ExactLocationVisibilityScopeOptionDTO.builder().value(ExactLocationVisibilityScope.EVERYONE).label("Everyone").description("Anyone who can view your job can see the exact address").build(),
                        ExactLocationVisibilityScopeOptionDTO.builder().value(ExactLocationVisibilityScope.CIRCLES).label("Selected circles").description("Only people from selected circles can see the exact address").build(),
                        ExactLocationVisibilityScopeOptionDTO.builder().value(ExactLocationVisibilityScope.USERS).label("Selected people").description("Only selected people can see the exact address").build()
                ))
                .questLocationVisibilities(List.of(
                        QuestLocationVisibilityOptionDTO.builder().value(QuestLocationVisibility.INHERIT).label("Use profile default").description("Follow your profile location setting").build(),
                        QuestLocationVisibilityOptionDTO.builder().value(QuestLocationVisibility.OFF).label("Hide location").description("Do not attach location to this quest").build(),
                        QuestLocationVisibilityOptionDTO.builder().value(QuestLocationVisibility.APPROXIMATE).label("Approximate area").description("Show only the wider quest area").build(),
                        QuestLocationVisibilityOptionDTO.builder().value(QuestLocationVisibility.EXACT).label("Exact address").description("Show the exact quest address when allowed").build()
                ))
                .build();
    }
}
