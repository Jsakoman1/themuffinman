package com.themuffinman.app.activity.controller;

import com.themuffinman.app.activity.dto.ActivityItemDTO;
import com.themuffinman.app.activity.service.ActivityReadService;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequiredArgsConstructor @RequestMapping("/activity")
public class ActivityController {
    private final ActivityReadService service;
    @GetMapping("/me") public List<ActivityItemDTO> getMine(@AuthenticationPrincipal AppUser user) { return service.getMine(user); }
    @PostMapping("/resume/{resumeKey}/dismiss") public void dismiss(@PathVariable String resumeKey, @AuthenticationPrincipal AppUser user) { service.dismiss(resumeKey, user); }
}
