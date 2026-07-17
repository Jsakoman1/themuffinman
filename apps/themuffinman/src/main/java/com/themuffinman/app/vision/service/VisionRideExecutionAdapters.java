package com.themuffinman.app.vision.service;

import com.themuffinman.app.rides.dto.RideOfferRequestDTO;
import com.themuffinman.app.rides.service.RideOfferService;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.stereotype.Service;
import java.time.Instant;

abstract class VisionRideAdapter implements VisionCapabilityExecutionAdapter {
    protected final RideOfferService rides;
    protected VisionRideAdapter(RideOfferService rides) { this.rides = rides; }
    protected Long id(VisionConversation c) { return Long.valueOf(c.getSlotData().get("ride_id")); }
    protected VisionExecutionResult action(VisionConversation c, java.util.function.Function<Long, ?> operation) {
        if (c == null || c.getOwner() == null) return VisionExecutionResult.blocked("Conversation owner is required for ride execution.");
        try { operation.apply(id(c)); return VisionExecutionResult.executedAction(capabilityId()); }
        catch (RuntimeException ex) { return VisionExecutionResult.blocked("The ride action could not be completed."); }
    }
}

@Service
class VisionCreateRideExecutionAdapter extends VisionRideAdapter {
    VisionCreateRideExecutionAdapter(RideOfferService rides) { super(rides); }
    public String capabilityId() { return "create_ride"; }
    public VisionExecutionResult execute(VisionConversation c) {
        if (c == null || c.getOwner() == null) return VisionExecutionResult.blocked("Conversation owner is required to offer a ride.");
        try {
            RideOfferRequestDTO request = RideOfferRequestDTO.builder()
                    .origin(c.getSlotData().get("ride_origin"))
                    .destination(c.getSlotData().get("ride_destination"))
                    .departureAt(Instant.parse(c.getSlotData().get("ride_departure_at")))
                    .seats(Integer.valueOf(c.getSlotData().getOrDefault("ride_seats", "1")))
                    .active(true).visibleCircleIds(java.util.List.of()).build();
            rides.createOffer(request, c.getOwner()); return VisionExecutionResult.executedAction(capabilityId());
        } catch (RuntimeException ex) { return VisionExecutionResult.blocked("The ride offer could not be created. Check the future time and route."); }
    }
}

@Service class VisionJoinRideExecutionAdapter extends VisionRideAdapter { VisionJoinRideExecutionAdapter(RideOfferService r){super(r);} public String capabilityId(){return "join_ride";} public VisionExecutionResult execute(VisionConversation c){return action(c,id->rides.join(id,c.getOwner()));} }
@Service class VisionUpdateRideExecutionAdapter extends VisionRideAdapter { VisionUpdateRideExecutionAdapter(RideOfferService r){super(r);} public String capabilityId(){return "update_ride";} public VisionExecutionResult execute(VisionConversation c){return action(c,id->rides.updateFromVision(id,c.getSlotData().get("ride_origin"),c.getSlotData().get("ride_destination"),c.getSlotData().get("ride_departure_at"),c.getSlotData().get("ride_seats"),c.getOwner()));} }
@Service class VisionLeaveRideExecutionAdapter extends VisionRideAdapter { VisionLeaveRideExecutionAdapter(RideOfferService r){super(r);} public String capabilityId(){return "leave_ride";} public VisionExecutionResult execute(VisionConversation c){return action(c,id->rides.leave(id,c.getOwner()));} }
@Service class VisionCancelRideExecutionAdapter extends VisionRideAdapter { VisionCancelRideExecutionAdapter(RideOfferService r){super(r);} public String capabilityId(){return "cancel_ride";} public VisionExecutionResult execute(VisionConversation c){return action(c,id->rides.cancel(id,c.getOwner()));} }
@Service class VisionStartRideExecutionAdapter extends VisionRideAdapter { VisionStartRideExecutionAdapter(RideOfferService r){super(r);} public String capabilityId(){return "start_ride";} public VisionExecutionResult execute(VisionConversation c){return action(c,id->rides.start(id,c.getOwner()));} }
@Service class VisionCompleteRideExecutionAdapter extends VisionRideAdapter { VisionCompleteRideExecutionAdapter(RideOfferService r){super(r);} public String capabilityId(){return "complete_ride";} public VisionExecutionResult execute(VisionConversation c){return action(c,id->rides.complete(id,c.getOwner()));} }
