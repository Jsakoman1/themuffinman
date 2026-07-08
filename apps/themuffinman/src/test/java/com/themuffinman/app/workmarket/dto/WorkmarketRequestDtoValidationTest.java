package com.themuffinman.app.workmarket.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkmarketRequestDtoValidationTest {

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
                .awardAmount(BigDecimal.valueOf(-1))
                .assigneeTarget(0)
                .images(List.of("https://example.com/image.png"))
                .build();

        assertFalse(VALIDATOR.validate(request).isEmpty());
    }

    @Test
    void applicationRequestRejectsOversizedMessage() {
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
    }

    @Test
    void adminApplicationUpdateRequestAcceptsOptionalPatchFields() {
        AdminQuestApplicationUpdateRequestDTO request = AdminQuestApplicationUpdateRequestDTO.builder().build();

        assertTrue(VALIDATOR.validate(request).isEmpty());
    }

    @Test
    void userReviewRequestCarriesExpectedFields() {
        UserReviewRequestDTO request = new UserReviewRequestDTO();
        request.setReviewedUserId(42L);
        request.setStars(5);
        request.setComment("Reliable worker");

        assertEquals(42L, request.getReviewedUserId());
        assertEquals(5, request.getStars());
        assertEquals("Reliable worker", request.getComment());
    }
}
