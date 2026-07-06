package com.themuffinman.app.vision.controller;

import com.themuffinman.app.common.dto.ActionResultDTO;
import com.themuffinman.app.vision.dto.QuestNewsItemResponseDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.service.VisionQuestNewsFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class QuestNewsController {

    private final VisionQuestNewsFacadeService questNewsService;

    @GetMapping("/me")
    public List<QuestNewsItemResponseDTO> getMyNews(@AuthenticationPrincipal AppUser currentUser) {
        return questNewsService.getMyNews(currentUser);
    }

    @GetMapping("/me/unread-count")
    public long getUnreadCount(@AuthenticationPrincipal AppUser currentUser) {
        return questNewsService.getUnreadCount(currentUser);
    }

    @PatchMapping("/me/read")
    public ActionResultDTO markMyNewsAsRead(@AuthenticationPrincipal AppUser currentUser) {
        return questNewsService.markMyNewsAsRead(currentUser);
    }

    @PatchMapping("/me/{id}/read")
    public ActionResultDTO markMyNewsItemAsRead(@PathVariable Long id, @AuthenticationPrincipal AppUser currentUser) {
        return questNewsService.markMyNewsItemAsRead(id, currentUser);
    }
}
