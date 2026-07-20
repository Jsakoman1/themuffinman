package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.dto.GuidedIntakeRequestDTO;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GuidedIntakeServiceTest {
    private final GuidedIntakeService service = new GuidedIntakeService();

    @Test
    void exposesOneBackendOwnedStepAndReviewBoundary() {
        GuidedIntakeRequestDTO start = request(Map.of());
        var first = service.advance(start);

        assertThat(first.getStep().getFieldId()).isEqualTo("title");
        assertThat(first.isReviewReady()).isFalse();

        start.setFieldId("title");
        start.setFieldValue("Mow the lawn");
        var second = service.advance(start);

        assertThat(second.getStep().getFieldId()).isEqualTo("description");
        assertThat(second.getDraft()).containsEntry("title", "Mow the lawn");
    }

    @Test
    void rejectsInvalidValuesWithoutAdvancing() {
        GuidedIntakeRequestDTO request = request(Map.of());
        request.setFieldId("title");
        request.setFieldValue(" ");

        var response = service.advance(request);

        assertThat(response.getStep().getFieldId()).isEqualTo("title");
        assertThat(response.getStep().isValid()).isFalse();
        assertThat(response.getStep().getError()).isNotBlank();
        assertThat(response.getDraft()).isEmpty();
    }

    @Test
    void backReturnsToPreviousBackendStep() {
        GuidedIntakeRequestDTO request = request(Map.of("title", "Mow the lawn"));
        request.setFieldId("description");
        request.setAction("back");

        var response = service.advance(request);

        assertThat(response.getStep().getFieldId()).isEqualTo("title");
        assertThat(response.getDraft()).doesNotContainKey("title");
    }

    private GuidedIntakeRequestDTO request(Map<String, String> draft) {
        GuidedIntakeRequestDTO request = new GuidedIntakeRequestDTO();
        request.setFlow("work.quest.create");
        request.setDraft(draft);
        return request;
    }
}
