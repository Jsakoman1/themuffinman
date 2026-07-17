package com.themuffinman.app.notification.controller;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.notification.dto.AttentionCenterDTO;
import com.themuffinman.app.notification.service.AttentionCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController @RequiredArgsConstructor @RequestMapping("/attention")
public class AttentionCenterController {
    private final AttentionCenterService service;
    @GetMapping("/me") public AttentionCenterDTO getMine(@AuthenticationPrincipal AppUser user) { return service.getMine(user); }
}
