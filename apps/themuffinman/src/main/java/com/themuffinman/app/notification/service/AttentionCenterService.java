package com.themuffinman.app.notification.service;

import com.themuffinman.app.activity.dto.ActivityItemDTO;
import com.themuffinman.app.activity.service.ActivityReadService;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.notification.dto.AttentionCenterDTO;
import com.themuffinman.app.workmarket.service.WorkmarketQuestNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service @RequiredArgsConstructor
public class AttentionCenterService {
    private final WorkmarketQuestNewsService newsService;
    private final ActivityReadService activityReadService;
    public AttentionCenterDTO getMine(AppUser user) {
        // Attention reuses the deduplicated activity contract; it only limits the
        // visible subset for the compact notification surface.
        List<ActivityItemDTO> items = activityReadService.getMine(user);
        return AttentionCenterDTO.builder().unreadCount(newsService.getUnreadCount(user)).items(items.stream().limit(12).toList()).build();
    }
}
