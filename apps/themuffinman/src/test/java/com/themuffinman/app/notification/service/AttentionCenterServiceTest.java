package com.themuffinman.app.notification.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.QuestNewsItemResponseDTO;
import com.themuffinman.app.workmarket.mapper.WorkmarketQuestNewsMgr;
import com.themuffinman.app.workmarket.model.QuestNewsItem;
import com.themuffinman.app.workmarket.service.WorkmarketQuestNewsService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AttentionCenterServiceTest {
    @Test
    void usesTheSameNewsStreamForItemsAndUnreadCount() {
        AppUser viewer = new AppUser();
        viewer.setId(7L);
        QuestNewsItem news = new QuestNewsItem();
        QuestNewsItemResponseDTO mappedNews = QuestNewsItemResponseDTO.builder().id(11L).title("New application").build();
        WorkmarketQuestNewsService newsService = mock(WorkmarketQuestNewsService.class);
        WorkmarketQuestNewsMgr newsMapper = mock(WorkmarketQuestNewsMgr.class);
        when(newsService.getMyNews(viewer)).thenReturn(List.of(news));
        when(newsService.getUnreadCount(viewer)).thenReturn(1L);
        when(newsMapper.toDtos(List.of(news))).thenReturn(List.of(mappedNews));

        var response = new AttentionCenterService(newsService, newsMapper).getMine(viewer);

        assertThat(response.getUnreadCount()).isEqualTo(1L);
        assertThat(response.getItems()).containsExactly(mappedNews);
    }
}
