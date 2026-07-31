package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestApplicationsViewDTO;
import com.themuffinman.app.workmarket.mapper.WorkmarketQuestMgr;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestStatus;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkmarketQuestApplicationsReadServiceTest {

    @Mock private WorkmarketQuestApplicationRepository applicationRepository;
    @Mock private WorkmarketQuestApplicationViewAssembler applicationViewAssembler;
    @Mock private WorkmarketQuestMgr questMgr;
    @Mock private WorkmarketQuestPresentationAssembler presentationAssembler;
    @Mock private WorkmarketQuestAccessPolicyService accessPolicyService;
    @Mock private WorkmarketQuestExecutionPrimitiveService workflowSupport;

    @Test
    void ownerViewChecksAuthorityBeforeReturningAHumanReviewQueue() {
        WorkmarketQuestApplicationReadService service = new WorkmarketQuestApplicationReadService(
                applicationRepository, applicationViewAssembler, questMgr, presentationAssembler, accessPolicyService, workflowSupport
        );
        Quest sideJob = new Quest();
        sideJob.setId(4L);
        sideJob.setStatus(QuestStatus.OPEN);
        AppUser owner = new AppUser();
        owner.setId(1L);
        when(workflowSupport.resolveTarget(4L)).thenReturn(sideJob);
        when(applicationRepository.findForQuestApplicationManagement(4L)).thenReturn(List.of());

        QuestApplicationsViewDTO view = service.getApplicationsViewForQuest(4L, owner, false);

        assertEquals(0, view.getPendingApplicationCount());
        assertEquals(List.of(), view.getVisibleApplications());
        verify(workflowSupport).validateOwnerAuthority(sideJob, owner);
    }
}
