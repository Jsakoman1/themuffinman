package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.config.RetentionProperties;
import com.themuffinman.app.chat.service.ChatRealtimeService;
import com.themuffinman.app.common.time.TimeSupport;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestNewsItem;
import com.themuffinman.app.workmarket.model.QuestNewsType;
import com.themuffinman.app.workmarket.repository.WorkmarketQuestNewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service("workmarketQuestNewsService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkmarketQuestNewsService {

    private static final int DEFAULT_LIMIT = 8;

    private final WorkmarketQuestNewsRepository questNewsRepository;
    private final RetentionProperties retentionProperties;
    private final ChatRealtimeService chatRealtimeService;

    public List<QuestNewsItem> getMyNews(AppUser currentUser) {
        return questNewsRepository.findByRecipientUserIdOrderByCreatedAtDesc(currentUser.getId(), PageRequest.of(0, DEFAULT_LIMIT));
    }

    public long getUnreadCount(AppUser currentUser) {
        return questNewsRepository.countByRecipientUserIdAndReadAtIsNull(currentUser.getId());
    }

    @Transactional
    public void markMyNewsAsRead(AppUser currentUser) {
        questNewsRepository.markAllAsRead(currentUser.getId(), TimeSupport.now());
        notifyUnreadCountChanged(currentUser.getId(), currentUser.getId(), "news_marked_read");
    }

    @Transactional
    public void markMyNewsItemAsRead(Long newsId, AppUser currentUser) {
        QuestNewsItem item = questNewsRepository.findByIdAndRecipientUserId(newsId, currentUser.getId())
                .orElse(null);

        if (item == null || item.getReadAt() != null) {
            return;
        }

        item.setReadAt(TimeSupport.now());
        questNewsRepository.save(item);
        notifyUnreadCountChanged(currentUser.getId(), currentUser.getId(), "news_item_marked_read");
    }

    @Transactional
    public void notifyApplicationCreated(Quest quest, QuestApplication application, AppUser actor) {
        storeItem(
                quest.getCreator(),
                actor,
                quest,
                null,
                application.getId(),
                null,
                QuestNewsType.APPLICATION_CREATED,
                "New application",
                application.getApplicant().getUsername() + " applied for \"" + quest.getTitle() + "\"."
        );
    }

    @Transactional
    public void notifyApplicationUpdated(Quest quest, QuestApplication application, AppUser actor) {
        storeItem(
                quest.getCreator(),
                actor,
                quest,
                null,
                application.getId(),
                null,
                QuestNewsType.APPLICATION_UPDATED,
                "Application updated",
                application.getApplicant().getUsername() + " updated their application for \"" + quest.getTitle() + "\"."
        );
    }

    @Transactional
    public void notifyApplicationWithdrawn(Quest quest, QuestApplication application, AppUser actor) {
        storeItem(
                quest.getCreator(),
                actor,
                quest,
                null,
                application.getId(),
                null,
                QuestNewsType.APPLICATION_WITHDRAWN,
                "Application withdrawn",
                application.getApplicant().getUsername() + " withdrew their application for \"" + quest.getTitle() + "\"."
        );
    }

    @Transactional
    public void notifyApplicationApproved(Quest quest, QuestApplication application, AppUser actor) {
        storeItem(
                application.getApplicant(),
                actor,
                quest,
                null,
                application.getId(),
                null,
                QuestNewsType.APPLICATION_APPROVED,
                "Application approved",
                "Your application for \"" + quest.getTitle() + "\" was approved."
        );
    }

    @Transactional
    public void notifyApplicationDeclined(Quest quest, QuestApplication application, AppUser actor) {
        storeItem(
                application.getApplicant(),
                actor,
                quest,
                null,
                application.getId(),
                null,
                QuestNewsType.APPLICATION_DECLINED,
                "Application declined",
                "Your application for \"" + quest.getTitle() + "\" was declined."
        );
    }

    @Transactional
    public void notifyQuestDeleted(Quest quest, AppUser actor, AppUser recipient) {
        storeItem(
                recipient,
                actor,
                quest,
                null,
                null,
                null,
                QuestNewsType.QUEST_DELETED,
                "Quest deleted",
                "The quest \"" + quest.getTitle() + "\" was deleted."
        );
    }

    @Transactional
    public void notifyCircleRequestReceived(AppUser recipient, AppUser actor, Long circleRequestId) {
        storeItem(
                recipient,
                actor,
                null,
                "Circles",
                null,
                circleRequestId,
                QuestNewsType.CIRCLE_REQUEST_RECEIVED,
                "New circle request",
                actor == null ? null : actor.getUsername() + " wants to connect with you."
        );
    }

    @Transactional
    public void notifyCircleRequestAccepted(AppUser recipient, AppUser actor) {
        storeItem(
                recipient,
                actor,
                null,
                "Circles",
                null,
                null,
                QuestNewsType.CIRCLE_REQUEST_ACCEPTED,
                "Circle request accepted",
                actor == null ? null : actor.getUsername() + " accepted your circle request."
        );
    }

    @Transactional
    public void notifyQuestEvent(
            AppUser recipient,
            AppUser actor,
            Quest quest,
            Long applicationId,
            QuestNewsType type,
            String title,
            String message
    ) {
        storeItem(recipient, actor, quest, null, applicationId, null, type, title, message);
    }

    @Transactional
    public void notifyRideEvent(AppUser recipient, AppUser actor, String title, String message) {
        storeItem(recipient, actor, null, "Rides", null, null, QuestNewsType.RIDE_EVENT, title, message);
    }

    @Transactional
    public int deleteExpiredNews() {
        int retentionDays = Math.max(retentionProperties.getNotifications().getDays(), 1);
        Instant cutoff = TimeSupport.daysAgo(retentionDays);
        return questNewsRepository.deleteByCreatedAtBefore(cutoff);
    }

    private void storeItem(
            AppUser recipient,
            AppUser actor,
            Quest quest,
            String questTitleOverride,
            Long applicationId,
            Long circleRequestId,
            QuestNewsType type,
            String title,
            String message
    ) {
        if (recipient == null || actor == null) {
            return;
        }

        QuestNewsItem item = new QuestNewsItem();
        item.setRecipientUserId(recipient.getId());
        item.setActorUserId(actor.getId());
        item.setActorUsername(actor.getUsername());
        item.setQuestId(quest == null ? null : quest.getId());
        item.setQuestTitle(questTitleOverride != null ? questTitleOverride : quest == null ? null : quest.getTitle());
        item.setApplicationId(applicationId);
        item.setCircleRequestId(circleRequestId);
        item.setType(type);
        item.setTitle(title);
        item.setMessage(message);
        questNewsRepository.save(item);
        notifyUnreadCountChanged(recipient.getId(), actor.getId(), "news_created");
    }

    private void notifyUnreadCountChanged(Long recipientUserId, Long actorUserId, String reason) {
        long unreadCount = questNewsRepository.countByRecipientUserIdAndReadAtIsNull(recipientUserId);
        chatRealtimeService.notifyNewsUpdated(recipientUserId, actorUserId, unreadCount, reason);
    }
}
