package com.themuffinman.app.chat.dto;

import com.themuffinman.app.common.contract.ContractOptional;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequestDTO {

    public static final int CHAT_IMAGE_MAX_LENGTH = 350000;

    @ContractOptional
    @Size(max = 2000, message = "Chat message must be 2000 characters or less")
    private String messageBody;

    @ContractOptional
    @Size(max = CHAT_IMAGE_MAX_LENGTH, message = "Chat image must be 350000 characters or less")
    @Pattern(regexp = "^data:image/.*", message = "Chat image must be an image data URL")
    private String imageDataUrl;
}
