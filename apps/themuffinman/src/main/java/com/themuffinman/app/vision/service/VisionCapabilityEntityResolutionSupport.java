package com.themuffinman.app.vision.service;

import com.themuffinman.app.common.normalization.SearchQueryNormalizer;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.identity.service.AppUserReadService;
import com.themuffinman.app.semantic.SemanticAliasRegistry;
import com.themuffinman.app.semantic.SemanticEntityFamily;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.social.service.CircleReadService;
import com.themuffinman.app.workmarket.dto.ApplicationAllowedActionDTO;
import com.themuffinman.app.workmarket.dto.QuestAllowedActionDTO;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.QuestResponseDTO;
import com.themuffinman.app.workmarket.service.WorkmarketQuestApplicationReadService;
import com.themuffinman.app.workmarket.service.WorkmarketQuestReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
final class VisionCapabilityEntityResolutionSupport {

    private final AppUserRepository appUserRepository;
    private final AppUserReadService appUserReadService;
    private final CircleReadService circleReadService;
    private final WorkmarketQuestApplicationReadService questApplicationReadService;
    private final WorkmarketQuestReadService questReadService;
    private final SemanticAliasRegistry semanticAliasRegistry;
    private static final Pattern QUEST_ID_PATTERN = Pattern.compile("(?i)(?:quest\\s*#?\\s*|#)(\\d+)|^(\\d+)$");
    private static final Pattern APPLICATION_ID_PATTERN = Pattern.compile("(?i)(?:application\\s*#?\\s*|#)(\\d+)|^(\\d+)$");
    private static final Pattern CIRCLE_ID_PATTERN = Pattern.compile("(?i)(?:circle\\s*#?\\s*|#)(\\d+)|^(\\d+)$");
    private static final Pattern USER_ID_PATTERN = Pattern.compile("(?i)(?:user\\s*#?\\s*|profile\\s*#?\\s*|#)(\\d+)|^(\\d+)$");

    VisionResolvedUserTarget resolveCircleRequestRecipient(AppUser currentUser, String query) {
        if (!hasText(query)) {
            return VisionResolvedUserTarget.unresolved("Who should receive the circle request? Say a username, email, or name fragment.");
        }

        String normalizedTargetQuery = normalizeEntityQuery(SemanticEntityFamily.USER, query).toLowerCase(Locale.ROOT);
        List<AppUser> matches = appUserRepository.searchByUsernameOrEmail(normalizedTargetQuery).stream()
                .filter(candidate -> candidate != null && currentUser != null && !candidate.getId().equals(currentUser.getId()))
                .toList();
        if (matches.isEmpty()) {
            return VisionResolvedUserTarget.unresolved("I could not identify one person for \"" + query.trim() + "\".");
        }
        List<AppUser> exactMatches = matches.stream()
                .filter(candidate -> candidate.getUsername() != null && candidate.getUsername().equalsIgnoreCase(query.trim()))
                .toList();
        if (exactMatches.size() == 1) {
            AppUser target = exactMatches.getFirst();
            return VisionResolvedUserTarget.resolved(target.getId(), target.getUsername());
        }
        if (matches.size() == 1) {
            AppUser target = matches.getFirst();
            return VisionResolvedUserTarget.resolved(target.getId(), target.getUsername());
        }
        String suggestions = matches.stream()
                .limit(3)
                .map(AppUser::getUsername)
                .reduce((left, right) -> left + ", " + right)
                .orElse("matching users");
        return VisionResolvedUserTarget.unresolved("I found several people matching \"" + query.trim() + "\": " + suggestions + ". Say the exact username.");
    }

    VisionResolvedCircleRequestTarget resolveIncomingCircleRequest(AppUser currentUser, String query) {
        return resolveCircleRequest(currentUser, query, true);
    }

