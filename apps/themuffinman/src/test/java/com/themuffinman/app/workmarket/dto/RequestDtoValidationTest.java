package com.themuffinman.app.workmarket.dto;

import com.themuffinman.app.identity.dto.AppUserRequestDTO;
import com.themuffinman.app.social.dto.CircleGroupRequestDTO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestDtoValidationTest {
    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @AfterAll
    static void tearDown() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    void questRequestRejectsInvalidAmountAndOversizedText() {
        QuestRequestDTO request = QuestRequestDTO.builder()
                .title("x".repeat(256))
                .description("x".repeat(2001))
                .awardAmount(BigDecimal.ZERO)
                .assigneeTarget(0)
                .images(List.of("https://example.com/image.png"))
                .build();

        assertFalse(VALIDATOR.validate(request).isEmpty());
    }

    @Test
    void applicationRequestRejectsMissingPriceAndOversizedMessage() {
        QuestApplicationRequestDTO request = QuestApplicationRequestDTO.builder()
                .message("x".repeat(2001))
                .build();

        assertFalse(VALIDATOR.validate(request).isEmpty());
    }

    @Test
    void requestValidationUsesBackendFriendlyMessages() {
        QuestRequestDTO questRequest = QuestRequestDTO.builder()
                .title("")
                .description("")
                .awardAmount(null)
                .build();
        QuestApplicationRequestDTO applicationRequest = QuestApplicationRequestDTO.builder()
                .message("")
                .proposedPrice(null)
                .build();
        CircleGroupRequestDTO circleRequest = CircleGroupRequestDTO.builder()
                .name("")
                .build();

        assertEquals("Quest title is required", VALIDATOR.validate(questRequest).stream()
                .filter(violation -> "title".equals(violation.getPropertyPath().toString()))
                .findFirst()
                .orElseThrow()
                .getMessage());
        assertEquals("Application message is required", VALIDATOR.validate(applicationRequest).stream()
                .filter(violation -> "message".equals(violation.getPropertyPath().toString()))
                .findFirst()
                .orElseThrow()
                .getMessage());
        assertEquals("Circle name is required", VALIDATOR.validate(circleRequest).stream()
                .filter(violation -> "name".equals(violation.getPropertyPath().toString()))
                .findFirst()
                .orElseThrow()
                .getMessage());
    }

    @Test
    void appUserRequestRejectsInvalidProfileFields() {
        AppUserRequestDTO request = AppUserRequestDTO.builder()
                .email("invalid-email")
                .username("ab")
                .password("short")
                .profileAvatarDataUrl("https://example.com/avatar.png")
                .build();

        assertFalse(VALIDATOR.validate(request).isEmpty());
    }

    @Test
    void questRequestAcceptsValidPayload() {
        QuestRequestDTO request = QuestRequestDTO.builder()
                .title("Fix garden fence")
                .description("Need help with a small repair")
                .awardAmount(BigDecimal.valueOf(45))
                .assigneeTarget(2)
                .images(List.of("data:image/jpeg;base64,abc"))
                .build();

        assertTrue(VALIDATOR.validate(request).isEmpty());
    }
}
