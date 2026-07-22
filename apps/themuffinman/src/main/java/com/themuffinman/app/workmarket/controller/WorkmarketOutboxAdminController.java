package com.themuffinman.app.workmarket.controller;

import com.themuffinman.app.common.dto.ActionResultDTO;
import com.themuffinman.app.common.dto.ActionResults;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.model.AppUserRole;
import com.themuffinman.app.workmarket.service.WorkmarketApplicationNewsOutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/operations/workmarket-news-outbox")
@RequiredArgsConstructor
public class WorkmarketOutboxAdminController {

    private final WorkmarketApplicationNewsOutboxService outboxService;

    @PostMapping("/{eventId}/replay")
    public ActionResultDTO replay(@PathVariable UUID eventId, @AuthenticationPrincipal AppUser currentUser) {
        if (currentUser == null || currentUser.getRole() != AppUserRole.ADMIN) {
            throw ServiceErrors.forbidden("Admin access is required for outbox replay");
        }
        if (!outboxService.requestOperatorReplay(eventId, currentUser.getUsername())) {
            throw ServiceErrors.notFound("Outbox event not found");
        }
        return ActionResults.of("REPLAY_WORKMARKET_NEWS_OUTBOX", "Outbox event queued for replay.");
    }
}
