package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessOfferingRequestDTO;
import com.themuffinman.app.business.dto.BusinessOfferingSetupDTO;
import com.themuffinman.app.business.dto.BusinessOfferingSetupFieldDTO;
import com.themuffinman.app.business.dto.BusinessOfferingSetupOptionDTO;
import com.themuffinman.app.business.dto.BusinessOfferingSetupStepDTO;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessOfferingSetupService {
    private final BusinessProfileRepository businessProfileRepository;

    public BusinessOfferingSetupDTO getSetup(AppUser currentUser, Long businessProfileId) {
        if (currentUser == null || businessProfileId == null || businessProfileRepository.findById(businessProfileId)
                .filter(profile -> profile.getOwner().getId().equals(currentUser.getId())).isEmpty()) {
            throw ServiceErrors.notFound("Business profile not found");
        }
        return BusinessOfferingSetupDTO.builder()
                .contractVersion("business-offering-setup-v1")
                .defaults(BusinessOfferingRequestDTO.builder().title("").slug("").summary("").description("")
                        .pricingType(com.themuffinman.app.business.model.BusinessOfferingPricingType.FIXED).basePriceAmount(BigDecimal.ZERO).basePriceCurrency("CHF")
                        .durationMode(com.themuffinman.app.business.model.BusinessOfferingDurationMode.FIXED).defaultDurationMinutes(60).minDurationMinutes(60).maxDurationMinutes(60)
                        .capacityMode(com.themuffinman.app.business.model.BusinessOfferingCapacityMode.SINGLE).slotCapacity(1)
                        .bookingMode(com.themuffinman.app.business.model.BusinessOfferingBookingMode.REQUEST).fulfillmentMode(com.themuffinman.app.business.model.BusinessOfferingFulfillmentMode.EXACT_APPOINTMENT)
                        .durationIncrementMinutes(30).minimumQuantity(1).maximumQuantity(1).requiresOwnerConfirmation(true).bufferBeforeMinutes(0).bufferAfterMinutes(0).active(true).sortOrder(0).build())
                .pricingTypes(options(new String[][]{{"FIXED", "One fixed price", "Customers see one clear price."}, {"FROM", "Starting price", "Show a starting price before review."}, {"CUSTOM_QUOTE", "Confirm price after review", "Agree the price before confirming."}, {"FREE", "Free", "No charge for this service."}}))
                .bookingModes(options(new String[][]{{"REQUEST", "I confirm each request", "You review each booking first."}, {"INSTANT", "Confirm automatically", "A free slot is booked immediately."}}))
                .fulfillmentModes(options(new String[][]{{"EXACT_APPOINTMENT", "Appointment at a specific time", "Best for most services."}, {"FIELD_SERVICE", "Service at the customer's place", "For travel-based work."}, {"ALL_DAY_STAY", "All-day or overnight stay", "For stays or whole-day work."}, {"CAPACITY_WINDOW", "Shared time window", "For group appointments."}, {"RESOURCE_ASSIGNMENT", "Requires a specific resource", "For staff, rooms, or equipment."}}))
                .durationOptions(options(new String[][]{{"15", "15 minutes", ""}, {"30", "30 minutes", ""}, {"45", "45 minutes", ""}, {"60", "1 hour", ""}, {"90", "1½ hours", ""}, {"120", "2 hours", ""}}))
                .steps(List.of(step("basics", "Service basics", "What customers are booking."), step("time", "Time and capacity", "How appointments fit into your schedule."), step("questions", "Customer questions", "Only information you need before booking."), step("pricing", "Price rules", "Predictable extra charges only."), step("resources", "Resources", "Optional staff, room, or equipment setup."), step("preview", "Customer preview", "The booking sequence customers will see.")))
                .fields(List.of(field("title", "Service name", "A clear customer-facing name.", true, null, 120), field("summary", "Short description", "One sentence customers see before booking.", true, null, 240), field("defaultDurationMinutes", "Appointment length", "Use a simple standard duration first.", true, 1, 1440), field("basePriceAmount", "Price", "Required unless the service is free or quoted.", false, 0, null)))
                .readyForBookings(false)
                .readinessMessage("Add a service and working hours before accepting bookings.")
                .build();
    }

    private List<BusinessOfferingSetupOptionDTO> options(String[][] values) { return java.util.Arrays.stream(values).map(value -> BusinessOfferingSetupOptionDTO.builder().value(value[0]).label(value[1]).helpText(value[2]).build()).toList(); }
    private BusinessOfferingSetupStepDTO step(String id, String title, String description) { return BusinessOfferingSetupStepDTO.builder().id(id).title(title).description(description).build(); }
    private BusinessOfferingSetupFieldDTO field(String name, String label, String helpText, boolean required, Integer minimum, Integer maximum) { return BusinessOfferingSetupFieldDTO.builder().name(name).label(label).helpText(helpText).required(required).minimum(minimum).maximum(maximum).build(); }
}
