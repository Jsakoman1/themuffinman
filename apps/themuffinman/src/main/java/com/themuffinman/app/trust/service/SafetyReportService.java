package com.themuffinman.app.trust.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.trust.dto.SafetyReportRequestDTO;
import com.themuffinman.app.trust.dto.SafetyReportResponseDTO;
import com.themuffinman.app.trust.model.SafetyReport;
import com.themuffinman.app.trust.repository.SafetyReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class SafetyReportService {
    private static final List<String> FAMILIES = List.of("user", "quest", "ride", "business", "thing", "circle", "chat");
    private final SafetyReportRepository repository;
    private final AppUserRepository userRepository;
    public List<SafetyReportResponseDTO> getMine(AppUser user) { return repository.findByReporterId(user.getId()).stream().map(this::toDto).toList(); }
    @Transactional
    public SafetyReportResponseDTO create(SafetyReportRequestDTO request, AppUser reporter) {
        if (request == null || request.getTargetFamily() == null || request.getTargetFamily().isBlank()) throw ServiceErrors.badRequest("Report target is required");
        String family = request.getTargetFamily().trim().toLowerCase(Locale.ROOT); if (!FAMILIES.contains(family)) throw ServiceErrors.badRequest("Unsupported report target");
        String reason = request.getReason() == null ? "" : request.getReason().trim(); if (reason.isBlank() || reason.length() > 1000) throw ServiceErrors.badRequest("Report reason is required and must be 1000 characters or less");
        if (reporter.getId().equals(request.getTargetUserId())) throw ServiceErrors.badRequest("You cannot report yourself");
        SafetyReport report = new SafetyReport(); report.setReporter(reporter); report.setTargetFamily(family); report.setTargetId(request.getTargetId()); report.setReason(reason);
        if (request.getTargetUserId() != null) report.setTargetUser(userRepository.findById(request.getTargetUserId()).orElseThrow(() -> ServiceErrors.notFound("Reported user not found")));
        return toDto(repository.save(report));
    }
    private SafetyReportResponseDTO toDto(SafetyReport report) { return SafetyReportResponseDTO.builder().id(report.getId()).targetFamily(report.getTargetFamily()).targetId(report.getTargetId()).reason(report.getReason()).status(report.getStatus()).createdAt(report.getCreatedAt()).build(); }
}
