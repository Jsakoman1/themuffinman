package com.sidequest.sidequest.controller;

import com.sidequest.sidequest.dto.UserReviewRequestDTO;
import com.sidequest.sidequest.dto.UserReviewResponseDTO;
import com.sidequest.sidequest.model.AppUser;
import com.sidequest.sidequest.service.UserReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
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
