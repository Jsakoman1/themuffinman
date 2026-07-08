package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.social.dto.CircleGroupRequestDTO;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.social.dto.CircleRequestCreateDTO;
import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.social.service.CircleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class VisionSocialMutationAdapter {

    private final CircleService circleService;

    CircleGroupResponseDTO createCircle(String circleName, AppUser currentUser) {
        return circleService.createCircle(CircleGroupRequestDTO.builder().name(circleName).build(), currentUser);
    }

    CircleGroupResponseDTO updateCircle(AppUser currentUser, Long circleId, String circleName) {
        return circleService.updateCircle(circleId, CircleGroupRequestDTO.builder().name(circleName).build(), currentUser);
    }

    void deleteCircle(AppUser currentUser, Long circleId) {
        circleService.deleteCircle(circleId, currentUser);
    }

    CircleRequestResponseDTO createCircleRequest(AppUser currentUser, Long targetUserId) {
        return circleService.createCircleRequest(CircleRequestCreateDTO.builder().recipientId(targetUserId).build(), currentUser);
    }

    CircleRequestResponseDTO acceptCircleRequest(AppUser currentUser, Long requestId) {
        return circleService.acceptCircleRequest(requestId, currentUser);
    }

    void deleteCircleRequest(AppUser currentUser, Long requestId) {
        circleService.deleteCircleRequest(requestId, currentUser);
    }
}
