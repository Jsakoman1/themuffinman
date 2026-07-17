package com.themuffinman.app.vision.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.social.dto.CircleGroupResponseDTO;
import com.themuffinman.app.social.dto.CircleMemberDTO;
import com.themuffinman.app.social.service.CircleReadService;
import com.themuffinman.app.vision.dto.VisionCapabilityPreviewDTO;
import com.themuffinman.app.vision.dto.VisionSlotSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
class VisionSocialPreviewRenderer {

    private final CircleReadService circleReadService;

    VisionCapabilityPreviewDTO previewCircles(AppUser currentUser) {
        if (currentUser == null) {
            return null;
        }

        List<CircleGroupResponseDTO> circles = circleReadService.getCircles(currentUser);
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        for (int index = 0; index < Math.min(circles.size(), 4); index++) {
            CircleGroupResponseDTO circle = circles.get(index);
            addItem(items, "circle_" + circle.getId(), circle.getName(),
                    circle.getMemberCount() + " members"
                            + (circle.getMemberPreviewLabel() == null || circle.getMemberPreviewLabel().isBlank()
                            ? ""
                            : " · " + circle.getMemberPreviewLabel()));
        }

        String summary = circles.isEmpty()
                ? "You do not have any circles yet."
                : circles.size() + " circle" + (circles.size() == 1 ? "" : "s") + ".";
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_circles")
                .title("Circles")
                .summary(summary)
                .items(items)
                .tone("info")
                .build();
    }

