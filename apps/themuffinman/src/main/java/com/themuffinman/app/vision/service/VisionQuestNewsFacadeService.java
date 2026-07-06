package com.themuffinman.app.vision.service;

import com.themuffinman.app.common.dto.ActionResultDTO;
import com.themuffinman.app.common.dto.ActionResults;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.dto.QuestNewsItemResponseDTO;
import com.themuffinman.app.workmarket.mapper.WorkmarketQuestNewsMgr;
import com.themuffinman.app.workmarket.service.WorkmarketQuestNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VisionQuestNewsFacadeService {

    private final WorkmarketQuestNewsService questNewsService;
    private final WorkmarketQuestNewsMgr questNewsMgr;

    public List<QuestNewsItemResponseDTO> getMyNews(AppUser currentUser) {
        return questNewsService.getMyNews(currentUser).stream().map(questNewsMgr::toDto).toList();
    }

    public long getUnreadCount(AppUser currentUser) {
        return questNewsService.getUnreadCount(currentUser);
    }

    public ActionResultDTO markMyNewsAsRead(AppUser currentUser) {
        questNewsService.markMyNewsAsRead(currentUser);
        return ActionResults.of("MARK_NEWS_AS_READ", "Updates marked as read.");
    }

    public ActionResultDTO markMyNewsItemAsRead(Long id, AppUser currentUser) {
        questNewsService.markMyNewsItemAsRead(id, currentUser);
        return ActionResults.of("MARK_NEWS_ITEM_AS_READ", "Update marked as read.");
    }
}
