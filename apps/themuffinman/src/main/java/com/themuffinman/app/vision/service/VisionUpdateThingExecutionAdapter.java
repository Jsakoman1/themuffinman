package com.themuffinman.app.vision.service;
import com.themuffinman.app.things.dto.ThingListingRequestDTO;
import com.themuffinman.app.things.service.ThingSharingService;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;
@Service public class VisionUpdateThingExecutionAdapter implements VisionCapabilityExecutionAdapter {
 private final ThingSharingService service; public VisionUpdateThingExecutionAdapter(ThingSharingService s){service=s;}
 public String capabilityId(){return "update_thing";} public VisionExecutionResult execute(VisionConversation c){try{service.updateMyListingForVision(Long.parseLong(c.getSlotData().get("thing_listing_id")),ThingListingRequestDTO.builder().title(c.getSlotData().get("thing_title")).description(c.getSlotData().get("thing_description")).conditionNote(c.getSlotData().get("thing_condition")).build(),c.getOwner());return VisionExecutionResult.executedAction(capabilityId());}catch(RuntimeException e){return VisionExecutionResult.blocked("The thing listing could not be updated.");}}
}
