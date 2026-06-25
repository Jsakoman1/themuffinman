package com.themuffinman.app.social.controller;

import com.themuffinman.app.common.dto.ActionResultDTO;
import com.themuffinman.app.common.dto.ActionResults;
import com.themuffinman.app.social.dto.CircleRequestCreateDTO;
import com.themuffinman.app.social.dto.CircleBlockCreateDTO;
import com.themuffinman.app.social.dto.CircleRelationDTO;
import com.themuffinman.app.social.dto.CircleOverviewDTO;
import com.themuffinman.app.social.dto.CircleSearchResultDTO;
import com.themuffinman.app.social.dto.CircleSearchResultListResponseDTO;
import com.themuffinman.app.social.dto.CircleRequestResponseDTO;
import com.themuffinman.app.social.dto.CircleRequestListResponseDTO;
import com.themuffinman.app.social.dto.AdminCircleOverviewDTO;
import com.themuffinman.app.social.dto.CircleConnectionsQueryDTO;
import com.themuffinman.app.social.dto.CircleContactDTO;
import com.themuffinman.app.social.dto.CircleContactListResponseDTO;
import com.themuffinman.app.social.dto.ConnectionCircleUpdateDTO;
import com.themuffinman.app.social.dto.CircleGroupRequestDTO;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.social.dto.PageQueryDTO;
import com.themuffinman.app.social.dto.TextPageQueryDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.social.service.CircleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/circles")
@RequiredArgsConstructor
public class CircleController {
    private final CircleService circleService;

    @GetMapping("/me/overview")
    public CircleOverviewDTO getOverview(@AuthenticationPrincipal AppUser currentUser) {
        return circleService.getOverview(currentUser);
    }

    @GetMapping("/admin/overview")
    public AdminCircleOverviewDTO getAdminOverview(
            @RequestParam(value = "q", required = false) String query,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return circleService.getAdminOverview(currentUser, query);
    }

    @PostMapping("/groups")
    public ActionResultDTO createCircle(
            @Valid @RequestBody CircleGroupRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        circleService.createCircle(dto, currentUser);
        return ActionResults.of("CREATE_CIRCLE", "Circle created.");
    }

    @PutMapping("/groups/{id}")
    public CircleGroupResponseDTO updateCircle(
            @PathVariable Long id,
            @Valid @RequestBody CircleGroupRequestDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return circleService.updateCircle(id, dto, currentUser);
    }

    @DeleteMapping("/groups/{id}")
    public ActionResultDTO deleteCircle(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        circleService.deleteCircle(id, currentUser);
        return ActionResults.of("DELETE_CIRCLE", "Circle removed.");
    }

    @DeleteMapping("/admin/groups/{id}")
    public ActionResultDTO deleteCircleAsAdmin(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        circleService.deleteCircleAsAdmin(id, currentUser);
        return ActionResults.of("DELETE_CIRCLE_AS_ADMIN", "Circle deleted.");
    }

    @GetMapping("/groups")
    public java.util.List<CircleGroupResponseDTO> getCircles(@AuthenticationPrincipal AppUser currentUser) {
        return circleService.getCircles(currentUser);
    }

    @PutMapping("/connections/{userId}/circles")
    public ActionResultDTO updateConnectionCircles(
            @PathVariable Long userId,
            @RequestBody ConnectionCircleUpdateDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        circleService.updateConnectionCircles(userId, dto, currentUser);
        return ActionResults.of("UPDATE_CONNECTION_CIRCLES", "Circles updated.");
    }

    @GetMapping
    public java.util.List<CircleRequestResponseDTO> getMyCircles(@AuthenticationPrincipal AppUser currentUser) {
        return circleService.getMyCircles(currentUser);
    }

    @GetMapping("/requests/incoming")
    public CircleRequestListResponseDTO getIncomingRequests(
            @ModelAttribute TextPageQueryDTO query,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return circleService.getIncomingRequests(
                currentUser,
                query.getQ(),
                query.getPage() == null ? 0 : query.getPage(),
                query.getSize() == null ? 8 : query.getSize()
        );
    }

    @GetMapping("/requests/outgoing")
    public CircleRequestListResponseDTO getOutgoingRequests(
            @ModelAttribute TextPageQueryDTO query,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return circleService.getOutgoingRequests(
                currentUser,
                query.getQ(),
                query.getPage() == null ? 0 : query.getPage(),
                query.getSize() == null ? 8 : query.getSize()
        );
    }

    @GetMapping("/candidates")
    public CircleSearchResultListResponseDTO getInviteCandidates(
            @ModelAttribute PageQueryDTO query,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return circleService.getInviteCandidatesPage(
                currentUser,
                query.getPage() == null ? 0 : query.getPage(),
                query.getSize() == null ? 12 : query.getSize()
        );
    }

    @GetMapping("/connections")
    public CircleContactListResponseDTO getConnections(
            @ModelAttribute CircleConnectionsQueryDTO query,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return circleService.getConnections(
                currentUser,
                query.getQ(),
                query.getCircleId(),
                Boolean.TRUE.equals(query.getUnassigned()),
                query.getPage() == null ? 0 : query.getPage(),
                query.getSize() == null ? 8 : query.getSize()
        );
    }

    @GetMapping("/relations/{userId}")
    public CircleRelationDTO getRelationWithUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return circleService.getRelationWithUser(currentUser, userId);
    }

    @GetMapping("/search")
    public CircleSearchResultListResponseDTO searchCircleUsers(
            @ModelAttribute TextPageQueryDTO query,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        return circleService.searchCircleUsers(
                currentUser,
                query.getQ(),
                query.getPage() == null ? 0 : query.getPage(),
                query.getSize() == null ? 12 : query.getSize()
        );
    }

    @PostMapping("/requests")
    public ActionResultDTO createCircleRequest(
            @Valid @RequestBody CircleRequestCreateDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        circleService.createCircleRequest(dto, currentUser);
        return ActionResults.of("CREATE_CIRCLE_REQUEST", "Connection invite sent.");
    }

    @PatchMapping("/requests/{id}/accept")
    public ActionResultDTO acceptCircleRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        circleService.acceptCircleRequest(id, currentUser);
        return ActionResults.of("ACCEPT_CIRCLE_REQUEST", "Invite accepted.");
    }

    @DeleteMapping("/requests/{id}")
    public ActionResultDTO deleteCircleRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        circleService.deleteCircleRequest(id, currentUser);
        return ActionResults.of("DELETE_CIRCLE_REQUEST", "Connection updated.");
    }

    @PostMapping("/blocks")
    public ActionResultDTO blockCircleUser(
            @Valid @RequestBody CircleBlockCreateDTO dto,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        circleService.blockCircleUser(dto, currentUser);
        return ActionResults.of("BLOCK_CIRCLE_USER", "User blocked.");
    }

    @DeleteMapping("/blocks/{userId}")
    public ActionResultDTO unblockCircleUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal AppUser currentUser
    ) {
        circleService.unblockCircleUser(userId, currentUser);
        return ActionResults.of("UNBLOCK_CIRCLE_USER", "User unblocked.");
    }
}
