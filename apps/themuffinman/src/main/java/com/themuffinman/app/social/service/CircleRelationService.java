package com.themuffinman.app.social.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.service.AppUserLookupService;
import com.themuffinman.app.social.dto.CircleBlockCreateDTO;
import com.themuffinman.app.social.dto.CircleRequestCreateDTO;
import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.social.mapper.CircleRequestMgr;
import com.themuffinman.app.social.model.CircleRequest;
import com.themuffinman.app.social.repository.CircleRequestRepository;
import com.themuffinman.app.workmarket.service.QuestNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CircleRelationService {

    private final CircleRequestRepository circleRequestRepository;
    private final AppUserLookupService appUserLookupService;
    private final CircleRequestMgr circleRequestMgr;
    private final QuestNewsService questNewsService;

    @Transactional
    public CircleRequestResponseDTO createCircleRequest(CircleRequestCreateDTO dto, AppUser currentUser) {
        Long recipientId = requireTargetUserId(dto.getRecipientId(), "Recipient is required");
        validateNotSelfAction(currentUser, recipientId, "You cannot send a circle request to yourself");

        AppUser recipient = appUserLookupService.requireById(recipientId);
        CircleRequest existingRelation = findRelation(currentUser, recipient).orElse(null);
        if (existingRelation != null && existingRelation.getBlockedAt() != null) {
            throw ServiceErrors.conflict("This user is blocked");
        }

        if (existingRelation != null) {
            throw ServiceErrors.conflict("A circle already exists between these users");
        }

        CircleRequest circleRequest = new CircleRequest();
        circleRequest.setRequester(currentUser);
        circleRequest.setRecipient(recipient);
        CircleRequest saved = circleRequestRepository.save(circleRequest);
        return circleRequestMgr.toDto(saved);
    }

    @Transactional
    public CircleRequestResponseDTO acceptCircleRequest(Long requestId, AppUser currentUser) {
        CircleRequest circleRequest = requireIncomingRequest(requestId, currentUser);
        circleRequest.setAcceptedAt(Instant.now());
        CircleRequest saved = circleRequestRepository.save(circleRequest);
        questNewsService.notifyCircleRequestAccepted(saved.getRequester(), currentUser);
        return circleRequestMgr.toDto(saved);
    }

    @Transactional
    public void deleteCircleRequest(Long requestId, AppUser currentUser) {
        if (currentUser != null && currentUser.getRole() == com.themuffinman.app.identity.model.AppUserRole.ADMIN) {
            circleRequestRepository.delete(requireRequest(requestId));
            return;
        }

        CircleRequest circleRequest = requireRequestAccessibleByCurrentUser(requestId, currentUser);
        circleRequestRepository.delete(circleRequest);
    }

    @Transactional
    public CircleRequestResponseDTO blockCircleUser(CircleBlockCreateDTO dto, AppUser currentUser) {
        Long blockedUserId = requireTargetUserId(dto.getBlockedUserId(), "Blocked user is required");
        validateNotSelfAction(currentUser, blockedUserId, "You cannot block yourself");

        AppUser blockedUser = appUserLookupService.requireById(blockedUserId);
        CircleRequest existingRelation = findRelation(currentUser, blockedUser).orElse(null);
        if (existingRelation != null && existingRelation.getBlockedAt() != null) {
            if (existingRelation.getBlockedBy() != null && Objects.equals(existingRelation.getBlockedBy().getId(), currentUser.getId())) {
                throw ServiceErrors.conflict("This user is already blocked");
            }

            throw ServiceErrors.conflict("This user has blocked you");
        }

        if (existingRelation != null) {
            circleRequestRepository.delete(existingRelation);
        }

        CircleRequest circleRequest = new CircleRequest();
        circleRequest.setRequester(currentUser);
        circleRequest.setRecipient(blockedUser);
        circleRequest.setAcceptedAt(null);
        circleRequest.setBlockedAt(Instant.now());
        circleRequest.setBlockedBy(currentUser);
        CircleRequest saved = circleRequestRepository.save(circleRequest);
        return circleRequestMgr.toDto(saved);
    }

    @Transactional
    public void unblockCircleUser(Long blockedUserId, AppUser currentUser) {
        Long targetUserId = requireTargetUserId(blockedUserId, "Blocked user is required");
        validateNotSelfAction(currentUser, targetUserId, "You cannot unblock yourself");

        AppUser blockedUser = appUserLookupService.requireById(targetUserId);
        CircleRequest circleRequest = findRelation(currentUser, blockedUser)
                .orElseThrow(() -> ServiceErrors.notFound("Blocked user not found"));

        if (circleRequest.getBlockedAt() == null) {
            throw ServiceErrors.badRequest("This user is not blocked");
        }

        if (circleRequest.getBlockedBy() == null || !Objects.equals(circleRequest.getBlockedBy().getId(), currentUser.getId())) {
            throw ServiceErrors.forbidden("Only the user who blocked this person can unblock them");
        }

        circleRequestRepository.delete(circleRequest);
    }

    public boolean isCircleBetween(AppUser leftUser, AppUser rightUser) {
        if (leftUser == null || rightUser == null) {
            return false;
        }

        return findRelation(leftUser, rightUser)
                .map(circleRequest -> circleRequest.getAcceptedAt() != null)
                .orElse(false);
    }

    public Optional<CircleRequest> findRelation(AppUser leftUser, AppUser rightUser) {
        return circleRequestRepository.findBetweenUsers(leftUser.getId(), rightUser.getId());
    }

    private CircleRequest requireIncomingRequest(Long requestId, AppUser currentUser) {
        CircleRequest circleRequest = requireRequest(requestId);
        if (!circleRequest.getRecipient().getId().equals(currentUser.getId())) {
            throw ServiceErrors.forbidden("Only the recipient can accept this circle request");
        }

        if (circleRequest.getAcceptedAt() != null) {
            throw ServiceErrors.badRequest("This circle request has already been accepted");
        }

        return circleRequest;
    }

    private CircleRequest requireRequestAccessibleByCurrentUser(Long requestId, AppUser currentUser) {
        CircleRequest circleRequest = requireRequest(requestId);
        Long requesterId = circleRequest.getRequester().getId();
        Long recipientId = circleRequest.getRecipient().getId();

        if (!currentUser.getId().equals(requesterId) && !currentUser.getId().equals(recipientId)) {
            throw ServiceErrors.forbidden("You can only manage your own circle requests");
        }

        if (circleRequest.getBlockedAt() != null) {
            throw ServiceErrors.forbidden("Blocked relationships must be managed through the unblock action");
        }

        return circleRequest;
    }

    private CircleRequest requireRequest(Long requestId) {
        return circleRequestRepository.findById(requestId)
                .orElseThrow(() -> ServiceErrors.notFound("Circle request not found with id " + requestId));
    }

    private Long requireTargetUserId(Long targetUserId, String message) {
        if (targetUserId == null) {
            throw ServiceErrors.badRequest(message);
        }

        return targetUserId;
    }

    private void validateNotSelfAction(AppUser currentUser, Long targetUserId, String message) {
        if (currentUser.getId().equals(targetUserId)) {
            throw ServiceErrors.badRequest(message);
        }
    }

}
