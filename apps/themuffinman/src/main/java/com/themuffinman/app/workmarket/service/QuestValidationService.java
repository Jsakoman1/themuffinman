package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.common.concepts.CircleVisibilitySelection;
import com.themuffinman.app.common.concepts.SchedulingWindow;
import com.themuffinman.app.workmarket.dto.QuestRequestDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.location.model.QuestLocationSource;
import com.themuffinman.app.social.model.CircleGroup;
import com.themuffinman.app.common.validation.RichTextInputValidator;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.workmarket.model.Quest;
import com.themuffinman.app.workmarket.model.QuestAudience;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuestValidationService {
    private static final int QUEST_IMAGE_MAX_LENGTH = 350000;

    private final QuestVisibilityService questVisibilityService;

    public QuestValidationService(QuestVisibilityService questVisibilityService) {
        this.questVisibilityService = questVisibilityService;
    }

    public void validateCreateRequest(QuestRequestDTO dto) {
        validateQuestBasics(dto);
        validateQuestCreationTermInput(dto.getScheduledAt(), dto.getEndsAt(), dto.getTermFixed());
        validateAssigneeTarget(dto.getAssigneeTarget());
        validateQuestImages(dto.getImages());
        validateQuestLocation(dto);
    }

    public void validateUpdateRequest(QuestRequestDTO dto) {
        validateQuestBasics(dto);
        validateAssigneeTarget(dto.getAssigneeTarget());
        validateQuestImages(dto.getImages());
        validateQuestLocation(dto);
    }

    public void validateQuestBasics(QuestRequestDTO dto) {
        if (dto == null) {
            throw ServiceErrors.badRequest("Quest request is required");
        }

        validateQuestTitle(dto.getTitle());
        validateQuestDescription(dto.getDescription());
        validateQuestAwardAmount(dto.getAwardAmount());
    }

    public void validateQuestCreationTermInput(Instant scheduledAt, Instant endsAt, Boolean termFixed) {
        if (termFixed == null) {
            throw ServiceErrors.badRequest("Term fixed flag is required");
        }

        if (Boolean.TRUE.equals(termFixed) && scheduledAt == null) {
            throw ServiceErrors.badRequest("Scheduled time is required when the term is fixed");
        }

        validateTermRange(scheduledAt, endsAt);
    }

    public void validateTermInput(Instant scheduledAt, Instant endsAt, Boolean termFixed) {
        if ((scheduledAt != null || endsAt != null) && termFixed == null) {
            throw ServiceErrors.badRequest("Term fixed flag is required when providing a time");
        }

        if (Boolean.TRUE.equals(termFixed) && scheduledAt == null) {
            throw ServiceErrors.badRequest("Scheduled time is required when the term is fixed");
        }

        if (endsAt != null && scheduledAt == null) {
            throw ServiceErrors.badRequest("Start time is required when providing an end time");
        }

        validateTermRange(scheduledAt, endsAt);
    }

    public void applyQuestVisibilityCircles(Quest quest, QuestAudience audience, List<Long> selectedCircleIds, AppUser owner) {
        if (audience != QuestAudience.CIRCLES) {
            quest.getVisibleToCircles().clear();
            return;
        }

        CircleVisibilitySelection selection = CircleVisibilitySelection.from(selectedCircleIds);
        if (selection.unrestricted() && selectedCircleIds == null) {
            return;
        }

        List<CircleGroup> selectedCircles = questVisibilityService.getVisibleCircles(owner, List.copyOf(selection.asDistinctSet()));
        if (selection.distinctCount() != selectedCircles.size()) {
            throw ServiceErrors.badRequest("One or more selected circles are invalid");
        }

        quest.getVisibleToCircles().clear();
        quest.getVisibleToCircles().addAll(selectedCircles);
    }

    public Integer normalizeAssigneeTarget(Integer assigneeTarget) {
        return assigneeTarget == null ? 1 : assigneeTarget;
    }

    public String normalizeQuestText(String value) {
        if (value == null) {
            return null;
        }

        return value.trim();
    }

    public String normalizeQuestRichText(String value) {
        return RichTextInputValidator.sanitize(value);
    }

    public List<String> copyImages(List<String> images) {
        return images == null ? null : new ArrayList<>(images);
    }

    private void validateQuestImages(List<String> images) {
        if (images == null) {
            return;
        }

        if (images.size() > 10) {
            throw ServiceErrors.badRequest("A quest can have at most 10 images");
        }

        for (String image : images) {
            if (image == null || image.isBlank()) {
                throw ServiceErrors.badRequest("Quest images must not be empty");
            }
            if (!image.startsWith("data:image/")) {
                throw ServiceErrors.badRequest("Quest images must be image data URLs");
            }
            if (image.length() > QUEST_IMAGE_MAX_LENGTH) {
                throw ServiceErrors.badRequest("Quest images must be 350000 characters or less");
            }
        }
    }

    private void validateQuestDescription(String description) {
        if (!RichTextInputValidator.hasContent(description)) {
            throw ServiceErrors.badRequest("Quest description is required");
        }
    }

    private void validateQuestTitle(String title) {
        String normalizedTitle = normalizeQuestText(title);
        if (normalizedTitle == null || normalizedTitle.isBlank()) {
            throw ServiceErrors.badRequest("Quest title is required");
        }

        if (normalizedTitle.length() > 255) {
            throw ServiceErrors.badRequest("Quest title must be 255 characters or less");
        }
    }

    private void validateQuestAwardAmount(BigDecimal awardAmount) {
        if (awardAmount == null) {
            throw ServiceErrors.badRequest("Award amount is required");
        }

        if (awardAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw ServiceErrors.badRequest("Award amount cannot be negative");
        }
    }

    private void validateAssigneeTarget(Integer assigneeTarget) {
        if (assigneeTarget != null && assigneeTarget < 1) {
            throw ServiceErrors.badRequest("Assignee target must be at least 1 when provided");
        }
    }

    private void validateQuestLocation(QuestRequestDTO dto) {
        QuestLocationSource source = dto.getLocationSource() == null ? QuestLocationSource.PROFILE : dto.getLocationSource();
        if (source != QuestLocationSource.CUSTOM) {
            return;
        }

        boolean hasAddressData = normalizeQuestText(dto.getLocationStreet()) != null
                || normalizeQuestText(dto.getLocationHouseNumber()) != null
                || normalizeQuestText(dto.getLocationPostalCode()) != null
                || normalizeQuestText(dto.getLocationLocality()) != null
                || normalizeQuestText(dto.getLocationCountry()) != null
                || normalizeQuestText(dto.getLocationLabel()) != null;
        if (!hasAddressData) {
            throw ServiceErrors.badRequest("Enter a custom quest location before saving.");
        }
    }

    private void validateTermRange(Instant scheduledAt, Instant endsAt) {
        SchedulingWindow window = new SchedulingWindow(scheduledAt, endsAt);
        if (window.startsBefore(Instant.now())) {
            throw ServiceErrors.badRequest("Start time cannot be in the past");
        }

        if (window.hasInvalidRange()) {
            throw ServiceErrors.badRequest("End time must be after the start time");
        }
    }
}
