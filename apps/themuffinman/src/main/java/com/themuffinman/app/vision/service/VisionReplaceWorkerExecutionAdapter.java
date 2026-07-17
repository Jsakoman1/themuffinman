package com.themuffinman.app.vision.service;

import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.workmarket.dto.QuestApplicationResponseDTO;
import com.themuffinman.app.workmarket.dto.WorkerReassignmentRequestDTO;
import com.themuffinman.app.workmarket.service.WorkmarketWorkerManagementService;
import org.springframework.stereotype.Service;

@Service
public class VisionReplaceWorkerExecutionAdapter implements VisionCapabilityExecutionAdapter {
    private static final String CAPABILITY_ID = "replace_worker";
    private final WorkmarketWorkerManagementService service;

    public VisionReplaceWorkerExecutionAdapter(WorkmarketWorkerManagementService service) {
        this.service = service;
    }

    @Override
    public String capabilityId() { return CAPABILITY_ID; }

    @Override
    public VisionExecutionResult execute(VisionConversation conversation) {
        if (conversation == null || conversation.getOwner() == null) return VisionExecutionResult.blocked("Conversation owner is required.");
        try {
            Long questId = Long.valueOf(conversation.getSlotData().get("worker_quest_id"));
            Long applicationId = Long.valueOf(conversation.getSlotData().get("worker_application_id"));
            WorkerReassignmentRequestDTO request = new WorkerReassignmentRequestDTO();
            request.setReplacementApplicationId(Long.valueOf(conversation.getSlotData().get("replacement_application_id")));
            QuestApplicationResponseDTO result = service.replaceWorker(questId, applicationId, request, conversation.getOwner());
            return VisionExecutionResult.executedApplication(CAPABILITY_ID, result);
        } catch (NumberFormatException | NullPointerException exception) {
            return VisionExecutionResult.blocked("Quest, worker, and replacement application ids are required.");
        } catch (RuntimeException exception) {
            return VisionExecutionResult.blocked("The worker assignment could not be replaced.");
        }
    }
}