    VisionResolvedCircleRequestTarget resolveAccessiblePendingCircleRequest(AppUser currentUser, String query) {
        if (!hasText(query)) {
            return VisionResolvedCircleRequestTarget.unresolved("Who is the person on this pending circle request? Say the exact username.");
        }
        VisionResolvedCircleRequestTarget incoming = resolveCircleRequest(currentUser, query, true);
        if (incoming.resolved()) {
            return incoming;
        }
        VisionResolvedCircleRequestTarget outgoing = resolveCircleRequest(currentUser, query, false);
        if (outgoing.resolved()) {
            return outgoing;
        }
        if (incoming.blockingMessage() != null && outgoing.blockingMessage() != null) {
            return VisionResolvedCircleRequestTarget.unresolved("I could not find one pending circle request for \"" + query.trim() + "\".");
        }
        return incoming.blockingMessage() == null ? outgoing : incoming;
    }

    VisionResolvedCircleTarget resolveOwnedCircle(AppUser currentUser, String query) {
        if (!hasText(query)) {
            return VisionResolvedCircleTarget.unresolved("What circle should I use? Say the circle name or circle id.");
        }

        Long circleId = extractCircleId(query);
        List<CircleGroupResponseDTO> circles = circleReadService.getCircles(currentUser);
        List<CircleGroupResponseDTO> candidates = circles.stream()
                .filter(circle -> matchesCircleQuery(circle, query, circleId))
                .toList();
        if (candidates.isEmpty()) {
            return VisionResolvedCircleTarget.unresolved("I could not find one owned circle from \"" + query.trim() + "\". Say the exact circle name or circle id.");
        }
        List<CircleGroupResponseDTO> exactCandidates = candidates.stream()
                .filter(circle -> circle.getName() != null && circle.getName().trim().equalsIgnoreCase(query.trim()))
                .toList();
        if (exactCandidates.size() == 1) {
            return toResolvedCircleTarget(exactCandidates.getFirst());
        }
        if (candidates.size() == 1) {
            return toResolvedCircleTarget(candidates.get(0));
        }
        String suggestions = candidates.stream()
                .limit(3)
                .map(circle -> "#" + circle.getId() + " " + circle.getName())
                .reduce((left, right) -> left + ", " + right)
                .orElse("matching circles");
        return VisionResolvedCircleTarget.unresolved("I found several circles matching \"" + query.trim() + "\": " + suggestions + ". Say the exact circle name or circle id.");
    }

    VisionResolvedQuestTarget resolveApplicationQuest(AppUser currentUser, String query) {
        if (!hasText(query)) {
            return VisionResolvedQuestTarget.unresolved("What quest should I apply to? Say the quest title or quest id.");
        }

        Long questId = extractQuestId(query);
        if (questId != null) {
            try {
                QuestResponseDTO quest = questReadService.getQuestResponseById(questId, currentUser);
                if (quest.getAllowedActions() == null || !quest.getAllowedActions().contains(QuestAllowedActionDTO.APPLY)) {
                    return VisionResolvedQuestTarget.unresolved("You cannot apply to quest #" + questId + ".");
                }
                return VisionResolvedQuestTarget.resolved(quest.getId(), quest.getTitle(), quest.getCreatorUsername(), requiresApplicationPrice(quest), formatRewardLabel(quest));
            } catch (RuntimeException ignored) {
                return VisionResolvedQuestTarget.unresolved("I could not find an applyable quest with id " + questId + ".");
            }
        }

        String normalizedQuery = normalizeEntityQuery(SemanticEntityFamily.QUEST, query).toLowerCase(Locale.ROOT);
        List<QuestResponseDTO> candidates = questReadService.getAllQuestResponses(currentUser).stream()
                .filter(quest -> quest.getAllowedActions() != null && quest.getAllowedActions().contains(QuestAllowedActionDTO.APPLY))
                .filter(quest -> matchesQuestQuery(quest, normalizedQuery))
                .toList();
        if (candidates.isEmpty()) {
            return VisionResolvedQuestTarget.unresolved("I could not find one open quest you can apply to from \"" + query.trim() + "\". Say the exact quest title or quest id.");
        }

        List<QuestResponseDTO> exactTitleCandidates = candidates.stream()
                .filter(quest -> quest.getTitle() != null && quest.getTitle().trim().equalsIgnoreCase(query.trim()))
                .toList();
        if (exactTitleCandidates.size() == 1) {
            QuestResponseDTO quest = exactTitleCandidates.get(0);
            return VisionResolvedQuestTarget.resolved(quest.getId(), quest.getTitle(), quest.getCreatorUsername(), requiresApplicationPrice(quest), formatRewardLabel(quest));
        }

        if (candidates.size() == 1) {
            QuestResponseDTO quest = candidates.get(0);
            return VisionResolvedQuestTarget.resolved(quest.getId(), quest.getTitle(), quest.getCreatorUsername(), requiresApplicationPrice(quest), formatRewardLabel(quest));
        }

        String suggestions = candidates.stream()
                .limit(3)
                .map(quest -> "#" + quest.getId() + " " + quest.getTitle())
                .reduce((left, right) -> left + ", " + right)
                .orElse("matching quests");
        return VisionResolvedQuestTarget.unresolved("I found several applyable quests matching \"" + query.trim() + "\": " + suggestions + ". Say the exact quest title or quest id.");
    }

