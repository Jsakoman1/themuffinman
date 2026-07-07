package com.themuffinman.app.vision.service;

import com.themuffinman.app.config.VoiceProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.DashboardResponseDTO;
import com.themuffinman.app.vision.dto.DashboardSectionsDTO;
import com.themuffinman.app.vision.dto.DashboardSummaryDTO;
import com.themuffinman.app.vision.dto.DashboardVoiceConfigDTO;
import com.themuffinman.app.vision.dto.QuestResponseDTO;
import com.themuffinman.app.vision.mapper.QuestNewsMgr;
import com.themuffinman.app.vision.model.QuestStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final QuestService questService;
    private final QuestReadService questReadService;
    private final DashboardReadQueryService dashboardReadQueryService;
    private final VisionOptionsService visionOptionsService;
    private final DashboardSectionsFactory dashboardSectionsFactory;
    private final DashboardSummaryAssembler dashboardSummaryAssembler;
    private final VoiceProperties voiceProperties;

    DashboardService(
            QuestService questService,
            QuestReadService questReadService,
            com.themuffinman.app.vision.repository.QuestApplicationRepository questApplicationRepository,
            QuestNewsService questNewsService,
            com.themuffinman.app.identity.repository.AppUserRepository appUserRepository,
            com.themuffinman.app.social.service.CircleReadService circleReadService,
            com.themuffinman.app.identity.service.AppUserReadService appUserReadService,
            QuestApplicationService questApplicationService,
            QuestNewsMgr questNewsMgr,
            com.themuffinman.app.identity.mapper.AppUserMgr appUserMgr,
            VisionOptionsService visionOptionsService,
            DashboardSectionsFactory dashboardSectionsFactory,
            DashboardSummaryAssembler dashboardSummaryAssembler,
            VoiceProperties voiceProperties
    ) {
        this.questService = questService;
        this.questReadService = questReadService;
        this.dashboardReadQueryService = new DashboardReadQueryService(
                questService,
                questApplicationRepository,
                questApplicationService,
                questNewsService,
                circleReadService,
                appUserRepository,
                appUserReadService,
                questNewsMgr,
                appUserMgr
        );
        this.visionOptionsService = visionOptionsService;
        this.dashboardSectionsFactory = dashboardSectionsFactory;
        this.dashboardSummaryAssembler = dashboardSummaryAssembler;
        this.voiceProperties = voiceProperties;
    }

    public DashboardResponseDTO getMyDashboard(AppUser currentUser) {
        DashboardReadModel readModel = dashboardReadQueryService.load(currentUser);
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

        List<QuestResponseDTO> questDtos = questReadService.toResponses(readModel.sortedQuests(), currentUser);
        List<QuestResponseDTO> myQuestDtos = questDtos.stream()
                .filter(quest -> quest.getViewerRelation() == com.themuffinman.app.vision.dto.QuestViewerRelationDTO.OWNER)
                .toList();
        List<QuestResponseDTO> availableQuestDtos = questDtos.stream()
                .filter(quest -> quest.getStatus() == QuestStatus.OPEN)
                .filter(quest -> quest.getViewerRelation() != com.themuffinman.app.vision.dto.QuestViewerRelationDTO.OWNER)
                .toList();
        DashboardSectionsDTO sections = dashboardSectionsFactory.buildSections(
                myQuestDtos,
                readModel.sortedApplications(),
                readModel.recentNews(),
                readModel.incomingCircleRequests()
        );

        return DashboardResponseDTO.builder()
                .options(visionOptionsService.getOptions(currentUser))
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
        DashboardReadModel readModel = dashboardReadQueryService.load(currentUser);
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

}
