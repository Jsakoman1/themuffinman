package com.themuffinman.app.trust.controller;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.trust.dto.SafetyReportRequestDTO;
import com.themuffinman.app.trust.dto.SafetyReportResponseDTO;
import com.themuffinman.app.trust.service.SafetyReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequiredArgsConstructor @RequestMapping("/trust/reports")
public class SafetyReportController {
    private final SafetyReportService service;
    @GetMapping("/me") public List<SafetyReportResponseDTO> getMine(@AuthenticationPrincipal AppUser user) { return service.getMine(user); }
    @PostMapping public SafetyReportResponseDTO create(@RequestBody SafetyReportRequestDTO request, @AuthenticationPrincipal AppUser user) { return service.create(request, user); }
}
