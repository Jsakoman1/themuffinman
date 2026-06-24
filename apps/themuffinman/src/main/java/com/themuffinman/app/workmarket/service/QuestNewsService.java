package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestNewsItem;
import com.themuffinman.app.workmarket.model.QuestNewsType;
import com.themuffinman.app.workmarket.repository.QuestNewsRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class QuestNewsService {

    private static final int DEFAULT_LIMIT = 8;

    private final QuestNewsRepository questNewsRepository;

    public List<QuestNewsItem> getMyNews(AppUser currentUser) {
        return questNewsRepository.findByRecipientUserIdOrderByCreatedAtDesc(currentUser.getId(), PageRequest.of(0, DEFAULT_LIMIT));
    }

    public long getUnreadCount(AppUser currentUser) {
        return questNewsRepository.countByRecipientUserIdAndReadAtIsNull(currentUser.getId());
    }

    @Transactional
    public void markMyNewsAsRead(AppUser currentUser) {
        questNewsRepository.markAllAsRead(currentUser.getId(), Instant.now());
    }

    @Transactional
    public void markMyNewsItemAsRead(Long newsId, AppUser currentUser) {
        QuestNewsItem item = questNewsRepository.findByIdAndRecipientUserId(newsId, currentUser.getId())
                .orElse(null);

        if (item == null || item.getReadAt() != null) {
            return;
        }

        item.setReadAt(Instant.now());
        questNewsRepository.save(item);
    }

    public void notifyApplicationCreated(Quest quest, QuestApplication application, AppUser actor) {
        storeItem(
                quest.getCreator(),
                actor,
                quest,
                null,
                application.getId(),
                QuestNewsType.APPLICATION_CREATED,
                "New application",
                application.getApplicant().getUsername() + " applied for \"" + quest.getTitle() + "\"."
        );
    }

    public void notifyApplicationUpdated(Quest quest, QuestApplication application, AppUser actor) {
        storeItem(
                quest.getCreator(),
                actor,
                quest,
                null,
                application.getId(),
                QuestNewsType.APPLICATION_UPDATED,
                "Application updated",
                application.getApplicant().getUsername() + " updated their application for \"" + quest.getTitle() + "\"."
        );
    }

    public void notifyApplicationWithdrawn(Quest quest, QuestApplication application, AppUser actor) {
        storeItem(
                quest.getCreator(),
                actor,
                quest,
                null,
                application.getId(),
                QuestNewsType.APPLICATION_WITHDRAWN,
                "Application withdrawn",
                application.getApplicant().getUsername() + " withdrew their application for \"" + quest.getTitle() + "\"."
        );
    }

    public void notifyApplicationApproved(Quest quest, QuestApplication application, AppUser actor) {
        storeItem(
                application.getApplicant(),
                actor,
                quest,
                null,
                application.getId(),
                QuestNewsType.APPLICATION_APPROVED,
                "Application approved",
                "Your application for \"" + quest.getTitle() + "\" was approved."
        );
    }

    public void notifyApplicationDeclined(Quest quest, QuestApplication application, AppUser actor) {
        storeItem(
                application.getApplicant(),
                actor,
                quest,
                null,
                application.getId(),
                QuestNewsType.APPLICATION_DECLINED,
                "Application declined",
                "Your application for \"" + quest.getTitle() + "\" was declined."
        );
    }

    public void notifyQuestDeleted(Quest quest, AppUser actor, AppUser recipient) {
        storeItem(
                recipient,
                actor,
                quest,
                null,
                null,
                QuestNewsType.QUEST_DELETED,
                "Quest deleted",
                "The quest \"" + quest.getTitle() + "\" was deleted."
        );
    }

    public void notifyCircleRequestAccepted(AppUser recipient, AppUser actor) {
        storeItem(
                recipient,
                actor,
                null,
                "Circles",
                null,
                QuestNewsType.CIRCLE_REQUEST_ACCEPTED,
                "Circle request accepted",
                actor == null ? null : actor.getUsername() + " accepted your circle request."
        );
    }

    public void notifyQuestEvent(
            AppUser recipient,
            AppUser actor,
            Quest quest,
            Long applicationId,
            QuestNewsType type,
            String title,
            String message
    ) {
        storeItem(recipient, actor, quest, null, applicationId, type, title, message);
    }

    private void storeItem(
            AppUser recipient,
            AppUser actor,
            Quest quest,
            String questTitleOverride,
            Long applicationId,
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
        item.setType(type);
        item.setTitle(title);
        item.setMessage(message);
        questNewsRepository.save(item);
    }
}
