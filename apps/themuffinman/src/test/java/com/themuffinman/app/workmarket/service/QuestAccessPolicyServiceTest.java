package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.testing.TestFixtures;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import com.themuffinman.app.workmarket.model.QuestStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QuestAccessPolicyServiceTest {

    private final QuestAccessPolicyService policyService = new QuestAccessPolicyService();

    @Test
    void canManageQuestOnlyAllowsOwnerOrAdmin() {
        AppUser owner = TestFixtures.user(1L, "owner");
        AppUser other = TestFixtures.user(2L, "other");
        AppUser admin = TestFixtures.admin(3L, "admin");
        Quest quest = TestFixtures.quest(10L, owner, QuestStatus.OPEN);

        assertThat(policyService.canManageQuest(quest, owner)).isTrue();
        assertThat(policyService.canManageQuest(quest, admin)).isTrue();
        assertThat(policyService.canManageQuest(quest, other)).isFalse();
    }

    @Test
    void canViewQuestApplicationAllowsManagerOrApplicant() {
        AppUser owner = TestFixtures.user(1L, "owner");
        AppUser applicant = TestFixtures.user(2L, "applicant");
        AppUser other = TestFixtures.user(3L, "other");
        Quest quest = TestFixtures.quest(10L, owner, QuestStatus.OPEN);
        QuestApplication application = TestFixtures.questApplication(20L, quest, applicant, QuestApplicationStatus.PENDING);

        assertThat(policyService.canViewQuestApplication(application, quest, owner)).isTrue();
        assertThat(policyService.canViewQuestApplication(application, quest, applicant)).isTrue();
        assertThat(policyService.canViewQuestApplication(application, quest, other)).isFalse();
    }

    @Test
    void canApplyToQuestRejectsOwnerAdminAndExistingApplicant() {
        AppUser owner = TestFixtures.user(1L, "owner");
        AppUser applicant = TestFixtures.user(2L, "applicant");
        AppUser admin = TestFixtures.admin(3L, "admin");
        Quest quest = TestFixtures.quest(10L, owner, QuestStatus.OPEN);
        QuestApplication existingApplication = TestFixtures.questApplication(20L, quest, applicant, QuestApplicationStatus.PENDING);

        assertThat(policyService.canApplyToQuest(quest, applicant, null)).isTrue();
        assertThat(policyService.canApplyToQuest(quest, owner, null)).isFalse();
        assertThat(policyService.canApplyToQuest(quest, admin, null)).isFalse();
        assertThat(policyService.canApplyToQuest(quest, applicant, existingApplication)).isFalse();
    }
}
