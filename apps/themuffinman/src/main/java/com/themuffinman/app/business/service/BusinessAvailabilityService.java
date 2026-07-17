package com.themuffinman.app.business.service;

import com.themuffinman.app.business.dto.BusinessAvailabilityExceptionListResponseDTO;
import com.themuffinman.app.business.dto.BusinessAvailabilityExceptionRequestDTO;
import com.themuffinman.app.business.dto.BusinessAvailabilityExceptionResponseDTO;
import com.themuffinman.app.business.dto.BusinessAvailabilityRuleListResponseDTO;
import com.themuffinman.app.business.dto.BusinessAvailabilityRuleRequestDTO;
import com.themuffinman.app.business.dto.BusinessAvailabilityRuleResponseDTO;
import com.themuffinman.app.business.mapper.BusinessAvailabilityMgr;
import com.themuffinman.app.business.model.BusinessAvailabilityException;
import com.themuffinman.app.business.model.BusinessAvailabilityRule;
import com.themuffinman.app.business.model.BusinessOffering;
import com.themuffinman.app.business.model.BusinessProfile;
import com.themuffinman.app.business.repository.BusinessAvailabilityExceptionRepository;
import com.themuffinman.app.business.repository.BusinessAvailabilityRuleRepository;
import com.themuffinman.app.business.repository.BusinessOfferingRepository;
import com.themuffinman.app.business.repository.BusinessProfileRepository;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessAvailabilityService {

    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessOfferingRepository businessOfferingRepository;
    private final BusinessAvailabilityRuleRepository businessAvailabilityRuleRepository;
    private final BusinessAvailabilityExceptionRepository businessAvailabilityExceptionRepository;
    private final BusinessAvailabilityValidationService businessAvailabilityValidationService;
    private final BusinessAvailabilityMgr businessAvailabilityMgr;

    public BusinessAvailabilityRuleListResponseDTO getMyRules(AppUser currentUser) {
        return getMyRules(currentUser, null);
    }

    public BusinessAvailabilityRuleListResponseDTO getMyRules(AppUser currentUser, Long businessProfileId) {
        BusinessProfile profile = businessProfileId == null ? null : requireOwnerProfile(currentUser, businessProfileId);
        return BusinessAvailabilityRuleListResponseDTO.builder()
                .items((profile == null ? businessAvailabilityRuleRepository.findByOwnerId(currentUser.getId()) : businessAvailabilityRuleRepository.findByBusinessProfileId(profile.getId(), currentUser.getId())).stream()
                        .map(businessAvailabilityMgr::toDto)
                        .toList())
                .build();
    }

    public BusinessAvailabilityExceptionListResponseDTO getMyExceptions(AppUser currentUser) {
        return getMyExceptions(currentUser, null);
    }

    public BusinessAvailabilityExceptionListResponseDTO getMyExceptions(AppUser currentUser, Long businessProfileId) {
        BusinessProfile profile = businessProfileId == null ? null : requireOwnerProfile(currentUser, businessProfileId);
        return BusinessAvailabilityExceptionListResponseDTO.builder()
                .items((profile == null ? businessAvailabilityExceptionRepository.findByOwnerId(currentUser.getId()) : businessAvailabilityExceptionRepository.findByBusinessProfileIdAndOwnerId(profile.getId(), currentUser.getId())).stream()
                        .map(businessAvailabilityMgr::toDto)
                        .toList())
                .build();
    }

    @Transactional
    public BusinessAvailabilityRuleResponseDTO createMyRule(BusinessAvailabilityRuleRequestDTO dto, AppUser currentUser) {
        return createMyRule(dto, currentUser, null);
    }

    @Transactional
    public BusinessAvailabilityRuleResponseDTO createMyRule(BusinessAvailabilityRuleRequestDTO dto, AppUser currentUser, Long businessProfileId) {
        BusinessProfile profile = businessProfileId == null ? requireOwnerProfile(currentUser) : requireOwnerProfile(currentUser, businessProfileId);
        businessAvailabilityValidationService.validateBusinessTimezoneConfigured(profile);
        businessAvailabilityValidationService.validateRuleRequest(dto);

        BusinessAvailabilityRule rule = new BusinessAvailabilityRule();
        rule.setBusinessProfile(profile);
        applyRule(rule, dto, currentUser, profile);
        return businessAvailabilityMgr.toDto(businessAvailabilityRuleRepository.save(rule));
    }

    @Transactional
    public BusinessAvailabilityRuleResponseDTO updateMyRule(Long ruleId, BusinessAvailabilityRuleRequestDTO dto, AppUser currentUser) {
        BusinessAvailabilityRule rule = businessAvailabilityRuleRepository.findOwnedById(ruleId, currentUser.getId())
                .orElseThrow(() -> ServiceErrors.notFound("Business availability rule not found"));
        businessAvailabilityValidationService.validateBusinessTimezoneConfigured(rule.getBusinessProfile());
        businessAvailabilityValidationService.validateRuleRequest(dto);
        applyRule(rule, dto, currentUser, rule.getBusinessProfile());
        return businessAvailabilityMgr.toDto(businessAvailabilityRuleRepository.save(rule));
    }

    @Transactional
    public void deleteMyRule(Long ruleId, AppUser currentUser) {
        BusinessAvailabilityRule rule = businessAvailabilityRuleRepository.findOwnedById(ruleId, currentUser.getId())
                .orElseThrow(() -> ServiceErrors.notFound("Business availability rule not found"));
        rule.setActive(false);
        businessAvailabilityRuleRepository.save(rule);
    }

    @Transactional
    public BusinessAvailabilityExceptionResponseDTO createMyException(BusinessAvailabilityExceptionRequestDTO dto, AppUser currentUser) {
        return createMyException(dto, currentUser, null);
    }

    @Transactional
    public BusinessAvailabilityExceptionResponseDTO createMyException(BusinessAvailabilityExceptionRequestDTO dto, AppUser currentUser, Long businessProfileId) {
        BusinessProfile profile = businessProfileId == null ? requireOwnerProfile(currentUser) : requireOwnerProfile(currentUser, businessProfileId);
        businessAvailabilityValidationService.validateBusinessTimezoneConfigured(profile);
        businessAvailabilityValidationService.validateExceptionRequest(dto);
        BusinessAvailabilityException exception = new BusinessAvailabilityException();
        exception.setBusinessProfile(profile);
        applyException(exception, dto, currentUser, profile);
        return businessAvailabilityMgr.toDto(businessAvailabilityExceptionRepository.save(exception));
    }

    @Transactional
    public BusinessAvailabilityExceptionResponseDTO updateMyException(Long exceptionId, BusinessAvailabilityExceptionRequestDTO dto, AppUser currentUser) {
        BusinessAvailabilityException exception = businessAvailabilityExceptionRepository.findOwnedById(exceptionId, currentUser.getId())
                .orElseThrow(() -> ServiceErrors.notFound("Business availability exception not found"));
        businessAvailabilityValidationService.validateBusinessTimezoneConfigured(exception.getBusinessProfile());
        businessAvailabilityValidationService.validateExceptionRequest(dto);
        applyException(exception, dto, currentUser, exception.getBusinessProfile());
        return businessAvailabilityMgr.toDto(businessAvailabilityExceptionRepository.save(exception));
    }

    @Transactional
    public void deleteMyException(Long exceptionId, AppUser currentUser) {
        BusinessAvailabilityException exception = businessAvailabilityExceptionRepository.findOwnedById(exceptionId, currentUser.getId())
                .orElseThrow(() -> ServiceErrors.notFound("Business availability exception not found"));
        businessAvailabilityExceptionRepository.delete(exception);
    }

    private BusinessProfile requireOwnerProfile(AppUser currentUser) {
        return businessProfileRepository.findByOwnerId(currentUser.getId())
                .orElseThrow(() -> ServiceErrors.badRequest("Create your business profile before managing availability"));
    }

    private BusinessProfile requireOwnerProfile(AppUser currentUser, Long businessProfileId) {
        return businessProfileRepository.findById(businessProfileId)
                .filter(profile -> profile.getOwner().getId().equals(currentUser.getId()))
                .orElseThrow(() -> ServiceErrors.notFound("Business profile not found"));
    }

    private void applyRule(BusinessAvailabilityRule rule, BusinessAvailabilityRuleRequestDTO dto, AppUser currentUser, BusinessProfile profile) {
        rule.setBusinessOffering(resolveOwnedOffering(dto.getBusinessOfferingId(), currentUser, profile));
        rule.setDayOfWeek(dto.getDayOfWeek());
        rule.setStartTimeLocal(dto.getStartTimeLocal());
        rule.setEndTimeLocal(dto.getEndTimeLocal());
        rule.setSlotGranularityMinutes(dto.getSlotGranularityMinutes());
        rule.setCapacityOverride(dto.getCapacityOverride());
        rule.setValidFrom(dto.getValidFrom());
        rule.setValidUntil(dto.getValidUntil());
        rule.setActive(dto.getActive() == null || dto.getActive());
    }

    private void applyException(BusinessAvailabilityException exception, BusinessAvailabilityExceptionRequestDTO dto, AppUser currentUser, BusinessProfile profile) {
        exception.setBusinessOffering(resolveOwnedOffering(dto.getBusinessOfferingId(), currentUser, profile));
        exception.setExceptionType(dto.getExceptionType());
        exception.setStartAt(dto.getStartAt());
        exception.setEndAt(dto.getEndAt());
        exception.setReplacementCapacity(dto.getReplacementCapacity());
        exception.setReplacementStartTimeLocal(dto.getReplacementStartTimeLocal());
        exception.setReplacementEndTimeLocal(dto.getReplacementEndTimeLocal());
        exception.setReason(dto.getReason() == null ? null : dto.getReason().trim());
    }

    private BusinessOffering resolveOwnedOffering(Long offeringId, AppUser currentUser, BusinessProfile profile) {
        if (offeringId == null) {
            return null;
        }
        return businessOfferingRepository.findOwnedById(offeringId, currentUser.getId())
                .filter(offering -> offering.getBusinessProfile().getId().equals(profile.getId()))
                .orElseThrow(() -> ServiceErrors.badRequest("Business offering not found for this owner"));
    }
}