    VisionResolvedManagedApplicationTarget resolveManagedPendingApplication(
            AppUser currentUser,
            String questQuery,
            String applicantQuery,
            ApplicationAllowedActionDTO requiredAction
    ) {
        VisionResolvedQuestTarget questTarget = resolveManagedQuest(currentUser, questQuery);
        if (!questTarget.resolved()) {
            return VisionResolvedManagedApplicationTarget.unresolved(questTarget.blockingMessage());
        }
        if (!hasText(applicantQuery)) {
            return VisionResolvedManagedApplicationTarget.unresolved("Who is the applicant? Say the applicant username.");
        }

        String normalizedApplicantQuery = SearchQueryNormalizer.normalize(applicantQuery).toLowerCase(Locale.ROOT);
        List<QuestApplicationResponseDTO> candidates = questApplicationReadService.getApplicationsForQuest(questTarget.questId(), currentUser).stream()
                .filter(application -> application.getAllowedActions() != null && application.getAllowedActions().contains(requiredAction))
                .filter(application -> matchesApplicantQuery(application, applicantQuery, normalizedApplicantQuery))
                .toList();
        if (candidates.isEmpty()) {
            return VisionResolvedManagedApplicationTarget.unresolved("I could not find one pending application for \"" + applicantQuery.trim()
                    + "\" on " + questTarget.questTitle() + ".");
        }

        List<QuestApplicationResponseDTO> exactCandidates = candidates.stream()
                .filter(application -> application.getApplicantUsername() != null
                        && application.getApplicantUsername().trim().equalsIgnoreCase(applicantQuery.trim()))
                .toList();
        if (exactCandidates.size() == 1) {
            return toResolvedManagedApplicationTarget(exactCandidates.get(0));
        }
        if (candidates.size() == 1) {
            return toResolvedManagedApplicationTarget(candidates.get(0));
        }

        String suggestions = candidates.stream()
                .limit(3)
                .map(QuestApplicationResponseDTO::getApplicantUsername)
                .reduce((left, right) -> left + ", " + right)
                .orElse("matching applicants");
        return VisionResolvedManagedApplicationTarget.unresolved("I found several pending applicants matching \"" + applicantQuery.trim()
                + "\" on " + questTarget.questTitle() + ": " + suggestions + ". Say the exact username.");
    }

