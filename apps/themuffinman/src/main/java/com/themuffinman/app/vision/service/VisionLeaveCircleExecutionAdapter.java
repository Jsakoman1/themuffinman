package com.themuffinman.app.vision.service;
import com.themuffinman.app.social.service.CircleMembershipService;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;
@Service public class VisionLeaveCircleExecutionAdapter implements VisionCapabilityExecutionAdapter {
 private final CircleMembershipService service; public VisionLeaveCircleExecutionAdapter(CircleMembershipService s){service=s;}
 public String capabilityId(){return "leave_circle";} public VisionExecutionResult execute(VisionConversation c){try{service.leaveCircle(Long.parseLong(c.getSlotData().get("circle_id")),c.getOwner());return VisionExecutionResult.executedAction(capabilityId());}catch(RuntimeException e){return VisionExecutionResult.blocked("The circle membership could not be removed.");}}
}
