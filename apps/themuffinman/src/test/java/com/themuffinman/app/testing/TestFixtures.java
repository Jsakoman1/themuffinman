package com.themuffinman.app.testing;

import com.themuffinman.app.chat.model.ChatConversation;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.location.model.ExactLocationVisibilityScope;
import com.themuffinman.app.location.model.UserLocationMode;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;

import java.math.BigDecimal;
import java.time.Instant;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static AppUser user(Long id, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("encoded-password");
        user.setRole(AppUserRole.USER);
        return user;
    }

    public static AppUser admin(Long id, String username) {
        AppUser user = user(id, username);
        user.setRole(AppUserRole.ADMIN);
        return user;
    }

    public static AppUser userWithProfileLocation(Long id, String username) {
        AppUser user = user(id, username);
        user.setLocationMode(UserLocationMode.EXACT);
        user.setLocationCountry("Croatia");
        user.setLocationLocality("Zagreb");
        user.setLocationPostalCode("10000");
        user.setLocationStreet("Ilica");
        user.setLocationHouseNumber("1");
        user.setLocationLatitude(BigDecimal.valueOf(45.8150));
        user.setLocationLongitude(BigDecimal.valueOf(15.9819));
        user.setExactLocationVisibilityScope(ExactLocationVisibilityScope.CIRCLES);
        return user;
    }

    public static CircleGroup circle(Long id, AppUser owner, String name) {
        CircleGroup circle = new CircleGroup();
        circle.setId(id);
        circle.setOwner(owner);
        circle.setName(name);
        return circle;
    }

    public static Quest quest(Long id, AppUser creator, QuestStatus status) {
        Quest quest = new Quest();
        quest.setId(id);
        quest.setCreator(creator);
        quest.setStatus(status);
        quest.setTitle("Quest title");
        quest.setDescription("Quest description");
        quest.setAwardAmount(BigDecimal.valueOf(25));
        quest.setAssigneeTarget(1);
        return quest;
    }

    public static QuestApplication questApplication(
            Long id,
            Quest quest,
            AppUser applicant,
            QuestApplicationStatus status
    ) {
        QuestApplication application = new QuestApplication();
        application.setId(id);
        application.setQuest(quest);
        application.setApplicant(applicant);
        application.setStatus(status);
        application.setMessage("Message");
        application.setProposedPrice(BigDecimal.valueOf(25));
        application.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z").plusSeconds(id == null ? 0 : id));
        return application;
    }

    public static ChatConversation conversation(Long id, AppUser leftParticipant, AppUser rightParticipant) {
        ChatConversation conversation = new ChatConversation();
        conversation.setId(id);
        conversation.setLeftParticipant(leftParticipant);
        conversation.setRightParticipant(rightParticipant);
        conversation.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        conversation.setLastMessageAt(Instant.parse("2026-01-01T00:01:00Z"));
        conversation.setLastMessagePreview("Hello");
        conversation.setLastMessageSender(leftParticipant);
        return conversation;
    }
}
