package com.themuffinman.app.activity.service;

import com.themuffinman.app.activity.dto.ActivityItemDTO;
import com.themuffinman.app.activity.model.ActivityResumeDismissal;
import com.themuffinman.app.activity.repository.ActivityResumeDismissalRepository;
import com.themuffinman.app.chat.model.ChatConversation;
import com.themuffinman.app.chat.repository.ChatConversationRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.things.model.ThingBorrowRequest;
import com.themuffinman.app.things.repository.ThingBorrowRequestRepository;
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
import java.util.HashSet;
import java.util.Set;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class ActivityReadService {
    private final WorkmarketQuestNewsService newsService;
    private final WorkmarketQuestNewsMgr newsMapper;
    private final VisionConversationRepository conversationRepository;
    private final ActivityResumeDismissalRepository dismissalRepository;
    private final ChatConversationRepository chatConversationRepository;
    private final ThingBorrowRequestRepository thingBorrowRequestRepository;

    public List<ActivityItemDTO> getMine(AppUser user) { return getWorkspaceActivity(user, 20); }

    /**
     * Viewer-scoped, read-only activity contract for workspace rails. Every item is prepared by
     * the originating backend domain and includes only a route the viewer may open.
     */
    public List<ActivityItemDTO> getWorkspaceActivity(AppUser user, int limit) {
        if (limit < 1 || limit > 50) throw new IllegalArgumentException("Activity limit must be between 1 and 50");
        List<ActivityItemDTO> items = new ArrayList<>();
        newsService.getMyNews(user).stream().limit(12).map(newsMapper::toDto).forEach(item -> items.add(ActivityItemDTO.builder().source("workmarket").kind("notification").title(item.getTitle()).summary(item.getMessage()).route(item.getQuestId() == null ? "/notifications" : "/work/quests/" + item.getQuestId()).primaryActionLabel("Open").occurredAt(item.getCreatedAt()).resumeKey("news:" + item.getId()).resumable(false).deliveryState(item.getDeliveryState()).readState(item.getReadState()).retryable(item.isRetryable()).build()));
        conversationRepository.findTop5ByOwnerOrderByUpdatedAtDesc(user).stream().filter(conversation -> conversation.getUpdatedAt() != null).forEach(conversation -> {
            String key = "vision:" + conversation.getId();
            if (!dismissalRepository.existsByUserIdAndResumeKey(user.getId(), key)) items.add(ActivityItemDTO.builder().source("vision").kind("vision").title("Continue Vision").summary("Resume your last guided task.").route("/vision?conversationId=" + conversation.getId()).primaryActionLabel("Continue").occurredAt(conversation.getUpdatedAt()).resumeKey(key).resumable(true).build());
        });
        chatConversationRepository.findDetailedByParticipantId(user.getId()).stream()
                .filter(conversation -> conversation.getLastMessageAt() != null)
                .filter(conversation -> !dismissalRepository.existsByUserIdAndResumeKey(user.getId(), "chat:" + conversation.getId()))
                .limit(8)
                .forEach(conversation -> items.add(toChatActivity(conversation, user)));
        thingBorrowRequestRepository.findForOwnerDashboard(user.getId()).stream()
                .limit(6)
                .filter(request -> !dismissalRepository.existsByUserIdAndResumeKey(user.getId(), thingResumeKey(request, "owner")))
                .forEach(request -> items.add(toThingActivity(request, true)));
        thingBorrowRequestRepository.findForBorrowerDashboard(user.getId()).stream()
                .limit(6)
                .filter(request -> !dismissalRepository.existsByUserIdAndResumeKey(user.getId(), thingResumeKey(request, "borrower")))
                .forEach(request -> items.add(toThingActivity(request, false)));
        Set<String> seen = new HashSet<>();
        return items.stream().filter(item -> seen.add(activityKey(item))).sorted(Comparator.comparing(ActivityItemDTO::getOccurredAt, Comparator.nullsLast(Comparator.reverseOrder()))).limit(limit).toList();
    }

    private String activityKey(ActivityItemDTO item) {
        return item.getResumeKey() != null ? item.getResumeKey() : item.getSource() + ":" + item.getKind() + ":" + item.getOccurredAt() + ":" + item.getTitle();
    }

    private ActivityItemDTO toChatActivity(ChatConversation conversation, AppUser user) {
        String title = conversation.getTitle();
        if (title == null || title.isBlank()) {
            title = "Chat conversation";
        }
        String key = "chat:" + conversation.getId();
        return ActivityItemDTO.builder()
                .source("chat")
                .kind("message")
                .title(title)
                .summary(conversation.getLastMessagePreview() == null ? "Open the latest conversation." : conversation.getLastMessagePreview())
                .route("/chat/" + conversation.getId())
                .primaryActionLabel("Open")
                .occurredAt(conversation.getLastMessageAt())
                .resumeKey(key)
                .resumable(true)
                .build();
    }

    private ActivityItemDTO toThingActivity(ThingBorrowRequest request, boolean owner) {
        String role = owner ? "owner" : "borrower";
        String title = owner ? "Borrow request for " + request.getListing().getTitle() : "Borrow request update";
        String summary = owner
                ? request.getBorrower().getUsername() + " · " + request.getStatus().name()
                : request.getListing().getTitle() + " · " + request.getStatus().name();
        return ActivityItemDTO.builder()
                .source("things")
                .kind("thing_request")
                .title(title)
                .summary(summary)
                .route(owner ? "/things/mine" : "/things")
                .primaryActionLabel("Open")
                .occurredAt(request.getCreatedAt())
                .resumeKey(thingResumeKey(request, role))
                .resumable(true)
                .build();
    }

    private String thingResumeKey(ThingBorrowRequest request, String role) {
        return "things:request:" + request.getId() + ":" + role;
    }

    public List<ActivityItemDTO> getRecent(AppUser user) { return getWorkspaceActivity(user, 8); }

    @Transactional public void dismiss(String resumeKey, AppUser user) { if (resumeKey == null || resumeKey.isBlank() || resumeKey.length() > 160) throw ServiceErrors.badRequest("Invalid resume key"); if (!dismissalRepository.existsByUserIdAndResumeKey(user.getId(), resumeKey)) { ActivityResumeDismissal dismissal = new ActivityResumeDismissal(); dismissal.setUser(user); dismissal.setResumeKey(resumeKey); dismissalRepository.save(dismissal); } }
}
