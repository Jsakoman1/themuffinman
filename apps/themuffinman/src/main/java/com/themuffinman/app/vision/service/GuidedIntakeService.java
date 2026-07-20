package com.themuffinman.app.vision.service;

import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.vision.dto.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.math.BigDecimal;

@Service
public class GuidedIntakeService {
    private record Field(String id, String kind, String label, String placeholder, List<String> choices) {}
    private static final Map<String, List<Field>> FLOWS = Map.of(
            "work.quest.create", List.of(new Field("title", "text", "Title", "What needs doing?", List.of()), new Field("description", "textarea", "Description", "Add the useful detail.", List.of()), new Field("awardAmount", "number", "Award", "0", List.of()), new Field("termFixed", "choice", "Terms", "Choose terms", List.of("Flexible", "Fixed"))),
            "work.application.create", List.of(new Field("message", "textarea", "Message", "Why are you a good fit?", List.of())),
            "business.profile.create", List.of(new Field("businessName", "text", "Business name", "Name your business", List.of()), new Field("headline", "text", "Headline", "What do you offer?", List.of()), new Field("description", "textarea", "Description", "Describe the business", List.of())),
            "things.listing.create", List.of(new Field("title", "text", "Title", "What are you sharing?", List.of()), new Field("description", "textarea", "Description", "Describe the thing", List.of())),
            "rides.offer.create", List.of(new Field("origin", "text", "From", "Starting area", List.of()), new Field("destination", "text", "To", "Destination area", List.of()), new Field("departureTime", "datetime-local", "Departure", "When do you leave?", List.of())),
            "social.circle.create", List.of(new Field("name", "text", "Circle name", "Name this circle", List.of()), new Field("description", "textarea", "Description", "What is this circle for?", List.of())),
            "identity.profile.update", List.of(new Field("description", "textarea", "Profile description", "What should people know?", List.of()), new Field("locationMode", "choice", "Location mode", "Choose visibility", List.of("Off", "Approximate", "Exact")))
    );

    public GuidedIntakeResponseDTO advance(GuidedIntakeRequestDTO request) {
        List<Field> fields = Optional.ofNullable(FLOWS.get(request.getFlow())).orElseThrow(() -> ServiceErrors.badRequest("Unsupported guided intake flow"));
        Map<String, String> draft = new LinkedHashMap<>(Optional.ofNullable(request.getDraft()).orElseGet(Map::of));
        if ("back".equalsIgnoreCase(request.getAction())) {
            int currentIndex = fields.stream().map(Field::id).toList().indexOf(request.getFieldId());
            if (currentIndex > 0) draft.remove(fields.get(currentIndex - 1).id());
        } else if (request.getFieldId() != null && request.getFieldValue() != null) {
            Field field = fields.stream().filter(item -> item.id().equals(request.getFieldId())).findFirst().orElseThrow(() -> ServiceErrors.badRequest("Unknown guided intake field"));
            String value = request.getFieldValue().trim();
            if (value.isBlank()) return response(request.getFlow(), fields, draft, field, false, "This field is required.");
            if ("number".equals(field.kind()) && parseNumber(value) == null) return response(request.getFlow(), fields, draft, field, false, "Enter a valid number.");
            if (!field.choices().isEmpty() && !field.choices().stream().anyMatch(choice -> choice.equalsIgnoreCase(value))) return response(request.getFlow(), fields, draft, field, false, "Choose one of the available options.");
            draft.put(field.id(), value);
        }
        Field next = fields.stream().filter(field -> !draft.containsKey(field.id())).findFirst().orElse(null);
        return GuidedIntakeResponseDTO.builder().flow(request.getFlow()).draft(draft).reviewReady(next == null).step(next == null ? GuidedIntakeStepDTO.builder().complete(true).valid(true).nextAction("review").build() : step(next, draft.get(next.id()), true, null)).build();
    }

    private GuidedIntakeResponseDTO response(String flow, List<Field> fields, Map<String, String> draft, Field field, boolean valid, String error) { return GuidedIntakeResponseDTO.builder().flow(flow).draft(draft).reviewReady(false).step(step(field, draft.get(field.id()), valid, error)).build(); }
    private GuidedIntakeStepDTO step(Field field, String value, boolean valid, String error) { return GuidedIntakeStepDTO.builder().fieldId(field.id()).inputKind(field.kind()).label(field.label()).placeholder(field.placeholder()).choices(field.choices()).currentValue(value).valid(valid).error(error).nextAction("next").complete(false).build(); }
    private BigDecimal parseNumber(String value) { try { return new BigDecimal(value); } catch (NumberFormatException ignored) { return null; } }
}