    VisionResolvedApplicationTarget resolveMyPendingApplication(
            AppUser currentUser,
            String query,
            ApplicationAllowedActionDTO requiredAction
    ) {
        if (!hasText(query)) {
            return VisionResolvedApplicationTarget.unresolved("What application should I use? Say the quest title or quest id.");
        }

        Long questId = extractQuestId(query);
        List<QuestApplicationResponseDTO> candidates = questApplicationReadService.getApplicationsForApplicant(currentUser).stream()
                .filter(application -> application.getAllowedActions() != null && application.getAllowedActions().contains(requiredAction))
                .filter(application -> matchesApplicationQuery(application, query, questId))
                .toList();
        if (candidates.isEmpty()) {
            return VisionResolvedApplicationTarget.unresolved(requiredAction == ApplicationAllowedActionDTO.WITHDRAW
                    ? "I could not find one pending application you can withdraw from \"" + query.trim() + "\". Say the exact quest title or quest id."
                    : "I could not find one pending application you can edit from \"" + query.trim() + "\". Say the exact quest title or quest id.");
        }

        List<QuestApplicationResponseDTO> exactTitleCandidates = candidates.stream()
                .filter(application -> application.getQuestTitle() != null
                        && application.getQuestTitle().trim().equalsIgnoreCase(query.trim()))
                .toList();
        if (exactTitleCandidates.size() == 1) {
            return toResolvedApplicationTarget(exactTitleCandidates.get(0));
        }

        if (candidates.size() == 1) {
            return toResolvedApplicationTarget(candidates.get(0));
        }

        String suggestions = candidates.stream()
                .limit(3)
                .map(application -> "#" + application.getQuestId() + " " + application.getQuestTitle())
                .reduce((left, right) -> left + ", " + right)
                .orElse("matching applications");
        return VisionResolvedApplicationTarget.unresolved("I found several pending applications matching \"" + query.trim() + "\": "
                + suggestions + ". Say the exact quest title or quest id.");
    }

    VisionResolvedApplicationTarget resolveMyApplicationDetail(AppUser currentUser, String query) {
        if (!hasText(query)) {
            return VisionResolvedApplicationTarget.unresolved("What application should I open? Say the application id or the exact quest title.");
        }

        Long applicationId = extractApplicationId(query);
        List<QuestApplicationResponseDTO> applications = questApplicationReadService.getApplicationsForApplicant(currentUser);
        if (applicationId != null) {
            return applications.stream()
                    .filter(application -> applicationId.equals(application.getId()))
                    .findFirst()
                    .map(this::toResolvedApplicationTarget)
                    .orElseGet(() -> VisionResolvedApplicationTarget.unresolved(
                            "I could not find one application with id " + applicationId + "."
                    ));
        }

        List<QuestApplicationResponseDTO> candidates = applications.stream()
                .filter(application -> matchesApplicationDetailQuery(application, query))
                .toList();
        if (candidates.isEmpty()) {
            return VisionResolvedApplicationTarget.unresolved(
                    "I could not find one application from \"" + query.trim() + "\". Say the exact quest title or application id."
            );
        }

        List<QuestApplicationResponseDTO> exactTitleCandidates = candidates.stream()
                .filter(application -> application.getQuestTitle() != null
                        && application.getQuestTitle().trim().equalsIgnoreCase(query.trim()))
                .toList();
        if (exactTitleCandidates.size() == 1) {
            return toResolvedApplicationTarget(exactTitleCandidates.get(0));
        }
        if (candidates.size() == 1) {
            return toResolvedApplicationTarget(candidates.get(0));
        }

        String suggestions = candidates.stream()
                .limit(3)
                .map(application -> "#" + application.getId() + " " + application.getQuestTitle())
                .reduce((left, right) -> left + ", " + right)
                .orElse("matching applications");
        return VisionResolvedApplicationTarget.unresolved(
                "I found several applications matching \"" + query.trim() + "\": " + suggestions + ". Say the exact quest title or application id."
        );
    }

