package com.themuffinman.app.vision.controller;

import com.themuffinman.app.common.dto.ActionResultDTO;
import com.themuffinman.app.common.dto.ActionResults;
import com.themuffinman.app.vision.dto.QuestNewsItemResponseDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.mapper.WorkmarketQuestNewsMgr;
import com.themuffinman.app.workmarket.service.WorkmarketQuestNewsService;
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

    private final WorkmarketQuestNewsService questNewsService;
    private final WorkmarketQuestNewsMgr questNewsMgr;

    @GetMapping("/me")
    public List<QuestNewsItemResponseDTO> getMyNews(@AuthenticationPrincipal AppUser currentUser) {
        return questNewsService.getMyNews(currentUser)
                .stream()
                .map(questNewsMgr::toDto)
                .toList();
    }

    @GetMapping("/me/unread-count")
    public long getUnreadCount(@AuthenticationPrincipal AppUser currentUser) {
        return questNewsService.getUnreadCount(currentUser);
    }

    @PatchMapping("/me/read")
    public ActionResultDTO markMyNewsAsRead(@AuthenticationPrincipal AppUser currentUser) {
        questNewsService.markMyNewsAsRead(currentUser);
        return ActionResults.of("MARK_NEWS_AS_READ", "Updates marked as read.");
    }

    @PatchMapping("/me/{id}/read")
    public ActionResultDTO markMyNewsItemAsRead(@PathVariable Long id, @AuthenticationPrincipal AppUser currentUser) {
        questNewsService.markMyNewsItemAsRead(id, currentUser);
        return ActionResults.of("MARK_NEWS_ITEM_AS_READ", "Update marked as read.");
    }
}
