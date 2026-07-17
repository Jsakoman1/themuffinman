package com.themuffinman.app.vision.service;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;
@Service public class VisionCreateAvailabilityRuleExecutionAdapter implements VisionCapabilityExecutionAdapter {
 private final VisionBusinessAvailabilityMutationService service; public VisionCreateAvailabilityRuleExecutionAdapter(VisionBusinessAvailabilityMutationService s){service=s;}
 public String capabilityId(){return "create_availability_rule";} public VisionExecutionResult execute(VisionConversation c){try{service.createRule(c.getSlotData(),c.getOwner());return VisionExecutionResult.executedAction(capabilityId());}catch(RuntimeException e){return VisionExecutionResult.blocked("The availability rule could not be created.");}}
}