    VisionResolvedQuestTarget resolveVisibleQuest(AppUser currentUser, String query) {
        if (!hasText(query)) {
            return VisionResolvedQuestTarget.unresolved("What quest should I open? Say the quest title or quest id.");
        }

        Long questId = extractQuestId(query);
        if (questId != null) {
            try {
                QuestResponseDTO quest = questReadService.getQuestResponseById(questId, currentUser);
                return VisionResolvedQuestTarget.resolved(quest.getId(), quest.getTitle(), quest.getCreatorUsername(), requiresApplicationPrice(quest), formatRewardLabel(quest));
            } catch (RuntimeException ignored) {
                return VisionResolvedQuestTarget.unresolved("I could not find one visible quest with id " + questId + ".");
            }
        }

        String normalizedQuery = normalizeEntityQuery(SemanticEntityFamily.QUEST, query).toLowerCase(Locale.ROOT);
        List<QuestResponseDTO> candidates = questReadService.getAllQuestResponses(currentUser).stream()
                .filter(quest -> matchesQuestQuery(quest, normalizedQuery))
                .toList();
        if (candidates.isEmpty()) {
            return VisionResolvedQuestTarget.unresolved("I could not find one visible quest from \"" + query.trim() + "\". Say the exact quest title or quest id.");
        }

        List<QuestResponseDTO> exactTitleCandidates = candidates.stream()
                .filter(quest -> quest.getTitle() != null && quest.getTitle().trim().equalsIgnoreCase(query.trim()))
                .toList();
        if (exactTitleCandidates.size() == 1) {
            QuestResponseDTO quest = exactTitleCandidates.get(0);
            return VisionResolvedQuestTarget.resolved(quest.getId(), quest.getTitle(), quest.getCreatorUsername(), requiresApplicationPrice(quest), formatRewardLabel(quest));
        }
        if (candidates.size() == 1) {
            QuestResponseDTO quest = candidates.get(0);
            return VisionResolvedQuestTarget.resolved(quest.getId(), quest.getTitle(), quest.getCreatorUsername(), requiresApplicationPrice(quest), formatRewardLabel(quest));
        }

        String suggestions = candidates.stream()
                .limit(3)
                .map(quest -> "#" + quest.getId() + " " + quest.getTitle())
                .reduce((left, right) -> left + ", " + right)
                .orElse("matching quests");
        return VisionResolvedQuestTarget.unresolved("I found several visible quests matching \"" + query.trim() + "\": " + suggestions + ". Say the exact quest title or quest id.");
    }

    VisionResolvedUserTarget resolveUserProfileTarget(AppUser currentUser, String query) {
        if (!hasText(query)) {
            return VisionResolvedUserTarget.unresolved("What profile should I open? Say a username, email, or user id.");
        }

        Long userId = extractUserId(query);
        if (userId != null) {
            try {
                AppUser user = appUserReadService.getAppUser(userId);
                if (user == null) {
                    return VisionResolvedUserTarget.unresolved("I could not find one profile with id " + userId + ".");
                }
                return VisionResolvedUserTarget.resolved(user.getId(), user.getUsername());
            } catch (RuntimeException ignored) {
                return VisionResolvedUserTarget.unresolved("I could not find one profile with id " + userId + ".");
            }
        }

        String normalizedTargetQuery = normalizeEntityQuery(SemanticEntityFamily.USER, query).toLowerCase(Locale.ROOT);
        List<AppUser> matches = appUserRepository.searchByUsernameOrEmail(normalizedTargetQuery);
        if (matches.isEmpty()) {
            return VisionResolvedUserTarget.unresolved("I could not identify one profile for \"" + query.trim() + "\".");
        }
        List<AppUser> exactMatches = matches.stream()
                .filter(candidate -> candidate.getUsername() != null && candidate.getUsername().equalsIgnoreCase(query.trim()))
                .toList();
        if (exactMatches.size() == 1) {
            AppUser target = exactMatches.getFirst();
            return VisionResolvedUserTarget.resolved(target.getId(), target.getUsername());
        }
        if (matches.size() == 1) {
            AppUser target = matches.getFirst();
            return VisionResolvedUserTarget.resolved(target.getId(), target.getUsername());
        }
        String suggestions = matches.stream()
                .limit(3)
                .map(AppUser::getUsername)
                .reduce((left, right) -> left + ", " + right)
                .orElse("matching users");
        return VisionResolvedUserTarget.unresolved("I found several profiles matching \"" + query.trim() + "\": " + suggestions + ". Say the exact username or user id.");
    }

