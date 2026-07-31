package com.themuffinman.app.notification.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.notification.dto.AttentionCenterDTO;
import com.themuffinman.app.workmarket.mapper.WorkmarketQuestNewsMgr;
import com.themuffinman.app.workmarket.service.WorkmarketQuestNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class AttentionCenterService {
    private final WorkmarketQuestNewsService newsService;
    private final WorkmarketQuestNewsMgr newsMapper;

    public AttentionCenterDTO getMine(AppUser user) {
        var items = newsMapper.toDtos(newsService.getMyNews(user));
        return AttentionCenterDTO.builder().unreadCount(newsService.getUnreadCount(user)).items(items).build();
    }
}
