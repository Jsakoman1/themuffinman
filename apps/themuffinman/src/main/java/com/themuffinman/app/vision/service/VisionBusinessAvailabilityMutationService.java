package com.themuffinman.app.vision.service;

import com.themuffinman.app.business.dto.BusinessAvailabilityExceptionRequestDTO;
import com.themuffinman.app.business.dto.BusinessAvailabilityRuleRequestDTO;
import com.themuffinman.app.business.model.BusinessAvailabilityExceptionType;
import com.themuffinman.app.business.service.BusinessAvailabilityService;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class VisionBusinessAvailabilityMutationService {
    private final BusinessAvailabilityService service;

    public void createRule(java.util.Map<String, String> slots, AppUser owner) {
        service.createMyRule(rule(slots), owner);
    }
    public void updateRule(java.util.Map<String, String> slots, AppUser owner) {
        service.updateMyRule(id(slots, "availability_rule_id"), rule(slots), owner);
    }
    public void deleteRule(java.util.Map<String, String> slots, AppUser owner) {
        service.deleteMyRule(id(slots, "availability_rule_id"), owner);
    }
    public void createException(java.util.Map<String, String> slots, AppUser owner) {
        service.createMyException(exception(slots), owner);
    }
    public void updateException(java.util.Map<String, String> slots, AppUser owner) {
        service.updateMyException(id(slots, "availability_exception_id"), exception(slots), owner);
    }
    public void deleteException(java.util.Map<String, String> slots, AppUser owner) {
        service.deleteMyException(id(slots, "availability_exception_id"), owner);
    }

    private BusinessAvailabilityRuleRequestDTO rule(java.util.Map<String, String> s) {
        try {
            return BusinessAvailabilityRuleRequestDTO.builder()
                    .businessOfferingId(optionalLong(s.get("business_offering_id")))
                    .dayOfWeek(Integer.parseInt(required(s, "availability_day_of_week")))
                    .startTimeLocal(LocalTime.parse(required(s, "availability_start_time")))
                    .endTimeLocal(LocalTime.parse(required(s, "availability_end_time")))
                    .slotGranularityMinutes(Integer.parseInt(required(s, "availability_slot_minutes")))
                    .capacityOverride(optionalInt(s.get("availability_capacity")))
                    .active(true).build();
        } catch (RuntimeException e) { throw ServiceErrors.badRequest("Availability rule fields are invalid"); }
    }

    private BusinessAvailabilityExceptionRequestDTO exception(java.util.Map<String, String> s) {
        try {
            return BusinessAvailabilityExceptionRequestDTO.builder()
                    .businessOfferingId(optionalLong(s.get("business_offering_id")))
                    .exceptionType(BusinessAvailabilityExceptionType.valueOf(required(s, "availability_exception_type").toUpperCase()))
                    .startAt(Instant.parse(required(s, "availability_exception_start")))
                    .endAt(Instant.parse(required(s, "availability_exception_end")))
                    .reason(s.get("availability_exception_reason")).build();
        } catch (RuntimeException e) { throw ServiceErrors.badRequest("Availability exception fields are invalid"); }
    }
    private Long id(java.util.Map<String, String> s, String key) { return Long.valueOf(required(s, key)); }
    private String required(java.util.Map<String, String> s, String key) { String value=s.get(key); if(value==null||value.isBlank()) throw ServiceErrors.badRequest("Missing " + key); return value; }
    private Long optionalLong(String value) { return value == null || value.isBlank() ? null : Long.valueOf(value); }
    private Integer optionalInt(String value) { return value == null || value.isBlank() ? null : Integer.valueOf(value); }
}