    private VisionResolvedCircleRequestTarget resolveCircleRequest(AppUser currentUser, String query, boolean incoming) {
        if (!hasText(query)) {
            return VisionResolvedCircleRequestTarget.unresolved("Who is the person on this circle request? Say the exact username.");
        }
        List<CircleRequestResponseDTO> requests = incoming
                ? circleReadService.getIncomingRequests(currentUser)
                : circleReadService.getOutgoingRequests(currentUser);
        String normalizedQuery = normalizeEntityQuery(SemanticEntityFamily.USER, query).toLowerCase(Locale.ROOT);
        List<CircleRequestResponseDTO> matches = requests.stream()
                .filter(request -> request.getAcceptedAt() == null)
                .filter(request -> matchesCircleRequestQuery(request, query, normalizedQuery))
                .toList();
        if (matches.isEmpty()) {
            return VisionResolvedCircleRequestTarget.unresolved(incoming
                    ? "I could not find one incoming circle request from \"" + query.trim() + "\"."
                    : "I could not find one outgoing circle invite for \"" + query.trim() + "\".");
        }
        List<CircleRequestResponseDTO> exactMatches = matches.stream()
                .filter(request -> request.getCounterpartUsername() != null
                        && request.getCounterpartUsername().equalsIgnoreCase(query.trim()))
                .toList();
        if (exactMatches.size() == 1) {
            return toResolvedCircleRequestTarget(exactMatches.getFirst(), incoming);
        }
        if (matches.size() == 1) {
            return toResolvedCircleRequestTarget(matches.getFirst(), incoming);
        }
        String suggestions = matches.stream()
                .limit(3)
                .map(CircleRequestResponseDTO::getCounterpartUsername)
                .reduce((left, right) -> left + ", " + right)
                .orElse("matching users");
        return VisionResolvedCircleRequestTarget.unresolved("I found several pending circle requests matching \"" + query.trim() + "\": " + suggestions + ". Say the exact username.");
    }

    private boolean matchesCircleRequestQuery(CircleRequestResponseDTO request, String rawQuery, String normalizedQuery) {
        if (!hasText(rawQuery)) {
            return false;
        }
        String haystack = String.join(" ",
                nullToEmpty(request.getCounterpartUsername()),
                nullToEmpty(request.getRequesterUsername()),
                nullToEmpty(request.getRecipientUsername()))
                .toLowerCase(Locale.ROOT);
        return haystack.contains(normalizedQuery);
    }

    private VisionResolvedCircleRequestTarget toResolvedCircleRequestTarget(CircleRequestResponseDTO request, boolean incoming) {
        return VisionResolvedCircleRequestTarget.resolved(
                request.getId(),
                request.getCounterpartUserId(),
                request.getCounterpartUsername(),
                incoming
        );
    }

    private boolean matchesCircleQuery(CircleGroupResponseDTO circle, String rawQuery, Long circleId) {
        if (circle == null || !hasText(rawQuery)) {
            return false;
        }
        if (circleId != null) {
            return circleId.equals(circle.getId());
        }
        String query = normalizeEntityQuery(SemanticEntityFamily.CIRCLE, rawQuery).toLowerCase(Locale.ROOT);
        String haystack = String.join(" ",
                nullToEmpty(circle.getName()),
                nullToEmpty(circle.getMemberPreviewLabel()))
                .toLowerCase(Locale.ROOT);
        return haystack.contains(query);
    }

    private VisionResolvedCircleTarget toResolvedCircleTarget(CircleGroupResponseDTO circle) {
        return VisionResolvedCircleTarget.resolved(
                circle.getId(),
                circle.getName(),
                String.valueOf(circle.getMemberCount())
        );
    }

