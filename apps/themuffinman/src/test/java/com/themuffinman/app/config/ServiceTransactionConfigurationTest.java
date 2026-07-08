package com.themuffinman.app.config;

import com.themuffinman.app.identity.service.AdminUserDetailService;
import com.themuffinman.app.identity.service.AppUserReadService;
import com.themuffinman.app.identity.service.UserProfileViewService;
import com.themuffinman.app.location.service.AdminDatabaseMetricsService;
import com.themuffinman.app.location.service.LocationLookupService;
import com.themuffinman.app.social.service.CircleDiscoveryService;
import com.themuffinman.app.social.service.CircleMembershipService;
import com.themuffinman.app.social.service.CircleReadService;
import com.themuffinman.app.social.service.CircleRelationService;
import com.themuffinman.app.vision.service.VisionConversationLifecycleService;
import com.themuffinman.app.workmarket.service.WorkmarketOptionsService;
import com.themuffinman.app.workmarket.service.WorkmarketQuestExecutionPrimitiveService;
import com.themuffinman.app.workmarket.service.WorkmarketQuestApplicationReadService;
import com.themuffinman.app.workmarket.service.WorkmarketQuestNewsService;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceTransactionConfigurationTest {

    @Test
    void selectedReadServicesUseReadOnlyTransactionalClassCoverage() {
        assertReadOnlyTransactional(AdminUserDetailService.class);
        assertReadOnlyTransactional(UserProfileViewService.class);
        assertReadOnlyTransactional(WorkmarketOptionsService.class);
        assertReadOnlyTransactional(AdminDatabaseMetricsService.class);
        assertReadOnlyTransactional(WorkmarketQuestApplicationReadService.class);
        assertReadOnlyTransactional(CircleDiscoveryService.class);
        assertReadOnlyTransactional(WorkmarketQuestNewsService.class);
    }

    @Test
    void locationDebugStatusUsesReadOnlyTransactionalMethodCoverage() throws Exception {
        Method method = LocationLookupService.class.getMethod("getDebugStatus");
        Transactional annotation = method.getAnnotation(Transactional.class);
        assertNotNull(annotation);
        assertTrue(annotation.readOnly());
    }

    @Test
    void selectedReadMethodsUseReadOnlyTransactionalMethodCoverage() throws Exception {
        assertMethodReadOnlyTransactional(AppUserReadService.class, "getAllAppUsers", String.class);
        assertMethodReadOnlyTransactional(AppUserReadService.class, "getAppUser", Long.class);
        assertMethodReadOnlyTransactional(AppUserReadService.class, "countQuestsByCreatorId", Long.class);
        assertMethodReadOnlyTransactional(AppUserReadService.class, "getOpenQuestsByCreatorId", Long.class);
        assertMethodReadOnlyTransactional(CircleMembershipService.class, "isCircleMember", Long.class, Long.class);
        assertMethodReadOnlyTransactional(CircleMembershipService.class, "getOwnedCirclesByIds", com.themuffinman.app.identity.model.AppUser.class, java.util.List.class);
        assertMethodReadOnlyTransactional(CircleMembershipService.class, "getMembershipsByOwner", Long.class);
        assertMethodReadOnlyTransactional(CircleMembershipService.class, "getMembershipsForContact", Long.class, Long.class);
        assertMethodReadOnlyTransactional(CircleMembershipService.class, "getMembershipsByUserIdForOwner", Long.class);
        assertMethodReadOnlyTransactional(CircleRelationService.class, "isCircleBetween", com.themuffinman.app.identity.model.AppUser.class, com.themuffinman.app.identity.model.AppUser.class);
        assertMethodReadOnlyTransactional(CircleRelationService.class, "findRelation", com.themuffinman.app.identity.model.AppUser.class, com.themuffinman.app.identity.model.AppUser.class);
        assertMethodReadOnlyTransactional(CircleReadService.class, "getOverview", com.themuffinman.app.identity.model.AppUser.class);
        assertMethodReadOnlyTransactional(CircleReadService.class, "getCircles", com.themuffinman.app.identity.model.AppUser.class);
        assertMethodReadOnlyTransactional(CircleReadService.class, "getConnections", com.themuffinman.app.identity.model.AppUser.class);
        assertMethodReadOnlyTransactional(CircleReadService.class, "getIncomingRequests", com.themuffinman.app.identity.model.AppUser.class);
        assertMethodReadOnlyTransactional(CircleReadService.class, "getOutgoingRequests", com.themuffinman.app.identity.model.AppUser.class);
        assertMethodReadOnlyTransactional(CircleReadService.class, "getRelationWithUser", com.themuffinman.app.identity.model.AppUser.class, Long.class);
        assertMethodReadOnlyTransactional(VisionConversationLifecycleService.class, "loadConversation", Long.class, com.themuffinman.app.identity.model.AppUser.class);
        assertMethodReadOnlyTransactional(VisionConversationLifecycleService.class, "listRecentConversations", com.themuffinman.app.identity.model.AppUser.class);
        assertMethodReadOnlyTransactional(WorkmarketQuestExecutionPrimitiveService.class, "resolveTarget", Long.class);
        assertMethodReadOnlyTransactional(WorkmarketQuestExecutionPrimitiveService.class, "resolveTargetForTermDecision", Long.class, com.themuffinman.app.identity.model.AppUser.class);
        assertMethodReadOnlyTransactional(WorkmarketQuestExecutionPrimitiveService.class, "resolveCreator", com.themuffinman.app.workmarket.dto.QuestRequestDTO.class, com.themuffinman.app.identity.model.AppUser.class);
    }

    private void assertReadOnlyTransactional(Class<?> type) {
        Transactional annotation = type.getAnnotation(Transactional.class);
        assertNotNull(annotation, () -> type.getSimpleName() + " should declare @Transactional");
        assertTrue(annotation.readOnly(), () -> type.getSimpleName() + " should be readOnly");
    }

    private void assertMethodReadOnlyTransactional(Class<?> type, String methodName, Class<?>... parameterTypes) throws Exception {
        Method method = type.getMethod(methodName, parameterTypes);
        Transactional annotation = method.getAnnotation(Transactional.class);
        assertNotNull(annotation, () -> type.getSimpleName() + "." + methodName + " should declare @Transactional");
        assertTrue(annotation.readOnly(), () -> type.getSimpleName() + "." + methodName + " should be readOnly");
    }
}