    VisionCapabilityPreviewDTO previewCircleDetail(AppUser currentUser, Long circleId) {
        if (currentUser == null || circleId == null) {
            return null;
        }

        CircleGroupResponseDTO circle = circleReadService.getCircles(currentUser).stream()
                .filter(candidate -> circleId.equals(candidate.getId()))
                .findFirst()
                .orElse(null);
        if (circle == null) {
            return null;
        }

        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "target_circle_query", "Circle", circle.getName());
        addItem(items, "circle_member_count", "Members", String.valueOf(circle.getMemberCount()));
        List<CircleMemberDTO> members = circle.getMembers() == null ? List.of() : circle.getMembers();
        for (int index = 0; index < Math.min(members.size(), 6); index++) {
            CircleMemberDTO member = members.get(index);
            addItem(items, "circle_member_" + (index + 1), "Member " + (index + 1), member.getUsername());
        }

        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("view_circle_detail")
                .title("Circle")
                .summary("Circle.")
                .items(items)
                .tone("info")
                .build();
    }

    VisionCapabilityPreviewDTO previewAccessibleCircle(AppUser currentUser, Long circleId) {
        if (currentUser == null || circleId == null) return null;
        CircleGroupResponseDTO circle = circleReadService.getAccessibleCircleDetail(circleId, currentUser);
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "accessible_circle_id", "Circle id", String.valueOf(circle.getId()));
        addItem(items, "accessible_circle_name", "Circle", circle.getName());
        addItem(items, "accessible_circle_member_count", "Members", String.valueOf(circle.getMemberCount()));
        List<CircleMemberDTO> members = circle.getMembers() == null ? List.of() : circle.getMembers();
        for (int index = 0; index < Math.min(members.size(), 6); index++) {
            addItem(items, "accessible_circle_member_" + (index + 1), "Member " + (index + 1), members.get(index).getUsername());
        }
        return VisionCapabilityPreviewDTO.builder().capabilityId("view_accessible_circle").title(circle.getName())
                .summary("Accessible circle detail.").items(items).tone("info").build();
    }

    VisionCapabilityPreviewDTO previewCircleDraft(String circleName) {
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "circle_name", "Circle name", circleName);
        long filledFieldCount = VisionCapabilityPreviewSupport.countFilledValues(items);
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("create_circle")
                .title("Circle draft")
                .summary(VisionCapabilityPreviewSupport.draftSummary(
                        filledFieldCount,
                        "Start the circle draft by adding a circle name.",
                        "Review the circle draft so far. Continue adding the remaining fields.",
                        "Review the new circle before confirmation.",
                        1
                ))
                .items(items)
                .tone("info")
                .build();
    }

    VisionCapabilityPreviewDTO previewCreateCircleRequestDraft(String targetUsername) {
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "target_user", "Person", targetUsername);
        long filledFieldCount = VisionCapabilityPreviewSupport.countFilledValues(items);
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("create_circle_request")
                .title("Circle request draft")
                .summary(VisionCapabilityPreviewSupport.draftSummary(
                        filledFieldCount,
                        "Start the circle request by adding the person.",
                        "Review the circle request so far. Continue adding the remaining fields.",
                        "Review the connection invite before confirmation.",
                        1
                ))
                .items(items)
                .tone("info")
                .build();
    }

    VisionCapabilityPreviewDTO previewAcceptCircleRequestDraft(String targetUsername) {
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "target_user", "Person", targetUsername);
        long filledFieldCount = VisionCapabilityPreviewSupport.countFilledValues(items);
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("accept_circle_request")
                .title("Circle request acceptance review")
                .summary(VisionCapabilityPreviewSupport.draftSummary(
                        filledFieldCount,
                        "Start the acceptance review by identifying the person.",
                        "Review the circle request acceptance so far.",
                        "Review the incoming circle request you are about to accept.",
                        1
                ))
                .items(items)
                .tone("info")
                .build();
    }

    VisionCapabilityPreviewDTO previewDeleteCircleRequestDraft(String targetUsername, boolean incoming) {
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "target_user", "Person", targetUsername);
        addItem(items, "circle_request_direction", "Direction", incoming ? "Incoming request" : "Outgoing invite");
        long filledFieldCount = VisionCapabilityPreviewSupport.countFilledValues(items);
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("delete_circle_request")
                .title(incoming ? "Circle request decline review" : "Circle invite cancellation review")
                .summary(VisionCapabilityPreviewSupport.draftSummary(
                        filledFieldCount,
                        incoming
                                ? "Start the decline review by identifying the person."
                                : "Start the cancellation review by identifying the person.",
                        incoming
                                ? "Review the incoming circle request so far."
                                : "Review the outgoing circle invite so far.",
                        incoming
                                ? "Review the incoming circle request you are about to decline."
                                : "Review the outgoing circle invite you are about to cancel.",
                        2
                ))
                .items(items)
                .tone("warning")
                .build();
    }

    VisionCapabilityPreviewDTO previewUpdateCircleDraft(String currentCircleName, String draftCircleName) {
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "target_circle_query", "Circle", currentCircleName);
        addItem(items, "circle_name", "New name", draftCircleName);
        long filledFieldCount = VisionCapabilityPreviewSupport.countFilledValues(items);
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("update_circle")
                .title("Circle update draft")
                .summary(VisionCapabilityPreviewSupport.draftSummary(
                        filledFieldCount,
                        "The current circle is loaded. Add the new circle name before confirmation.",
                        "Review the circle rename so far. Continue adding the remaining fields.",
                        "Review the circle rename before confirmation.",
                        2
                ))
                .items(items)
                .tone("info")
                .build();
    }

    VisionCapabilityPreviewDTO previewDeleteCircleDraft(String currentCircleName, String memberCountLabel) {
        List<VisionSlotSummaryDTO> items = new ArrayList<>();
        addItem(items, "target_circle_query", "Circle", currentCircleName);
        addItem(items, "circle_member_count", "Members", memberCountLabel);
        long filledFieldCount = VisionCapabilityPreviewSupport.countFilledValues(items);
        return VisionCapabilityPreviewDTO.builder()
                .capabilityId("delete_circle")
                .title("Circle deletion review")
                .summary(VisionCapabilityPreviewSupport.draftSummary(
                        filledFieldCount,
                        "Start the deletion review by loading the circle.",
                        "Review the circle deletion so far.",
                        "Review the circle you are about to delete.",
                        2
                ))
                .items(items)
                .tone("warning")
                .build();
    }

    private void addItem(List<VisionSlotSummaryDTO> items, String slotId, String label, String value) {
        VisionCapabilityPreviewSupport.addItem(items, slotId, label, value);
    }
}
