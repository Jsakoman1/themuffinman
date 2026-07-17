package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.concepts.ActorIdentity;
import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.identity.mapper.AppUserMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.identity.service.AppUserReadService;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.social.service.CircleReadService;
import com.themuffinman.app.workmarket.dto.DashboardResponseDTO;
import com.themuffinman.app.workmarket.dto.DashboardSectionsDTO;
import com.themuffinman.app.workmarket.dto.DashboardSummaryDTO;
import com.themuffinman.app.workmarket.dto.DashboardVoiceConfigDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.config.VoiceProperties;
import com.themuffinman.app.workmarket.mapper.WorkmarketQuestNewsMgr;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service("workmarketDashboardReadService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkmarketDashboardReadService {

    private final WorkmarketQuestReadService questReadService;
    private final WorkmarketQuestApplicationReadService questApplicationReadService;
    private final WorkmarketDashboardSummaryAssembler dashboardSummaryAssembler;
    private final WorkmarketQuestApplicationRepository questApplicationRepository;
    private final WorkmarketQuestNewsService questNewsService;
    private final CircleReadService circleReadService;
    private final AppUserRepository appUserRepository;
    private final AppUserReadService appUserReadService;
    private final WorkmarketQuestNewsMgr questNewsMgr;
    private final AppUserMgr appUserMgr;
    private final WorkmarketOptionsService optionsService;
    private final WorkmarketDashboardSectionsFactory dashboardSectionsFactory;
    private final VoiceProperties voiceProperties;

    public DashboardResponseDTO getMyDashboard(AppUser currentUser) {
        DashboardReadModel readModel = load(currentUser);
        if (currentUser == null) {
            return DashboardResponseDTO.builder()
                    .options(optionsService.getOptions(null))
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

        List<QuestResponseDTO> questDtos = questReadService.toResponses(readModel.sortedQuests(), currentUser);
        List<QuestResponseDTO> myQuestDtos = questDtos.stream()
                .filter(quest -> quest.getViewerRelation() == com.themuffinman.app.workmarket.dto.QuestViewerRelationDTO.OWNER)
                .toList();
        List<QuestResponseDTO> availableQuestDtos = questDtos.stream()
                .filter(quest -> quest.getStatus() == com.themuffinman.app.workmarket.model.QuestStatus.OPEN)
                .filter(quest -> quest.getViewerRelation() != com.themuffinman.app.workmarket.dto.QuestViewerRelationDTO.OWNER)
                .toList();
        DashboardSectionsDTO sections = dashboardSectionsFactory.buildSections(
                myQuestDtos,
                readModel.sortedApplications(),
                readModel.recentNews(),
                readModel.incomingCircleRequests()
        );

        return DashboardResponseDTO.builder()
                .options(optionsService.getOptions(currentUser))
                .summary(dashboardSummaryAssembler.buildSummary(
                        currentUser,
                        readModel.sortedQuests(),
                        readModel.applications(),
                        readModel.unreadNewsCount(),
                        readModel.totalUserCount(),
                        readModel.adminUserCount()
                ))
                .sections(sections)
                .quests(questDtos)
                .myQuests(myQuestDtos)
                .availableQuests(availableQuestDtos)
                .myApplications(readModel.sortedApplications())
                .recentNews(readModel.recentNews())
                .incomingCircleRequests(readModel.incomingCircleRequests())
                .circles(readModel.circles())
                .appUsers(readModel.appUsers())
                .build();
    }

    public DashboardSummaryDTO getMySummary(AppUser currentUser) {
        DashboardReadModel readModel = load(currentUser);
        if (currentUser == null) {
            return DashboardSummaryDTO.builder().build();
        }

        return dashboardSummaryAssembler.buildSummary(
                currentUser,
                readModel.sortedQuests(),
                readModel.applications(),
                readModel.unreadNewsCount(),
                readModel.totalUserCount(),
                readModel.adminUserCount()
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

    private DashboardReadModel load(AppUser currentUser) {
        if (currentUser == null) {
            return DashboardReadModel.empty();
        }

        List<Quest> sortedQuests = sortQuests(questReadService.getAllQuests(currentUser));
        List<QuestApplication> applications = questApplicationRepository.findForApplicantDashboard(currentUser.getId());
        List<com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO> sortedApplications = sortApplications(applications);
        List<com.themuffinman.app.workmarket.dto.QuestNewsItemResponseDTO> recentNews = questNewsService.getMyNews(currentUser).stream()
                .limit(6)
                .map(questNewsMgr::toDto)
                .toList();
        List<CircleRequestResponseDTO> incomingCircleRequests = circleReadService.getIncomingRequests(currentUser);
        List<CircleGroupResponseDTO> circles = circleReadService.getCircles(currentUser);
        long unreadNewsCount = questNewsService.getUnreadCount(currentUser);
        long totalUserCount = appUserRepository.count();
        long adminUserCount = appUserRepository.countByRole(AppUserRole.ADMIN);
        List<AppUserResponseDTO> appUsers = dashboardAppUsers(currentUser);

        return new DashboardReadModel(
                sortedQuests,
                applications,
                sortedApplications,
                recentNews,
                incomingCircleRequests,
                circles,
                unreadNewsCount,
                totalUserCount,
                adminUserCount,
                appUsers
        );
    }

    private List<Quest> sortQuests(List<Quest> quests) {
        return quests.stream()
                .sorted(Comparator
                        .comparing((Quest quest) -> QUEST_STATUS_SORT_ORDER.getOrDefault(quest.getStatus(), Integer.MAX_VALUE))
                        .thenComparing(Quest::getId, Comparator.reverseOrder()))
                .toList();
    }

    private List<com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO> sortApplications(List<QuestApplication> applications) {
        return applications.stream()
                .sorted(Comparator
                        .comparing((QuestApplication application) -> APPLICATION_STATUS_SORT_ORDER.getOrDefault(application.getStatus(), Integer.MAX_VALUE))
                        .thenComparing(QuestApplication::getId, Comparator.reverseOrder()))
                .map(questApplicationReadService::toApplicantResponse)
                .toList();
    }

    private List<AppUserResponseDTO> dashboardAppUsers(AppUser currentUser) {
        if (!ActorIdentity.from(currentUser).admin()) {
            return List.of();
        }

        return appUserReadService.getAllAppUsers(null).stream()
                .map(appUserMgr::toDto)
                .toList();
    }

    private static final Map<com.themuffinman.app.workmarket.model.QuestStatus, Integer> QUEST_STATUS_SORT_ORDER = Map.of(
            com.themuffinman.app.workmarket.model.QuestStatus.OPEN, 0,
            com.themuffinman.app.workmarket.model.QuestStatus.ASSIGNED, 1,
            com.themuffinman.app.workmarket.model.QuestStatus.WAITING_CONFIRMATION, 2,
            com.themuffinman.app.workmarket.model.QuestStatus.IN_PROGRESS, 3,
            com.themuffinman.app.workmarket.model.QuestStatus.COMPLETED, 4,
            com.themuffinman.app.workmarket.model.QuestStatus.CANCELLED, 5
    );

    private static final Map<com.themuffinman.app.workmarket.model.QuestApplicationStatus, Integer> APPLICATION_STATUS_SORT_ORDER = Map.of(
            com.themuffinman.app.workmarket.model.QuestApplicationStatus.APPROVED, 0,
            com.themuffinman.app.workmarket.model.QuestApplicationStatus.PENDING, 1,
            com.themuffinman.app.workmarket.model.QuestApplicationStatus.DECLINED, 2,
            com.themuffinman.app.workmarket.model.QuestApplicationStatus.WITHDRAWN, 3,
            com.themuffinman.app.workmarket.model.QuestApplicationStatus.RELEASED, 4
    );

    private record DashboardReadModel(
            List<Quest> sortedQuests,
            List<QuestApplication> applications,
            List<com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO> sortedApplications,
            List<com.themuffinman.app.workmarket.dto.QuestNewsItemResponseDTO> recentNews,
            List<CircleRequestResponseDTO> incomingCircleRequests,
            List<CircleGroupResponseDTO> circles,
            long unreadNewsCount,
            long totalUserCount,
            long adminUserCount,
            List<AppUserResponseDTO> appUsers
    ) {
        static DashboardReadModel empty() {
            return new DashboardReadModel(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), 0, 0, 0, List.of());
        }
    }
}
