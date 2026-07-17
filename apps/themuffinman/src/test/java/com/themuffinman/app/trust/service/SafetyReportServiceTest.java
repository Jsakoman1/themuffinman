package com.themuffinman.app.trust.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.repository.AppUserRepository;
import com.themuffinman.app.trust.dto.SafetyReportRequestDTO;
import com.themuffinman.app.trust.model.SafetyReport;
import com.themuffinman.app.trust.repository.SafetyReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SafetyReportServiceTest {
    @Mock private SafetyReportRepository repository;
    @Mock private AppUserRepository userRepository;
    @InjectMocks private SafetyReportService service;

    @Test
    void reportIsPrivateAndTargetsKnownUser() {
        AppUser reporter = user(1L); AppUser target = user(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(repository.save(any(SafetyReport.class))).thenAnswer(invocation -> invocation.getArgument(0));
        SafetyReportRequestDTO request = new SafetyReportRequestDTO(); request.setTargetUserId(2L); request.setTargetFamily("USER"); request.setReason("Spam");
        var result = service.create(request, reporter);
        assertEquals("user", result.getTargetFamily()); assertEquals("OPEN", result.getStatus());
    }

    @Test
    void selfReportIsRejected() {
        SafetyReportRequestDTO request = new SafetyReportRequestDTO(); request.setTargetUserId(1L); request.setTargetFamily("user"); request.setReason("x");
        assertThrows(RuntimeException.class, () -> service.create(request, user(1L)));
    }

    private AppUser user(Long id) { AppUser user = new AppUser(); user.setId(id); return user; }
}
