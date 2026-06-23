package com.sidequest.sidequest.controller;

import com.sidequest.sidequest.dto.QuestNewsItemResponseDTO;
import com.sidequest.sidequest.mapper.QuestNewsMgr;
import com.sidequest.sidequest.model.AppUser;
import com.sidequest.sidequest.service.QuestNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class QuestNewsController {

    private final QuestNewsService questNewsService;
    private final QuestNewsMgr questNewsMgr;

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
    public void markMyNewsAsRead(@AuthenticationPrincipal AppUser currentUser) {
        questNewsService.markMyNewsAsRead(currentUser);
    }

    @PatchMapping("/me/{id}/read")
    public void markMyNewsItemAsRead(@PathVariable Long id, @AuthenticationPrincipal AppUser currentUser) {
        questNewsService.markMyNewsItemAsRead(id, currentUser);
    }
}
