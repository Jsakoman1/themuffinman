package com.themuffinman.app.workmarket.controller;

import com.themuffinman.app.workmarket.dto.UserReviewRequestDTO;
import com.themuffinman.app.workmarket.dto.UserReviewResponseDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.service.UserReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quests/{questId}/reviews")
@RequiredArgsConstructor
public class UserReviewController {
    private final UserReviewService userReviewService;

    @PostMapping
    public UserReviewResponseDTO createOrUpdateReview(
            @PathVariable Long questId,
            @RequestBody UserReviewRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return userReviewService.createOrUpdateReview(questId, dto, currentUser);
    }
}
