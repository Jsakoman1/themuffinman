package com.themuffinman.app.social.service;

import com.themuffinman.app.social.dto.CircleRequestCreateDTO;
import com.themuffinman.app.social.dto.CircleBlockCreateDTO;
import com.themuffinman.app.social.dto.AdminCircleOverviewDTO;
import com.themuffinman.app.social.dto.BulkCircleMembershipActionDTO;
import com.themuffinman.app.social.dto.BulkCircleMembershipUpdateDTO;
import com.themuffinman.app.social.dto.CircleOverviewDTO;
import com.themuffinman.app.social.dto.CircleRelationDTO;
import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.social.dto.CircleRequestListResponseDTO;
import com.themuffinman.app.social.dto.CircleRelationStatusDTO;
import com.themuffinman.app.social.dto.CircleSearchResultDTO;
import com.themuffinman.app.social.dto.CircleSearchResultListResponseDTO;
import com.themuffinman.app.social.dto.CircleContactDTO;
import com.themuffinman.app.social.dto.CircleContactListResponseDTO;
import com.themuffinman.app.social.dto.ConnectionCircleUpdateDTO;
import com.themuffinman.app.social.mapper.CircleRequestMgr;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.social.model.CircleRequest;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.social.model.CircleMembership;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.identity.service.AppUserLookupService;
import com.themuffinman.app.location.service.LocationGeoService;
import com.themuffinman.app.social.repository.CircleRequestRepository;
import com.themuffinman.app.social.repository.CircleGroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CircleServiceTest {

    @Mock
    private CircleRequestRepository circleRequestRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private AppUserLookupService appUserLookupService;

    @Mock
    private CircleGroupRepository circleGroupRepository;

    @Mock
    private CircleMembershipService circleMembershipService;

    @Mock
    private CircleRelationService circleRelationService;

    @Mock
    private CircleRequestMgr circleRequestMgr;

    @Mock
    private LocationGeoService locationGeoService;

    @Spy
    private CircleViewAssembler circleViewAssembler = new CircleViewAssembler(
            new SocialPresentationHelper(),
            new SocialRelationActionHelper()
    );

    @Spy
    private CircleSearchQueryService circleSearchQueryService = new CircleSearchQueryService();

    @Spy
    private CircleAdminOverviewAssembler circleAdminOverviewAssembler = new CircleAdminOverviewAssembler(
            new SocialPresentationHelper()
    );

    private CircleReadService circleReadService;

    private CircleDiscoveryService circleDiscoveryService;

    private CircleService circleService;

    @BeforeEach
    void setUp() {
        circleDiscoveryService = new CircleDiscoveryService(
                appUserRepository,
                circleRequestRepository,
                circleViewAssembler,
                circleSearchQueryService,
                locationGeoService
        );
        circleReadService = new CircleReadService(
                circleRequestRepository,
                appUserRepository,
                appUserLookupService,
                circleGroupRepository,
                circleMembershipService,
                circleRelationService,
                circleRequestMgr,
                circleAdminOverviewAssembler,
                circleViewAssembler,
                circleSearchQueryService
        );
        circleService = new CircleService(
                appUserLookupService,
                circleGroupRepository,
                circleMembershipService,
                circleRelationService,
                circleDiscoveryService,
                circleReadService,
                circleViewAssembler
        );
    }

    @Test
    void deleteCircleRequestRejectsBlockedRelationship() {
        AppUser blocker = createUser(1L, "blocker");
        AppUser blocked = createUser(2L, "blocked");
        when(circleRelationService.isCircleBetween(blocker, blocked)).thenReturn(false);

        assertEquals(false, circleService.isCircleBetween(blocker, blocked));
    }

    @Test
    void getOverviewCombinesCircleCollections() {
        AppUser currentUser = createUser(1L, "requester");
        when(circleRequestRepository.findAcceptedByUserId(1L)).thenReturn(List.of());
        when(circleRequestRepository.findIncomingPendingByRecipientId(1L)).thenReturn(List.of());
        when(circleRequestRepository.findOutgoingPendingByRequesterId(1L)).thenReturn(List.of());
        when(circleMembershipService.getMembershipsByUserIdForOwner(1L)).thenReturn(Map.of());

        CircleOverviewDTO result = circleService.getOverview(currentUser);

        assertEquals(0, result.getConnectionCount());
        assertEquals(0, result.getUnassignedConnectionCount());
        assertEquals(0, result.getIncomingRequestCount());
        assertEquals(0, result.getOutgoingRequestCount());
    }

    @Test
    void getAdminOverviewFiltersCirclesAndRelationsByQuery() {
        AppUser admin = createUser(1L, "admin");
        admin.setRole(AppUserRole.ADMIN);
        AppUser owner = createUser(2L, "owner");
        AppUser member = createUser(3L, "helper");
        AppUser requester = createUser(4L, "requester");
        AppUser recipient = createUser(5L, "helper-user");
        AppUser otherRequester = createUser(6L, "other");
        AppUser otherRecipient = createUser(7L, "person");

        CircleGroup matchingCircle = new CircleGroup();
        matchingCircle.setId(10L);
        matchingCircle.setName("Helpers");
        matchingCircle.setOwner(owner);

        CircleMembership membership = new CircleMembership();
        membership.setCircle(matchingCircle);
        membership.setMember(member);
        matchingCircle.setMemberships(Set.of(membership));

        CircleGroup nonMatchingCircle = new CircleGroup();
        nonMatchingCircle.setId(11L);
        nonMatchingCircle.setName("Drivers");
        nonMatchingCircle.setOwner(owner);
        nonMatchingCircle.setMemberships(Set.of());

        CircleRequest matchingPendingRequest = new CircleRequest();
        matchingPendingRequest.setId(20L);
        matchingPendingRequest.setRequester(requester);
        matchingPendingRequest.setRecipient(recipient);

        CircleRequest nonMatchingAcceptedRequest = new CircleRequest();
        nonMatchingAcceptedRequest.setId(21L);
        nonMatchingAcceptedRequest.setRequester(otherRequester);
        nonMatchingAcceptedRequest.setRecipient(otherRecipient);
        nonMatchingAcceptedRequest.setAcceptedAt(Instant.now());

        when(circleGroupRepository.findAllDetailed()).thenReturn(List.of(nonMatchingCircle, matchingCircle));
        when(circleRequestRepository.findAllDetailed()).thenReturn(List.of(matchingPendingRequest, nonMatchingAcceptedRequest));

        AdminCircleOverviewDTO result = circleService.getAdminOverview(admin, "helper");

        assertEquals(1, result.getCircles().size());
        assertEquals("Helpers", result.getCircles().getFirst().getName());
        assertEquals("helper", result.getCircles().getFirst().getMemberPreviewLabel());
        assertEquals(0, result.getAcceptedConnections().size());
        assertEquals(1, result.getPendingRequests().size());
        assertEquals("helper-user", result.getPendingRequests().getFirst().getRecipientUsername());
        assertEquals("Pending", result.getPendingRequests().getFirst().getStatusLabel());
    }

    @Test
    void getAdminOverviewBuildsBlockedRowsWithPresentationFields() {
        AppUser admin = createUser(1L, "admin");
        admin.setRole(AppUserRole.ADMIN);
        AppUser requester = createUser(2L, "requester");
        AppUser recipient = createUser(3L, "recipient");

        CircleRequest blockedRequest = new CircleRequest();
        blockedRequest.setId(22L);
        blockedRequest.setRequester(requester);
        blockedRequest.setRecipient(recipient);
        blockedRequest.setBlockedAt(Instant.now());

        when(circleGroupRepository.findAllDetailed()).thenReturn(List.of());
        when(circleRequestRepository.findAllDetailed()).thenReturn(List.of(blockedRequest));

        AdminCircleOverviewDTO result = circleService.getAdminOverview(admin, null);

        assertEquals(1, result.getBlockedRelations().size());
        assertEquals("Blocked", result.getBlockedRelations().getFirst().getStatusLabel());
        assertEquals("badge--danger", result.getBlockedRelations().getFirst().getStatusBadgeClass());
    }

    @Test
    void createCircleRequestUsesAuthenticatedUserAsRequester() {
        AppUser requester = createUser(1L, "requester");
        CircleRequestCreateDTO requestDTO = CircleRequestCreateDTO.builder()
                .recipientId(2L)
                .build();
        CircleRequestResponseDTO responseDTO = CircleRequestResponseDTO.builder()
                .id(10L)
                .build();

        when(circleRelationService.createCircleRequest(requestDTO, requester)).thenReturn(responseDTO);

        CircleRequestResponseDTO result = circleService.createCircleRequest(requestDTO, requester);

        assertEquals(10L, result.getId());
        verify(circleRelationService).createCircleRequest(requestDTO, requester);
    }

    @Test
    void createCircleRequestThrowsWhenRecipientIsSelf() {
        AppUser requester = createUser(1L, "requester");
        CircleRequestCreateDTO requestDTO = CircleRequestCreateDTO.builder()
                .recipientId(requester.getId())
                .build();
        when(circleRelationService.createCircleRequest(requestDTO, requester))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST));

        assertThrows(ResponseStatusException.class, () -> circleService.createCircleRequest(requestDTO, requester));
    }

    @Test
    void acceptCircleRequestSetsAcceptedAtTimestamp() {
        AppUser recipient = createUser(2L, "recipient");
        CircleRequestResponseDTO responseDTO = CircleRequestResponseDTO.builder()
                .id(3L)
                .build();

        when(circleRelationService.acceptCircleRequest(3L, recipient)).thenReturn(responseDTO);

        CircleRequestResponseDTO result = circleService.acceptCircleRequest(3L, recipient);

        assertEquals(3L, result.getId());
        verify(circleRelationService).acceptCircleRequest(3L, recipient);
    }

    @Test
    void blockCircleUserStoresBlockedRelationship() {
        AppUser blocker = createUser(1L, "blocker");
        CircleBlockCreateDTO dto = CircleBlockCreateDTO.builder()
                .blockedUserId(2L)
                .build();
        CircleRequestResponseDTO responseDTO = CircleRequestResponseDTO.builder()
                .id(11L)
                .build();

        when(circleRelationService.blockCircleUser(dto, blocker)).thenReturn(responseDTO);

        CircleRequestResponseDTO result = circleService.blockCircleUser(dto, blocker);

        assertEquals(11L, result.getId());
        verify(circleRelationService).blockCircleUser(dto, blocker);
    }

    @Test
    void blockCircleUserRejectsDuplicateBlockBySameUser() {
        AppUser blocker = createUser(1L, "blocker");
        AppUser blocked = createUser(2L, "blocked");
        CircleBlockCreateDTO dto = CircleBlockCreateDTO.builder()
                .blockedUserId(blocked.getId())
                .build();
        when(circleRelationService.blockCircleUser(dto, blocker))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT));

        assertThrows(ResponseStatusException.class, () -> circleService.blockCircleUser(dto, blocker));
    }

    @Test
    void blockCircleUserRejectsWhenOtherUserAlreadyBlockedYou() {
        AppUser blocker = createUser(1L, "blocker");
        AppUser blocked = createUser(2L, "blocked");
        CircleBlockCreateDTO dto = CircleBlockCreateDTO.builder()
                .blockedUserId(blocked.getId())
                .build();
        when(circleRelationService.blockCircleUser(dto, blocker))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT));

        assertThrows(ResponseStatusException.class, () -> circleService.blockCircleUser(dto, blocker));
    }

    @Test
    void unblockCircleUserDeletesBlockedRelationWhenRequesterIsBlocker() {
        AppUser blocker = createUser(1L, "blocker");

        circleService.unblockCircleUser(2L, blocker);

        verify(circleRelationService).unblockCircleUser(2L, blocker);
    }

    @Test
    void unblockCircleUserRejectsWhenCurrentUserDidNotCreateBlock() {
        AppUser blocker = createUser(1L, "blocker");
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN))
                .when(circleRelationService)
                .unblockCircleUser(2L, blocker);

        assertThrows(ResponseStatusException.class, () -> circleService.unblockCircleUser(2L, blocker));
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
        assertEquals(CircleRelationStatusDTO.OUTGOING_REQUEST, result.getFirst().getRelationStatus());
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
        assertEquals(CircleRelationStatusDTO.BLOCKED, result.getFirst().getRelationStatus());
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

        when(circleMembershipService.getMembershipsByUserIdForOwner(1L)).thenReturn(Map.of(2L, List.of(membership)));
        when(circleRequestRepository.findAcceptedByUserId(1L)).thenReturn(List.of(relationOne, relationTwo));

        CircleContactListResponseDTO result = circleService.getConnections(currentUser, "alice", null, false, 0, 1);

        assertEquals(1, result.getItems().size());
        assertEquals(2L, result.getItems().getFirst().getUserId());
        assertEquals(List.of("Friends"), result.getItems().getFirst().getCircleNames());
        assertEquals("Friends", result.getItems().getFirst().getCircleSummaryLabel());
        assertEquals(1, result.getTotalItems());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    void getCirclesBuildsMemberPreviewLabel() {
        AppUser currentUser = createUser(1L, "owner");
        AppUser alice = createUser(2L, "alice");
        AppUser bob = createUser(3L, "bob");

        CircleGroup circle = new CircleGroup();
        circle.setId(10L);
        circle.setName("Friends");
        circle.setOwner(currentUser);

        CircleMembership aliceMembership = new CircleMembership();
        aliceMembership.setCircle(circle);
        aliceMembership.setMember(alice);

        CircleMembership bobMembership = new CircleMembership();
        bobMembership.setCircle(circle);
        bobMembership.setMember(bob);

        circle.setMemberships(Set.of(aliceMembership, bobMembership));

        when(circleGroupRepository.findAllDetailedByOwnerId(1L)).thenReturn(List.of(circle));

        var result = circleService.getCircles(currentUser);

        assertEquals(1, result.size());
        assertEquals("circle:10", result.getFirst().getResolutionKey());
        assertEquals("Friends", result.getFirst().getResolutionLabel());
        assertEquals(true, result.getFirst().isExactResolutionEligible());
        assertEquals(2, result.getFirst().getMemberCount());
        assertEquals("alice, bob", result.getFirst().getMemberPreviewLabel());
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
                .requesterId(2L)
                .requesterUsername("alice")
                .requesterProfileDescription("Local helper")
                .build();
        CircleRequestResponseDTO second = CircleRequestResponseDTO.builder()
                .id(12L)
                .requesterId(3L)
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
        assertEquals("circle-request:11", result.getItems().getFirst().getResolutionKey());
        assertEquals("alice", result.getItems().getFirst().getResolutionLabel());
        assertEquals(true, result.getItems().getFirst().isExactResolutionEligible());
        assertEquals(2L, result.getItems().getFirst().getCounterpartUserId());
        assertEquals("alice", result.getItems().getFirst().getCounterpartUsername());
        assertEquals("Wants to connect", result.getItems().getFirst().getRequestSummaryLabel());
        assertEquals("ACCEPT_REQUEST", result.getItems().getFirst().getPrimaryAction().getType());
        assertEquals("Accept", result.getItems().getFirst().getPrimaryAction().getLabel());
        assertEquals("DECLINE_REQUEST", result.getItems().getFirst().getSecondaryAction().getType());
    }

    @Test
    void outgoingRequestsExposeViewerCentricCounterpartFields() {
        AppUser currentUser = createUser(1L, "requester");
        AppUser bob = createUser(3L, "bob");
        bob.setProfileDescription("Remote mentor");

        CircleRequest request = new CircleRequest();
        request.setId(12L);
        request.setRequester(currentUser);
        request.setRecipient(bob);

        CircleRequestResponseDTO dto = CircleRequestResponseDTO.builder()
                .id(12L)
                .requesterId(1L)
                .requesterUsername("requester")
                .recipientId(3L)
                .recipientUsername("bob")
                .recipientProfileDescription("Remote mentor")
                .build();

        when(circleRequestRepository.findOutgoingPendingByRequesterId(1L)).thenReturn(List.of(request));
        when(circleRequestMgr.toDto(request)).thenReturn(dto);

        CircleRequestListResponseDTO result = circleService.getOutgoingRequests(currentUser, "mentor", 0, 10);

        assertEquals(1, result.getItems().size());
        assertEquals("circle-request:12", result.getItems().getFirst().getResolutionKey());
        assertEquals("bob", result.getItems().getFirst().getResolutionLabel());
        assertEquals(true, result.getItems().getFirst().isExactResolutionEligible());
        assertEquals(3L, result.getItems().getFirst().getCounterpartUserId());
        assertEquals("bob", result.getItems().getFirst().getCounterpartUsername());
        assertEquals("Invite sent", result.getItems().getFirst().getRequestSummaryLabel());
        assertEquals("CANCEL_REQUEST", result.getItems().getFirst().getPrimaryAction().getType());
    }

    @Test
    void searchCircleUsersUsesSharedRelationActionsForBlockedByOtherUser() {
        AppUser currentUser = createUser(1L, "requester");
        AppUser candidate = createUser(2L, "candidate");

        CircleRequest request = new CircleRequest();
        request.setRequester(candidate);
        request.setRecipient(currentUser);
        request.setBlockedAt(Instant.now());
        request.setBlockedBy(candidate);

        when(appUserRepository.findAll()).thenReturn(List.of(currentUser, candidate));
        when(circleRequestRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(request));

        List<CircleSearchResultDTO> result = circleService.searchCircleUsers(currentUser, "cand");

        assertEquals(1, result.size());
        assertEquals("NONE", result.getFirst().getPrimaryAction().getType());
        assertEquals("Blocked by them", result.getFirst().getPrimaryAction().getLabel());
        assertEquals(false, result.getFirst().getPrimaryAction().isEnabled());
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
        assertEquals(CircleRelationStatusDTO.NONE, result.getFirst().getRelationStatus());
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

        assertEquals(CircleRelationStatusDTO.BLOCKED, relation.getRelationStatus());
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

        when(appUserLookupService.requireById(2L)).thenReturn(contact);
        when(circleRelationService.isCircleBetween(owner, contact)).thenReturn(true);
        when(circleRelationService.findRelation(owner, contact)).thenReturn(Optional.of(relation));
        when(circleMembershipService.getMembershipsForContact(2L, 1L))
                .thenReturn(List.of(savedMembership));

        CircleContactDTO result = circleService.updateConnectionCircles(
                2L,
                ConnectionCircleUpdateDTO.builder().circleIds(List.of(10L)).build(),
                owner
        );

        verify(circleMembershipService).syncConnectionCircles(owner, contact, List.of(10L));
        assertEquals(List.of(10L), result.getCircleIds());
        assertEquals(List.of("Friends"), result.getCircleNames());
    }

    @Test
    void updateConnectionCirclesBulkAddsSelectedCircleToMultipleUsers() {
        AppUser owner = createUser(1L, "owner");
        AppUser alice = createUser(2L, "alice");
        AppUser bob = createUser(3L, "bob");

        CircleGroup circle = new CircleGroup();
        circle.setId(10L);
        circle.setOwner(owner);
        circle.setName("Friends");

        when(circleGroupRepository.findByIdAndOwnerId(10L, 1L)).thenReturn(Optional.of(circle));
        when(appUserLookupService.requireById(2L)).thenReturn(alice);
        when(appUserLookupService.requireById(3L)).thenReturn(bob);
        when(circleRelationService.isCircleBetween(owner, alice)).thenReturn(true);
        when(circleRelationService.isCircleBetween(owner, bob)).thenReturn(true);
        when(circleMembershipService.getMembershipsForContact(2L, 1L)).thenReturn(List.of());
        when(circleMembershipService.getMembershipsForContact(3L, 1L)).thenReturn(List.of());

        circleService.updateConnectionCirclesBulk(BulkCircleMembershipUpdateDTO.builder()
                .circleId(10L)
                .userIds(List.of(2L, 3L))
                .action(BulkCircleMembershipActionDTO.ADD)
                .build(), owner);

        verify(circleMembershipService).syncConnectionCircles(owner, alice, List.of(10L));
        verify(circleMembershipService).syncConnectionCircles(owner, bob, List.of(10L));
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
