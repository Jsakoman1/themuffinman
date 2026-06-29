package com.themuffinman.app.docs;

import com.themuffinman.app.things.repository.ThingBorrowRequestRepository;
import com.themuffinman.app.things.repository.ThingListingRepository;
import com.themuffinman.app.workmarket.repository.QuestApplicationRepository;
import com.themuffinman.app.workmarket.repository.QuestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryFetchProfileContractTest {

    private static final Map<Class<?>, List<String>> REQUIRED_FETCH_PROFILES = Map.of(
            QuestRepository.class,
            List.of("findForQuestList", "findForQuestListByIds", "findForQuestDetail", "findForOwnerStatusList"),
            QuestApplicationRepository.class,
            List.of(
                    "findForQuestApplicationManagement",
                    "findForApplicantDashboard",
                    "findForViewerApplication",
                    "findForViewerApplicationWithStatus",
                    "findForQuestApplicationDetail",
                    "findForApplicationDetail",
                    "findForQuestApplicationsByStatus",
                    "findForAdminApplicationList"
            ),
            ThingListingRepository.class,
            List.of("findAvailableForCatalog", "findForOwnerDashboard", "findForListingDetail"),
            ThingBorrowRequestRepository.class,
            List.of("findPendingForCatalogViewer", "findForBorrowRequestDetail")
    );

    @Test
    void readRepositoriesExposeNamedFetchProfilesForDtoSurfaces() {
        REQUIRED_FETCH_PROFILES.forEach((repositoryClass, requiredProfiles) -> requiredProfiles.forEach(profileName -> {
            Method method = findMethod(repositoryClass, profileName);
            assertNotNull(method, repositoryClass.getSimpleName() + " is missing fetch profile " + profileName);
            assertTrue(
                    method.isAnnotationPresent(Query.class),
                    repositoryClass.getSimpleName() + "." + profileName + " must declare an explicit fetch query"
            );
        }));
    }

    private Method findMethod(Class<?> repositoryClass, String methodName) {
        for (Method method : repositoryClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }
}
