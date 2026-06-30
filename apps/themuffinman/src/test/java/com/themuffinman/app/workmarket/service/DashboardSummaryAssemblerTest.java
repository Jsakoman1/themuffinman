package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.workmarket.dto.DashboardSummaryDTO;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DashboardSummaryAssemblerTest {

    private final DashboardSummaryAssembler assembler = new DashboardSummaryAssembler();

    @Test
    void buildSummaryAggregatesCountsForAuthenticatedUser() {
        AppUser currentUser = new AppUser();
        currentUser.setId(5L);
        currentUser.setRole(AppUserRole.USER);

        AppUser otherUser = new AppUser();
        otherUser.setId(6L);

        Quest openQuest = new Quest();
        openQuest.setId(1L);
        openQuest.setCreator(currentUser);
        openQuest.setStatus(QuestStatus.OPEN);

        Quest assignedQuest = new Quest();
        assignedQuest.setId(2L);
        assignedQuest.setCreator(currentUser);
        assignedQuest.setStatus(QuestStatus.ASSIGNED);

        Quest waitingQuest = new Quest();
        waitingQuest.setId(3L);
        waitingQuest.setCreator(currentUser);
        waitingQuest.setStatus(QuestStatus.WAITING_CONFIRMATION);

        Quest inProgressQuest = new Quest();
        inProgressQuest.setId(4L);
        inProgressQuest.setCreator(otherUser);
        inProgressQuest.setStatus(QuestStatus.IN_PROGRESS);

        QuestApplication pendingApplication = new QuestApplication();
        pendingApplication.setId(11L);
        pendingApplication.setStatus(QuestApplicationStatus.PENDING);

        QuestApplication approvedOpenApplication = new QuestApplication();
        approvedOpenApplication.setId(12L);
        approvedOpenApplication.setStatus(QuestApplicationStatus.APPROVED);
        approvedOpenApplication.setQuest(openQuest);

        QuestApplication approvedAssignedApplication = new QuestApplication();
        approvedAssignedApplication.setId(13L);
        approvedAssignedApplication.setStatus(QuestApplicationStatus.APPROVED);
        approvedAssignedApplication.setQuest(assignedQuest);

        QuestApplication approvedWaitingApplication = new QuestApplication();
        approvedWaitingApplication.setId(14L);
        approvedWaitingApplication.setStatus(QuestApplicationStatus.APPROVED);
        approvedWaitingApplication.setQuest(waitingQuest);

        QuestApplication approvedInProgressApplication = new QuestApplication();
        approvedInProgressApplication.setId(16L);
        approvedInProgressApplication.setStatus(QuestApplicationStatus.APPROVED);
        approvedInProgressApplication.setQuest(inProgressQuest);

        QuestApplication declinedApplication = new QuestApplication();
        declinedApplication.setId(15L);
        declinedApplication.setStatus(QuestApplicationStatus.DECLINED);

        DashboardSummaryDTO summary = assembler.buildSummary(
                currentUser,
                List.of(openQuest, assignedQuest, waitingQuest, inProgressQuest),
                List.of(
                        pendingApplication,
                        approvedOpenApplication,
                        approvedAssignedApplication,
                        approvedWaitingApplication,
                        approvedInProgressApplication,
                        declinedApplication
                ),
                7L,
                12L,
                2L
        );

        assertFalse(summary.isAdminModeEnabled());
        assertEquals(4L, summary.getQuestCount());
        assertEquals(2L, summary.getVisibleMyQuestsCount());
        assertEquals(1L, summary.getPendingWorkApplicationsCount());
        assertEquals(3L, summary.getActiveWorkApplicationsCount());
        assertEquals(1L, summary.getActiveMyQuestsCount());
        assertEquals(4L, summary.getActiveWorkCount());
        assertEquals(0L, summary.getCompletedMyQuestsCount());
        assertEquals(1L, summary.getOpenQuestCount());
        assertEquals(1L, summary.getAssignedQuestCount());
        assertEquals(1L, summary.getWaitingConfirmationQuestCount());
        assertEquals(7L, summary.getUnreadNewsCount());
        assertEquals(12L, summary.getTotalUserCount());
        assertEquals(2L, summary.getAdminUserCount());
    }

    @Test
    void buildSummaryReturnsEmptySummaryForAnonymousUser() {
        DashboardSummaryDTO summary = assembler.buildSummary(null, List.of(), List.of(), 7L, 12L, 2L);

        assertEquals(0L, summary.getQuestCount());
        assertEquals(0L, summary.getUnreadNewsCount());
        assertEquals(0L, summary.getTotalUserCount());
    }
}
