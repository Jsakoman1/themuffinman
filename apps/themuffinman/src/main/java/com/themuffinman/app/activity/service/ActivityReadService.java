package com.themuffinman.app.activity.service;

import com.themuffinman.app.activity.dto.ActivityItemDTO;
import com.themuffinman.app.activity.model.ActivityResumeDismissal;
import com.themuffinman.app.activity.repository.ActivityResumeDismissalRepository;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.repository.VisionConversationRepository;
import com.themuffinman.app.workmarket.mapper.WorkmarketQuestNewsMgr;
import com.themuffinman.app.workmarket.service.WorkmarketQuestNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class ActivityReadService {
    private final WorkmarketQuestNewsService newsService;
    private final WorkmarketQuestNewsMgr newsMapper;
    private final VisionConversationRepository conversationRepository;
    private final ActivityResumeDismissalRepository dismissalRepository;

    public List<ActivityItemDTO> getMine(AppUser user) {
        List<ActivityItemDTO> items = new ArrayList<>();
        newsService.getMyNews(user).stream().limit(12).map(newsMapper::toDto).forEach(item -> items.add(ActivityItemDTO.builder().kind("notification").title(item.getTitle()).summary(item.getMessage()).route(item.getQuestId() == null ? "/notifications" : "/work/quests/" + item.getQuestId()).occurredAt(item.getCreatedAt()).resumeKey("news:" + item.getId()).resumable(false).build()));
        conversationRepository.findTop5ByOwnerOrderByUpdatedAtDesc(user).stream().filter(conversation -> conversation.getUpdatedAt() != null).forEach(conversation -> {
            String key = "vision:" + conversation.getId();
            if (!dismissalRepository.existsByUserIdAndResumeKey(user.getId(), key)) items.add(ActivityItemDTO.builder().kind("vision").title("Continue Vision").summary("Resume your last guided task.").route("/vision?conversationId=" + conversation.getId()).occurredAt(conversation.getUpdatedAt()).resumeKey(key).resumable(true).build());
        });
        return items.stream().sorted(Comparator.comparing(ActivityItemDTO::getOccurredAt, Comparator.nullsLast(Comparator.reverseOrder()))).limit(20).toList();
    }

    @Transactional public void dismiss(String resumeKey, AppUser user) { if (resumeKey == null || resumeKey.isBlank() || resumeKey.length() > 160) throw new IllegalArgumentException("Invalid resume key"); if (!dismissalRepository.existsByUserIdAndResumeKey(user.getId(), resumeKey)) { ActivityResumeDismissal dismissal = new ActivityResumeDismissal(); dismissal.setUser(user); dismissal.setResumeKey(resumeKey); dismissalRepository.save(dismissal); } }
}
