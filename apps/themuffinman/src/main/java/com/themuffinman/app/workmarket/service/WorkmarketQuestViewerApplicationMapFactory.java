package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class WorkmarketQuestViewerApplicationMapFactory {

    private final WorkmarketQuestApplicationRepository questApplicationRepository;

    Map<Long, QuestApplication> getCurrentUserApplicationsByQuestId(AppUser currentUser) {
        if (currentUser == null) {
            return Map.of();
        }

        return questApplicationRepository.findForApplicantDashboard(currentUser.getId()).stream()
                .collect(Collectors.toMap(
                        application -> application.getQuest().getId(),
                        Function.identity(),
                        (left, right) -> left
                ));
    }
}
