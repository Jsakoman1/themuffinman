package com.themuffinman.app.vision.controller;

import com.themuffinman.app.vision.dto.UserReviewRequestDTO;
import com.themuffinman.app.vision.dto.UserReviewResponseDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.mapper.WorkmarketUserReviewMgr;
import com.themuffinman.app.workmarket.service.WorkmarketUserReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quests/{questId}/reviews")
@RequiredArgsConstructor
public class UserReviewController {
    private final WorkmarketUserReviewService userReviewService;
    private final WorkmarketUserReviewMgr userReviewMgr;

    @PostMapping
    public UserReviewResponseDTO createOrUpdateReview(
            @PathVariable Long questId,
            @RequestBody UserReviewRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return userReviewMgr.toDto(userReviewService.createOrUpdateReview(questId, dto, currentUser));
    }
}
