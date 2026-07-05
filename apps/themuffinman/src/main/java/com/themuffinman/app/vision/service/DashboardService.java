package com.themuffinman.app.vision.service;

import com.themuffinman.app.common.concepts.ActorIdentity;
import com.themuffinman.app.config.VoiceProperties;
import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.identity.mapper.AppUserMgr;
import com.themuffinman.app.vision.mapper.QuestNewsMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.DashboardResponseDTO;
import com.themuffinman.app.vision.dto.DashboardSectionsDTO;
import com.themuffinman.app.vision.dto.DashboardSummaryDTO;
import com.themuffinman.app.vision.dto.DashboardVoiceConfigDTO;
import com.themuffinman.app.vision.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.vision.dto.QuestNewsItemResponseDTO;
import com.themuffinman.app.vision.dto.QuestResponseDTO;
import com.themuffinman.app.vision.model.Quest;
import com.themuffinman.app.vision.model.QuestApplication;
import com.themuffinman.app.vision.model.QuestApplicationStatus;
import com.themuffinman.app.vision.model.QuestStatus;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.identity.service.AppUserService;
import com.themuffinman.app.social.service.CircleService;
import com.themuffinman.app.social.service.CircleReadService;
import com.themuffinman.app.vision.repository.QuestApplicationRepository;
import com.themuffinman.app.vision.service.VisionOptionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final QuestService questService;
    private final QuestApplicationRepository questApplicationRepository;
    private final QuestNewsService questNewsService;
    private final AppUserRepository appUserRepository;
    private final CircleReadService circleReadService;
    private final AppUserService appUserService;
    private final QuestApplicationService questApplicationService;
    private final QuestNewsMgr questNewsMgr;
    private final AppUserMgr appUserMgr;
    private final VisionOptionsService visionOptionsService;
    private final DashboardSectionsFactory dashboardSectionsFactory;
    private final DashboardSummaryAssembler dashboardSummaryAssembler;
    private final VoiceProperties voiceProperties;

    private static final Map<QuestStatus, Integer> QUEST_STATUS_SORT_ORDER = Map.of(
            QuestStatus.OPEN, 0,
            QuestStatus.ASSIGNED, 1,
            QuestStatus.WAITING_CONFIRMATION, 2,
            QuestStatus.IN_PROGRESS, 3,
            QuestStatus.COMPLETED, 4,
            QuestStatus.CANCELLED, 5
    );

    private static final Map<QuestApplicationStatus, Integer> APPLICATION_STATUS_SORT_ORDER = Map.of(
            QuestApplicationStatus.APPROVED, 0,
            QuestApplicationStatus.PENDING, 1,
            QuestApplicationStatus.DECLINED, 2,
            QuestApplicationStatus.WITHDRAWN, 3
    );
    public DashboardResponseDTO getMyDashboard(AppUser currentUser) {
        if (currentUser == null) {
            return DashboardResponseDTO.builder()
                    .options(visionOptionsService.getOptions(null))
                    .summary(DashboardSummaryDTO.builder().build())
                    .sections(dashboardSectionsFactory.emptySections())
                    .quests(List.of())
                    .myQuests(List.of())
                    .availableQuests(List.of())
                    .myApplications(List.of())
                    .recentNews(List.of())
                    .incomingCircleRequests(List.of())
                    .circles(List.of())
                    .appUsers(List.of())
                    .build();
        }

        List<Quest> visibleQuests = questService.getAllQuests(currentUser);
        List<Quest> sortedQuests = sortQuests(visibleQuests);
        List<QuestApplication> applications = questApplicationRepository.findForApplicantDashboard(currentUser.getId());
        List<QuestApplicationResponseDTO> sortedApplications = sortApplications(applications);
        List<QuestNewsItemResponseDTO> recentNews = questNewsService.getMyNews(currentUser).stream()
                .limit(6)
                .map(questNewsMgr::toDto)
                .toList();
        List<CircleRequestResponseDTO> incomingCircleRequests = circleReadService.getIncomingRequests(currentUser);
        List<CircleGroupResponseDTO> circles = circleReadService.getCircles(currentUser);

        List<QuestResponseDTO> questDtos = questService.toResponses(sortedQuests, currentUser);
        List<QuestResponseDTO> myQuestDtos = questDtos.stream()
                .filter(quest -> quest.getViewerRelation() == com.themuffinman.app.vision.dto.QuestViewerRelationDTO.OWNER)
                .toList();
        List<QuestResponseDTO> availableQuestDtos = questDtos.stream()
                .filter(quest -> quest.getStatus() == QuestStatus.OPEN)
                .filter(quest -> quest.getViewerRelation() != com.themuffinman.app.vision.dto.QuestViewerRelationDTO.OWNER)
                .toList();
        DashboardSectionsDTO sections = dashboardSectionsFactory.buildSections(myQuestDtos, sortedApplications, recentNews, incomingCircleRequests);

        return DashboardResponseDTO.builder()
                .options(visionOptionsService.getOptions(currentUser))
                .summary(dashboardSummaryAssembler.buildSummary(
                        currentUser,
                        visibleQuests,
                        applications,
                        questNewsService.getUnreadCount(currentUser),
                        appUserRepository.count(),
                        appUserRepository.countByRole(com.themuffinman.app.identity.model.AppUserRole.ADMIN)
                ))
                .sections(sections)
                .quests(questDtos)
                .myQuests(myQuestDtos)
                .availableQuests(availableQuestDtos)
                .myApplications(sortedApplications)
                .recentNews(recentNews)
                .incomingCircleRequests(incomingCircleRequests)
                .circles(circles)
                .appUsers(getDashboardAppUsers(currentUser))
                .build();
    }

    public DashboardSummaryDTO getMySummary(AppUser currentUser) {
        if (currentUser == null) {
            return DashboardSummaryDTO.builder().build();
        }

        List<Quest> quests = questService.getAllQuests(currentUser);
        List<QuestApplication> applications = questApplicationRepository.findForApplicantDashboard(currentUser.getId());
        return dashboardSummaryAssembler.buildSummary(
                currentUser,
                quests,
                applications,
                questNewsService.getUnreadCount(currentUser),
                appUserRepository.count(),
                appUserRepository.countByRole(com.themuffinman.app.identity.model.AppUserRole.ADMIN)
        );
    }

    public DashboardVoiceConfigDTO getMyVoiceConfig(AppUser currentUser) {
        boolean authenticated = currentUser != null;
        boolean configured = authenticated && voiceProperties.isEnabled() && voiceProperties.isConfigured();

        return DashboardVoiceConfigDTO.builder()
                .enabled(configured)
                .speechToTextEnabled(configured && voiceProperties.isSpeechToTextEnabled())
                .textToSpeechEnabled(configured && voiceProperties.isTextToSpeechEnabled())
                .recognitionProvider(voiceProperties.getRecognitionProvider())
                .synthesisProvider(voiceProperties.getSynthesisProvider())
                .preferredLocale(voiceProperties.getPreferredLocale())
                .interimResults(voiceProperties.isInterimResults())
                .continuousRecognition(voiceProperties.isContinuousRecognition())
                .maxAlternatives(voiceProperties.getMaxAlternatives())
                .autoSpeakResponses(voiceProperties.isAutoSpeakResponses())
                .maxRecordingMillis(voiceProperties.getMaxRecordingMillis())
                .maxAudioBytes(voiceProperties.getMaxAudioBytes())
                .maxSpeechTextLength(voiceProperties.getMaxSpeechTextLength())
                .build();
    }

    private List<Quest> sortQuests(List<Quest> quests) {
        return quests.stream()
                .sorted(Comparator
                        .comparing((Quest quest) -> QUEST_STATUS_SORT_ORDER.getOrDefault(quest.getStatus(), Integer.MAX_VALUE))
                        .thenComparing(Quest::getId, Comparator.reverseOrder()))
                .toList();
    }

    private List<QuestApplicationResponseDTO> sortApplications(List<QuestApplication> applications) {
        return applications.stream()
                .sorted(Comparator
                        .comparing((QuestApplication application) -> APPLICATION_STATUS_SORT_ORDER.getOrDefault(application.getStatus(), Integer.MAX_VALUE))
                        .thenComparing(QuestApplication::getId, Comparator.reverseOrder()))
                .map(questApplicationService::toApplicantResponse)
                .toList();
    }

    private List<AppUserResponseDTO> getDashboardAppUsers(AppUser currentUser) {
        if (!ActorIdentity.from(currentUser).admin()) {
            return List.of();
        }

        return appUserService.getAllAppUsers(null).stream()
                .map(appUserMgr::toDto)
                .toList();
    }

}