    private Long extractCircleId(String query) {
        if (!hasText(query)) {
            return null;
        }
        Matcher matcher = CIRCLE_ID_PATTERN.matcher(query.trim());
        if (!matcher.find()) {
            return null;
        }
        String direct = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        if (direct == null || direct.isBlank()) {
            return null;
        }
        return Long.parseLong(direct);
    }

    private Long extractQuestId(String query) {
        if (!hasText(query)) {
            return null;
        }
        Matcher matcher = QUEST_ID_PATTERN.matcher(query.trim());
        if (!matcher.find()) {
            return null;
        }
        String direct = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        if (direct == null || direct.isBlank()) {
            return null;
        }
        return Long.parseLong(direct);
    }

    private Long extractApplicationId(String query) {
        if (!hasText(query)) {
            return null;
        }
        Matcher matcher = APPLICATION_ID_PATTERN.matcher(query.trim());
        if (!matcher.find()) {
            return null;
        }
        String direct = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        if (direct == null || direct.isBlank()) {
            return null;
        }
        return Long.parseLong(direct);
    }

    private Long extractUserId(String query) {
        if (!hasText(query)) {
            return null;
        }
        Matcher matcher = USER_ID_PATTERN.matcher(query.trim());
        if (!matcher.find()) {
            return null;
        }
        String direct = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        if (direct == null || direct.isBlank()) {
            return null;
        }
        return Long.parseLong(direct);
    }

    private boolean matchesQuestQuery(QuestResponseDTO quest, String query) {
        if (!hasText(query)) {
            return false;
        }
        String haystack = String.join(" ",
                nullToEmpty(quest.getTitle()),
                nullToEmpty(quest.getDescription()),
                nullToEmpty(quest.getCreatorUsername()))
                .toLowerCase(Locale.ROOT);
        return haystack.contains(query);
    }

    private boolean matchesApplicationQuery(QuestApplicationResponseDTO application, String rawQuery, Long questId) {
        if (application == null || !hasText(rawQuery)) {
            return false;
        }
        if (questId != null) {
            return questId.equals(application.getQuestId());
        }
        String query = normalizeEntityQuery(SemanticEntityFamily.APPLICATION, rawQuery).toLowerCase(Locale.ROOT);
        String haystack = String.join(" ",
                nullToEmpty(application.getQuestTitle()),
                nullToEmpty(application.getQuestDescription()),
                nullToEmpty(application.getQuestCreatorUsername()))
                .toLowerCase(Locale.ROOT);
        return haystack.contains(query);
    }

    private boolean matchesApplicationDetailQuery(QuestApplicationResponseDTO application, String rawQuery) {
        if (application == null || !hasText(rawQuery)) {
            return false;
        }
        Long applicationId = extractApplicationId(rawQuery);
        if (applicationId != null) {
            return applicationId.equals(application.getId());
        }
        String query = normalizeEntityQuery(SemanticEntityFamily.APPLICATION, rawQuery).toLowerCase(Locale.ROOT);
        String haystack = String.join(" ",
                nullToEmpty(application.getQuestTitle()),
                nullToEmpty(application.getQuestDescription()),
                nullToEmpty(application.getQuestCreatorUsername()),
                "#" + application.getId())
                .toLowerCase(Locale.ROOT);
        return haystack.contains(query);
    }

    private boolean matchesApplicantQuery(QuestApplicationResponseDTO application, String rawQuery, String normalizedQuery) {
        if (application == null || !hasText(rawQuery)) {
            return false;
        }
        String haystack = String.join(" ",
                nullToEmpty(application.getApplicantUsername()),
                nullToEmpty(application.getApplicantProfileDescription()))
                .toLowerCase(Locale.ROOT);
        return haystack.contains(normalizedQuery);
    }

