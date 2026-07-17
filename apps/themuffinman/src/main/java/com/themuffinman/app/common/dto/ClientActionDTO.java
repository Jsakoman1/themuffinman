package com.themuffinman.app.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientActionDTO {
    private String id;
    private String label;
    private ClientActionToneDTO tone;
    private boolean enabled;
    private boolean requiresConfirmation;
    @Nullable
    private String confirmationTitle;
    @Nullable
    private String confirmationMessage;
    @Nullable
    private String disabledReason;
}
