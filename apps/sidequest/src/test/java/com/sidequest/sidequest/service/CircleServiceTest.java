package com.sidequest.sidequest.service;

import com.sidequest.sidequest.dto.CircleRequestCreateDTO;
import com.sidequest.sidequest.dto.CircleBlockCreateDTO;
import com.sidequest.sidequest.dto.CircleOverviewDTO;
import com.sidequest.sidequest.dto.CircleRelationDTO;
import com.sidequest.sidequest.dto.CircleRequestResponseDTO;
import com.sidequest.sidequest.dto.CircleRequestListResponseDTO;
import com.sidequest.sidequest.dto.CircleRelationStatus;
import com.sidequest.sidequest.dto.CircleSearchResultDTO;
import com.sidequest.sidequest.dto.CircleSearchResultListResponseDTO;
import com.sidequest.sidequest.dto.CircleContactDTO;
import com.sidequest.sidequest.dto.CircleContactListResponseDTO;
import com.sidequest.sidequest.dto.ConnectionCircleUpdateDTO;
import com.sidequest.sidequest.mapper.CircleRequestMgr;
import com.sidequest.sidequest.model.AppUser;
import com.sidequest.sidequest.model.AppUserRole;
import com.sidequest.sidequest.model.CircleRequest;
import com.sidequest.sidequest.model.CircleGroup;
import com.sidequest.sidequest.model.CircleMembership;
import com.sidequest.sidequest.repository.AppUserRepository;
import com.sidequest.sidequest.repository.CircleRequestRepository;
import com.sidequest.sidequest.repository.CircleMembershipRepository;
import com.sidequest.sidequest.repository.CircleGroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CircleServiceTest {

    @Mock
    private CircleRequestRepository circleRequestRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private CircleGroupRepository circleGroupRepository;

    @Mock
    private CircleMembershipRepository circleMembershipRepository;

    @Mock
    private CircleRequestMgr circleRequestMgr;

    @Mock
    private QuestNewsService questNewsService;

    @InjectMocks
    private CircleService circleService;

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

        assertThrows(ResponseStatusException.class, () -> circleService.deleteCircleRequest(3L, blocked));
    }

    @Test
    void getOverviewCombinesCircleCollections() {
        AppUser currentUser = createUser(1L, "requester");
        when(circleRequestRepository.findAcceptedByUserId(1L)).thenReturn(List.of());
        when(circleRequestRepository.findIncomingPendingByRecipientId(1L)).thenReturn(List.of());
        when(circleRequestRepository.findOutgoingPendingByRequesterId(1L)).thenReturn(List.of());

        CircleOverviewDTO result = circleService.getOverview(currentUser);

        assertEquals(0, result.getConnectionCount());
        assertEquals(0, result.getUnassignedConnectionCount());
        assertEquals(0, result.getIncomingRequestCount());
        assertEquals(0, result.getOutgoingRequestCount());
    }

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

        when(appUserRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(circleRequestRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
        when(circleRequestRepository.save(org.mockito.ArgumentMatchers.any(CircleRequest.class))).thenReturn(savedRequest);
        when(circleRequestMgr.toDto(savedRequest)).thenReturn(responseDTO);

        CircleRequestResponseDTO result = circleService.createCircleRequest(requestDTO, requester);

        assertEquals(10L, result.getId());
        ArgumentCaptor<CircleRequest> circleCaptor = ArgumentCaptor.forClass(CircleRequest.class);
        verify(circleRequestRepository).save(circleCaptor.capture());
        CircleRequest savedCircle = circleCaptor.getValue();
        assertEquals(requester, savedCircle.getRequester());
        assertEquals(recipient, savedCircle.getRecipient());
    }

    @Test
    void createCircleRequestThrowsWhenRecipientIsSelf() {
        AppUser requester = createUser(1L, "requester");
        CircleRequestCreateDTO requestDTO = CircleRequestCreateDTO.builder()
                .recipientId(requester.getId())
                .build();

        assertThrows(ResponseStatusException.class, () -> circleService.createCircleRequest(requestDTO, requester));
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

        circleService.acceptCircleRequest(3L, recipient);

        assertEquals(true, circleRequest.getAcceptedAt() != null);
        verify(questNewsService).notifyCircleRequestAccepted(requester, recipient);
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

        when(appUserRepository.findById(2L)).thenReturn(Optional.of(blocked));
        when(circleRequestRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
        when(circleRequestRepository.save(org.mockito.ArgumentMatchers.any(CircleRequest.class))).thenReturn(savedRequest);
        when(circleRequestMgr.toDto(savedRequest)).thenReturn(responseDTO);

        CircleRequestResponseDTO result = circleService.blockCircleUser(dto, blocker);

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

        when(appUserRepository.findById(2L)).thenReturn(Optional.of(blocked));
        when(circleRequestRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(existingBlock));

        assertThrows(ResponseStatusException.class, () -> circleService.blockCircleUser(dto, blocker));
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

        when(appUserRepository.findById(2L)).thenReturn(Optional.of(blocked));
        when(circleRequestRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(existingBlock));

        assertThrows(ResponseStatusException.class, () -> circleService.blockCircleUser(dto, blocker));
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

        when(appUserRepository.findById(2L)).thenReturn(Optional.of(blocked));
        when(circleRequestRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(existingBlock));

        circleService.unblockCircleUser(blocked.getId(), blocker);

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

        when(appUserRepository.findById(2L)).thenReturn(Optional.of(blocked));
        when(circleRequestRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(existingBlock));

        assertThrows(ResponseStatusException.class, () -> circleService.unblockCircleUser(blocked.getId(), blocker));
    }

    @Test
    void searchCircleUsersMarksOutgoingRequests() {
        AppUser currentUser = createUser(1L, "requester");
        AppUser candidate = createUser(2L, "candidate");
        candidate.setProfileDescription("Local helper");

        CircleRequest request = new CircleRequest();
        request.setRequester(currentUser);
        request.setRecipient(candidate);

        when(appUserRepository.findAll()).thenReturn(List.of(currentUser, candidate));
        when(circleRequestRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(request));

        List<CircleSearchResultDTO> result = circleService.searchCircleUsers(currentUser, "cand");

        assertEquals(1, result.size());
        assertEquals(CircleRelationStatus.OUTGOING_REQUEST, result.getFirst().getRelationStatus());
        assertEquals("candidate", result.getFirst().getUsername());
    }

    @Test
    void searchCircleUsersIgnoresBareAtQueries() {
        AppUser currentUser = createUser(1L, "requester");

        List<CircleSearchResultDTO> result = circleService.searchCircleUsers(currentUser, "@");

        assertEquals(0, result.size());
    }

    @Test
    void searchCircleUsersIgnoresShortQueries() {
        AppUser currentUser = createUser(1L, "requester");

        List<CircleSearchResultDTO> result = circleService.searchCircleUsers(currentUser, "a");

        assertEquals(0, result.size());
    }

    @Test
    void searchCircleUsersMarksBlockedUsers() {
        AppUser currentUser = createUser(1L, "requester");
        AppUser candidate = createUser(2L, "candidate");

        CircleRequest request = new CircleRequest();
        request.setRequester(currentUser);
        request.setRecipient(candidate);
        request.setBlockedAt(Instant.now());
        request.setBlockedBy(currentUser);

        when(appUserRepository.findAll()).thenReturn(List.of(currentUser, candidate));
        when(circleRequestRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(request));

        List<CircleSearchResultDTO> result = circleService.searchCircleUsers(currentUser, "cand");

        assertEquals(1, result.size());
        assertEquals(CircleRelationStatus.BLOCKED, result.getFirst().getRelationStatus());
        assertEquals(true, result.getFirst().isBlockedByCurrentUser());
    }

    @Test
    void getConnectionsFiltersByCircleAndPaginatesResults() {
        AppUser currentUser = createUser(1L, "requester");
        AppUser contactOne = createUser(2L, "alice");
        AppUser contactTwo = createUser(3L, "bob");

        CircleGroup friends = new CircleGroup();
        friends.setId(10L);
        friends.setName("Friends");

        CircleRequest relationOne = new CircleRequest();
        relationOne.setId(20L);
        relationOne.setRequester(currentUser);
        relationOne.setRecipient(contactOne);
        relationOne.setAcceptedAt(Instant.now());

        CircleRequest relationTwo = new CircleRequest();
        relationTwo.setId(21L);
        relationTwo.setRequester(currentUser);
        relationTwo.setRecipient(contactTwo);
        relationTwo.setAcceptedAt(Instant.now());

        CircleMembership membership = new CircleMembership();
        membership.setCircle(friends);
        membership.setMember(contactOne);

        when(circleMembershipRepository.findByCircleOwnerId(1L)).thenReturn(List.of(membership));
        when(circleRequestRepository.findAcceptedByUserId(1L)).thenReturn(List.of(relationOne, relationTwo));

        CircleContactListResponseDTO result = circleService.getConnections(currentUser, "alice", null, false, 0, 1);

        assertEquals(1, result.getItems().size());
        assertEquals(2L, result.getItems().getFirst().getUserId());
        assertEquals(1, result.getTotalItems());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    void incomingRequestsCanBeFilteredAndPagedOnBackend() {
        AppUser currentUser = createUser(1L, "requester");
        AppUser alice = createUser(2L, "alice");
        AppUser bob = createUser(3L, "bob");

        CircleRequest requestOne = new CircleRequest();
        requestOne.setId(11L);
        requestOne.setRequester(alice);
        requestOne.setRecipient(currentUser);

        CircleRequest requestTwo = new CircleRequest();
        requestTwo.setId(12L);
        requestTwo.setRequester(bob);
        requestTwo.setRecipient(currentUser);

        CircleRequestResponseDTO first = CircleRequestResponseDTO.builder()
                .id(11L)
                .requesterUsername("alice")
                .requesterProfileDescription("Local helper")
                .build();
        CircleRequestResponseDTO second = CircleRequestResponseDTO.builder()
                .id(12L)
                .requesterUsername("bob")
                .requesterProfileDescription("Remote mentor")
                .build();

        when(circleRequestRepository.findIncomingPendingByRecipientId(1L)).thenReturn(List.of(requestOne, requestTwo));
        when(circleRequestMgr.toDto(requestOne)).thenReturn(first);
        when(circleRequestMgr.toDto(requestTwo)).thenReturn(second);

        CircleRequestListResponseDTO result = circleService.getIncomingRequests(currentUser, "helper", 0, 1);

        assertEquals(1, result.getItems().size());
        assertEquals(1, result.getTotalItems());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    void searchCircleUsersMatchesProfileDescriptionAndPaginatesResults() {
        AppUser currentUser = createUser(1L, "requester");
        AppUser helper = createUser(2L, "helper");
        helper.setProfileDescription("Local helper for errands");
        AppUser other = createUser(3L, "other");
        other.setProfileDescription("Something else");

        when(appUserRepository.findAll()).thenReturn(List.of(currentUser, helper, other));
        when(circleRequestRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
        when(circleRequestRepository.findBetweenUsers(1L, 3L)).thenReturn(Optional.empty());

        CircleSearchResultListResponseDTO result = circleService.searchCircleUsers(currentUser, "helper", 0, 1);

        assertEquals(1, result.getItems().size());
        assertEquals(2L, result.getItems().getFirst().getId());
        assertEquals(1, result.getTotalItems());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    void getInviteCandidatesReturnsOnlyUsersWithoutExistingRelations() {
        AppUser currentUser = createUser(1L, "requester");
        AppUser available = createUser(2L, "available");
        AppUser incomingRequester = createUser(3L, "incoming");
        AppUser blockedUser = createUser(4L, "blocked");
        AppUser circleUser = createUser(5L, "circle");
        AppUser outgoingRecipient = createUser(6L, "outgoing");

        CircleRequest incomingRequest = new CircleRequest();
        incomingRequest.setRequester(incomingRequester);
        incomingRequest.setRecipient(currentUser);

        CircleRequest blockedRequest = new CircleRequest();
        blockedRequest.setRequester(currentUser);
        blockedRequest.setRecipient(blockedUser);
        blockedRequest.setBlockedAt(Instant.now());
        blockedRequest.setBlockedBy(currentUser);

        CircleRequest circleRequest = new CircleRequest();
        circleRequest.setRequester(currentUser);
        circleRequest.setRecipient(circleUser);
        circleRequest.setAcceptedAt(Instant.now());

        CircleRequest outgoingRequest = new CircleRequest();
        outgoingRequest.setRequester(currentUser);
        outgoingRequest.setRecipient(outgoingRecipient);

        when(appUserRepository.findAll()).thenReturn(List.of(
                currentUser,
                available,
                incomingRequester,
                blockedUser,
                circleUser,
                outgoingRecipient
        ));
        when(circleRequestRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
        when(circleRequestRepository.findBetweenUsers(1L, 3L)).thenReturn(Optional.of(incomingRequest));
        when(circleRequestRepository.findBetweenUsers(1L, 4L)).thenReturn(Optional.of(blockedRequest));
        when(circleRequestRepository.findBetweenUsers(1L, 5L)).thenReturn(Optional.of(circleRequest));
        when(circleRequestRepository.findBetweenUsers(1L, 6L)).thenReturn(Optional.of(outgoingRequest));

        List<CircleSearchResultDTO> result = circleService.getInviteCandidates(currentUser);

        assertEquals(1, result.size());
        assertEquals(2L, result.getFirst().getId());
        assertEquals(CircleRelationStatus.NONE, result.getFirst().getRelationStatus());
    }

    @Test
    void getRelationWithUserReturnsBlockedByCurrentUserFlag() {
        AppUser currentUser = createUser(1L, "requester");
        AppUser candidate = createUser(2L, "candidate");

        CircleRequest request = new CircleRequest();
        request.setRequester(currentUser);
        request.setRecipient(candidate);
        request.setBlockedAt(Instant.now());
        request.setBlockedBy(currentUser);

        when(appUserRepository.findById(2L)).thenReturn(Optional.of(candidate));
        when(circleRequestRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(request));

        CircleRelationDTO relation = circleService.getRelationWithUser(currentUser, 2L);

        assertEquals(CircleRelationStatus.BLOCKED, relation.getRelationStatus());
        assertEquals(true, relation.isBlockedByCurrentUser());
    }

    @Test
    void updateConnectionCirclesSavesMembershipAndReturnsUpdatedContact() {
        AppUser owner = createUser(1L, "owner");
        AppUser contact = createUser(2L, "contact");

        CircleGroup circle = new CircleGroup();
        circle.setId(10L);
        circle.setOwner(owner);
        circle.setName("Friends");

        CircleRequest relation = new CircleRequest();
        relation.setId(40L);
        relation.setRequester(owner);
        relation.setRecipient(contact);
        relation.setAcceptedAt(Instant.now());

        CircleMembership savedMembership = new CircleMembership();
        savedMembership.setCircle(circle);
        savedMembership.setMember(contact);

        when(appUserRepository.findById(2L)).thenReturn(Optional.of(contact));
        when(circleRequestRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(relation));
        when(circleGroupRepository.findAllByOwnerIdAndIdIn(1L, Set.of(10L))).thenReturn(List.of(circle));
        when(circleMembershipRepository.findByMemberIdAndCircleOwnerId(2L, 1L))
                .thenReturn(List.of())
                .thenReturn(List.of(savedMembership));
        when(circleMembershipRepository.save(any(CircleMembership.class))).thenReturn(savedMembership);

        CircleContactDTO result = circleService.updateConnectionCircles(
                2L,
                ConnectionCircleUpdateDTO.builder().circleIds(List.of(10L)).build(),
                owner
        );

        verify(circleMembershipRepository).save(any(CircleMembership.class));
        assertEquals(List.of(10L), result.getCircleIds());
        assertEquals(List.of("Friends"), result.getCircleNames());
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