    private VisionResolvedQuestTarget resolveManagedQuest(AppUser currentUser, String query) {
        if (!hasText(query)) {
            return VisionResolvedQuestTarget.unresolved("What quest should I use? Say the quest title or quest id.");
        }

        Long questId = extractQuestId(query);
        List<QuestResponseDTO> candidates = questReadService.getAllQuestResponses(currentUser).stream()
                .filter(quest -> quest.getAllowedActions() != null && quest.getAllowedActions().contains(QuestAllowedActionDTO.VIEW_APPLICATIONS))
                .filter(quest -> matchesManagedQuestQuery(quest, query, questId))
                .toList();
        if (candidates.isEmpty()) {
            return VisionResolvedQuestTarget.unresolved("I could not find one quest you can manage from \"" + query.trim() + "\". Say the exact quest title or quest id.");
        }
        List<QuestResponseDTO> exactCandidates = candidates.stream()
                .filter(quest -> quest.getTitle() != null && quest.getTitle().trim().equalsIgnoreCase(query.trim()))
                .toList();
        if (exactCandidates.size() == 1) {
            QuestResponseDTO quest = exactCandidates.get(0);
            return VisionResolvedQuestTarget.resolved(quest.getId(), quest.getTitle(), quest.getCreatorUsername(), requiresApplicationPrice(quest), formatRewardLabel(quest));
        }
        if (candidates.size() == 1) {
            QuestResponseDTO quest = candidates.get(0);
            return VisionResolvedQuestTarget.resolved(quest.getId(), quest.getTitle(), quest.getCreatorUsername(), requiresApplicationPrice(quest), formatRewardLabel(quest));
        }
        String suggestions = candidates.stream()
                .limit(3)
                .map(quest -> "#" + quest.getId() + " " + quest.getTitle())
                .reduce((left, right) -> left + ", " + right)
                .orElse("matching quests");
        return VisionResolvedQuestTarget.unresolved("I found several manageable quests matching \"" + query.trim() + "\": " + suggestions + ". Say the exact quest title or quest id.");
    }

    private boolean matchesManagedQuestQuery(QuestResponseDTO quest, String rawQuery, Long questId) {
        if (quest == null || !hasText(rawQuery)) {
            return false;
        }
        if (questId != null) {
            return questId.equals(quest.getId());
        }
        String query = normalizeEntityQuery(SemanticEntityFamily.QUEST, rawQuery).toLowerCase(Locale.ROOT);
        return matchesQuestQuery(quest, query);
    }

    private boolean requiresApplicationPrice(QuestResponseDTO quest) {
        return quest != null && quest.getAwardAmount() != null && quest.getAwardAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    private VisionResolvedApplicationTarget toResolvedApplicationTarget(QuestApplicationResponseDTO application) {
        boolean priceRequired = application.getProposedPrice() != null && application.getProposedPrice().compareTo(BigDecimal.ZERO) > 0;
        return VisionResolvedApplicationTarget.resolved(
                application.getQuestId(),
                application.getQuestTitle(),
                application.getQuestCreatorUsername(),
                priceRequired,
                application.getProposedPrice() == null ? "Free" : application.getProposedPrice().stripTrailingZeros().toPlainString(),
                application.getMessage(),
                application.getProposedPrice() == null ? null : application.getProposedPrice().stripTrailingZeros().toPlainString(),
                application.getId()
        );
    }

    private VisionResolvedManagedApplicationTarget toResolvedManagedApplicationTarget(QuestApplicationResponseDTO application) {
        return VisionResolvedManagedApplicationTarget.resolved(
                application.getQuestId(),
                application.getQuestTitle(),
                application.getApplicantUsername(),
                application.getMessage(),
                application.getProposedPrice() == null ? null : application.getProposedPrice().stripTrailingZeros().toPlainString(),
                application.getId()
        );
    }

    private String formatRewardLabel(QuestResponseDTO quest) {
        return VisionCapabilityPreviewSupport.formatRewardLabel(quest);
    }

    private String normalizeEntityQuery(SemanticEntityFamily family, String query) {
        return semanticAliasRegistry.normalizeQuery(family, SearchQueryNormalizer.normalize(query));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
