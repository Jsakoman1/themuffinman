package com.themuffinman.app.vision.service;
import com.themuffinman.app.things.service.ThingSharingService;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;
@Service public class VisionArchiveThingExecutionAdapter implements VisionCapabilityExecutionAdapter {
 private final ThingSharingService service; public VisionArchiveThingExecutionAdapter(ThingSharingService s){service=s;}
 public String capabilityId(){return "archive_thing";} public VisionExecutionResult execute(VisionConversation c){try{service.archiveMyListingForVision(Long.parseLong(c.getSlotData().get("thing_listing_id")),c.getOwner());return VisionExecutionResult.executedAction(capabilityId());}catch(RuntimeException e){return VisionExecutionResult.blocked("The thing listing could not be archived.");}}
}
