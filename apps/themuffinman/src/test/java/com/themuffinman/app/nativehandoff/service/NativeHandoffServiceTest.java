package com.themuffinman.app.nativehandoff.service;

import com.themuffinman.app.config.NativeHandoffProperties;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.nativehandoff.dto.NativeHandoffConsumeRequestDTO;
import com.themuffinman.app.nativehandoff.dto.NativeHandoffIssueRequestDTO;
import com.themuffinman.app.nativehandoff.model.NativeHandoffToken;
import com.themuffinman.app.nativehandoff.repository.NativeHandoffTokenRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NativeHandoffServiceTest {
    @Test
    void issuedTokenCanBeConsumedOnceForBoundDevice() {
        NativeHandoffTokenRepository repository = mock(NativeHandoffTokenRepository.class);
        NativeHandoffProperties properties = new NativeHandoffProperties();
        properties.setTtlSeconds(300);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        NativeHandoffService service = new NativeHandoffService(repository, properties);
        AppUser user = user(7L);
        NativeHandoffIssueRequestDTO issue = new NativeHandoffIssueRequestDTO();
        issue.setTargetDevice("mobile");
        issue.setIntent("OPEN_QUEST");
        issue.setResourceReference("quest:42");
        issue.setRedactedContext("{\"allowedActions\":[\"VIEW\"]}");

        var issued = service.issue(issue, user);
        NativeHandoffToken stored = new NativeHandoffToken();
        stored.setUser(user);
        stored.setTargetDevice("mobile");
        stored.setIntent("OPEN_QUEST");
        stored.setExpiresAt(Instant.now().plusSeconds(60));
        when(repository.findByTokenHash(any())).thenReturn(Optional.of(stored));
        NativeHandoffConsumeRequestDTO consume = new NativeHandoffConsumeRequestDTO();
        consume.setToken(issued.getToken());
        consume.setTargetDevice("mobile");

        assertEquals("OPEN_QUEST", service.consume(consume, user).getIntent());
        assertThrows(RuntimeException.class, () -> service.consume(consume, user));
    }

    @Test
    void expiredTokenIsRejected() {
        NativeHandoffTokenRepository repository = mock(NativeHandoffTokenRepository.class);
        NativeHandoffProperties properties = new NativeHandoffProperties();
        NativeHandoffService service = new NativeHandoffService(repository, properties);
        AppUser user = user(7L);
        NativeHandoffToken stored = new NativeHandoffToken();
        stored.setUser(user);
        stored.setTargetDevice("watch");
        stored.setExpiresAt(Instant.now().minusSeconds(1));
        when(repository.findByTokenHash(any())).thenReturn(Optional.of(stored));
        NativeHandoffConsumeRequestDTO consume = new NativeHandoffConsumeRequestDTO();
        consume.setToken("token");
        consume.setTargetDevice("watch");

        assertThrows(RuntimeException.class, () -> service.consume(consume, user));
    }

    private AppUser user(Long id) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername("user");
        return user;
    }
}
