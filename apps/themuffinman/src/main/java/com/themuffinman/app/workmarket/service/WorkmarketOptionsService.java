package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.workmarket.dto.AppUserRoleOptionDTO;
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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkmarketOptionsService {

    public WorkmarketOptionsDTO getOptions() {
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
                        QuestSortOptionDTO.builder().value("recommended").label("Recommended").build(),
                        QuestSortOptionDTO.builder().value("newest").label("Soonest").build(),
                        QuestSortOptionDTO.builder().value("highest").label("Highest award").build()
                ))
                .build();
    }
}
