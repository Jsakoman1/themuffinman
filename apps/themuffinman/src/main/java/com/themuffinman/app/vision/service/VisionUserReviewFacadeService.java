package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.UserReviewRequestDTO;
import com.themuffinman.app.vision.dto.UserReviewResponseDTO;
import com.themuffinman.app.workmarket.mapper.WorkmarketUserReviewMgr;
import com.themuffinman.app.workmarket.service.WorkmarketUserReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VisionUserReviewFacadeService {

    private final WorkmarketUserReviewService userReviewService;
    private final WorkmarketUserReviewMgr userReviewMgr;

    public UserReviewResponseDTO createOrUpdateReview(Long questId, UserReviewRequestDTO dto, AppUser currentUser) {
        return userReviewMgr.toDto(userReviewService.createOrUpdateReview(questId, dto, currentUser));
    }
}
