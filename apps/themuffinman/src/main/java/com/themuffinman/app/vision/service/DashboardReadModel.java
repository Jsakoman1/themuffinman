package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.dto.AppUserResponseDTO;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.vision.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.vision.dto.QuestNewsItemResponseDTO;
import com.themuffinman.app.vision.model.Quest;
import com.themuffinman.app.vision.model.QuestApplication;

import java.util.List;

record DashboardReadModel(
        List<Quest> sortedQuests,
        List<QuestApplication> applications,
        List<QuestApplicationResponseDTO> sortedApplications,
        List<QuestNewsItemResponseDTO> recentNews,
        List<CircleRequestResponseDTO> incomingCircleRequests,
        List<CircleGroupResponseDTO> circles,
        long unreadNewsCount,
        long totalUserCount,
        long adminUserCount,
        List<AppUserResponseDTO> appUsers
) {

    static DashboardReadModel empty() {
        return new DashboardReadModel(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                0L,
                0L,
                0L,
                List.of()
        );
    }
}
