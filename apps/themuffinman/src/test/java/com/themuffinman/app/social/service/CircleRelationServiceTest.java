package com.themuffinman.app.social.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.identity.service.AppUserLookupService;
import com.themuffinman.app.social.dto.CircleBlockCreateDTO;
import com.themuffinman.app.social.dto.CircleRequestCreateDTO;
import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.social.mapper.CircleRequestMgr;
import com.themuffinman.app.social.model.CircleRequest;
import com.themuffinman.app.social.repository.CircleRequestRepository;
import com.themuffinman.app.workmarket.service.QuestNewsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CircleRelationServiceTest {

    @Mock
    private CircleRequestRepository circleRequestRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private AppUserLookupService appUserLookupService;

    @Mock
    private CircleRequestMgr circleRequestMgr;

    @Mock
    private QuestNewsService questNewsService;

    @InjectMocks
    private CircleRelationService circleRelationService;

    @Test
    void createCircleRequestUsesAuthenticatedUserAsRequester() {
        AppUser requester = createUser(1L, "requester");
        AppUser recipient = createUser(2L, "recipient");
        CircleRequestCreateDTO requestDTO = CircleRequestCreateDTO.builder()
                .recipientId(recipient.getId())
                .build();
        CircleRequest savedRequest = new CircleRequest();
        CircleRequestResponseDTO responseDTO = CircleRequestResponseDTO.builder()
                .id(10L)
                .build();

        when(appUserLookupService.requireById(2L)).thenReturn(recipient);
        when(circleRequestRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
        when(circleRequestRepository.save(any(CircleRequest.class))).thenReturn(savedRequest);
        when(circleRequestMgr.toDto(savedRequest)).thenReturn(responseDTO);

        CircleRequestResponseDTO result = circleRelationService.createCircleRequest(requestDTO, requester);

        assertEquals(10L, result.getId());
        ArgumentCaptor<CircleRequest> circleCaptor = ArgumentCaptor.forClass(CircleRequest.class);
        verify(circleRequestRepository).save(circleCaptor.capture());
        CircleRequest savedCircle = circleCaptor.getValue();
        assertEquals(requester, savedCircle.getRequester());
        assertEquals(recipient, savedCircle.getRecipient());
        verify(questNewsService).notifyCircleRequestReceived(recipient, requester, savedRequest.getId());
    }

    @Test
    void createCircleRequestThrowsWhenRecipientIsSelf() {
        AppUser requester = createUser(1L, "requester");
        CircleRequestCreateDTO requestDTO = CircleRequestCreateDTO.builder()
                .recipientId(requester.getId())
                .build();

        assertThrows(ResponseStatusException.class, () -> circleRelationService.createCircleRequest(requestDTO, requester));
    }

    @Test
    void acceptCircleRequestSetsAcceptedAtTimestamp() {
        AppUser requester = createUser(1L, "requester");
        AppUser recipient = createUser(2L, "recipient");
        CircleRequest circleRequest = new CircleRequest();
        circleRequest.setId(3L);
        circleRequest.setRequester(requester);
        circleRequest.setRecipient(recipient);

        CircleRequestResponseDTO responseDTO = CircleRequestResponseDTO.builder()
                .id(3L)
                .build();

        when(circleRequestRepository.findById(3L)).thenReturn(Optional.of(circleRequest));
        when(circleRequestRepository.save(circleRequest)).thenReturn(circleRequest);
        when(circleRequestMgr.toDto(circleRequest)).thenReturn(responseDTO);

        circleRelationService.acceptCircleRequest(3L, recipient);

        assertEquals(true, circleRequest.getAcceptedAt() != null);
        verify(questNewsService).notifyCircleRequestAccepted(requester, recipient);
    }

    @Test
    void deleteCircleRequestRejectsBlockedRelationship() {
        AppUser blocker = createUser(1L, "blocker");
        AppUser blocked = createUser(2L, "blocked");
        CircleRequest relation = new CircleRequest();
        relation.setId(3L);
        relation.setRequester(blocker);
        relation.setRecipient(blocked);
        relation.setBlockedAt(Instant.now());
        relation.setBlockedBy(blocker);
        when(circleRequestRepository.findById(3L)).thenReturn(Optional.of(relation));

        assertThrows(ResponseStatusException.class, () -> circleRelationService.deleteCircleRequest(3L, blocked));
    }

    @Test
    void blockCircleUserStoresBlockedRelationship() {
        AppUser blocker = createUser(1L, "blocker");
        AppUser blocked = createUser(2L, "blocked");
        CircleBlockCreateDTO dto = CircleBlockCreateDTO.builder()
                .blockedUserId(blocked.getId())
                .build();
        CircleRequest savedRequest = new CircleRequest();
        CircleRequestResponseDTO responseDTO = CircleRequestResponseDTO.builder()
                .id(11L)
                .build();

        when(appUserLookupService.requireById(2L)).thenReturn(blocked);
        when(circleRequestRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
        when(circleRequestRepository.save(any(CircleRequest.class))).thenReturn(savedRequest);
        when(circleRequestMgr.toDto(savedRequest)).thenReturn(responseDTO);

        CircleRequestResponseDTO result = circleRelationService.blockCircleUser(dto, blocker);

        assertEquals(11L, result.getId());
        verify(circleRequestRepository).save(org.mockito.ArgumentMatchers.argThat(request ->
                request.getBlockedAt() != null
                        && request.getBlockedBy() == blocker
                        && request.getRequester() == blocker
                        && request.getRecipient() == blocked
        ));
    }

    @Test
    void blockCircleUserRejectsDuplicateBlockBySameUser() {
        AppUser blocker = createUser(1L, "blocker");
        AppUser blocked = createUser(2L, "blocked");
        CircleBlockCreateDTO dto = CircleBlockCreateDTO.builder()
                .blockedUserId(blocked.getId())
                .build();

        CircleRequest existingBlock = new CircleRequest();
        existingBlock.setRequester(blocker);
        existingBlock.setRecipient(blocked);
        existingBlock.setBlockedAt(Instant.now());
        existingBlock.setBlockedBy(blocker);

        when(appUserLookupService.requireById(2L)).thenReturn(blocked);
        when(circleRequestRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(existingBlock));

        assertThrows(ResponseStatusException.class, () -> circleRelationService.blockCircleUser(dto, blocker));
    }

    @Test
    void blockCircleUserRejectsWhenOtherUserAlreadyBlockedYou() {
        AppUser blocker = createUser(1L, "blocker");
        AppUser blocked = createUser(2L, "blocked");
        CircleBlockCreateDTO dto = CircleBlockCreateDTO.builder()
                .blockedUserId(blocked.getId())
                .build();

        CircleRequest existingBlock = new CircleRequest();
        existingBlock.setRequester(blocked);
        existingBlock.setRecipient(blocker);
        existingBlock.setBlockedAt(Instant.now());
        existingBlock.setBlockedBy(blocked);

        when(appUserLookupService.requireById(2L)).thenReturn(blocked);
        when(circleRequestRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(existingBlock));

        assertThrows(ResponseStatusException.class, () -> circleRelationService.blockCircleUser(dto, blocker));
    }

    @Test
    void unblockCircleUserDeletesBlockedRelationWhenRequesterIsBlocker() {
        AppUser blocker = createUser(1L, "blocker");
        AppUser blocked = createUser(2L, "blocked");

        CircleRequest existingBlock = new CircleRequest();
        existingBlock.setRequester(blocker);
        existingBlock.setRecipient(blocked);
        existingBlock.setBlockedAt(Instant.now());
        existingBlock.setBlockedBy(blocker);

        when(appUserLookupService.requireById(2L)).thenReturn(blocked);
        when(circleRequestRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(existingBlock));

        circleRelationService.unblockCircleUser(blocked.getId(), blocker);

        verify(circleRequestRepository).delete(existingBlock);
    }

    @Test
    void unblockCircleUserRejectsWhenCurrentUserDidNotCreateBlock() {
        AppUser blocker = createUser(1L, "blocker");
        AppUser blocked = createUser(2L, "blocked");

        CircleRequest existingBlock = new CircleRequest();
        existingBlock.setRequester(blocked);
        existingBlock.setRecipient(blocker);
        existingBlock.setBlockedAt(Instant.now());
        existingBlock.setBlockedBy(blocked);

        when(appUserLookupService.requireById(2L)).thenReturn(blocked);
        when(circleRequestRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(existingBlock));

        assertThrows(ResponseStatusException.class, () -> circleRelationService.unblockCircleUser(blocked.getId(), blocker));
    }

    @Test
    void isCircleBetweenReturnsFalseWhenEitherUserIsMissing() {
        assertFalse(circleRelationService.isCircleBetween(null, createUser(2L, "user")));
        assertFalse(circleRelationService.isCircleBetween(createUser(1L, "user"), null));
    }

    private AppUser createUser(Long id, String username) {
        AppUser appUser = new AppUser();
        appUser.setId(id);
        appUser.setUsername(username);
        appUser.setEmail(username + "@example.com");
        appUser.setRole(AppUserRole.USER);
        return appUser;
    }
}
