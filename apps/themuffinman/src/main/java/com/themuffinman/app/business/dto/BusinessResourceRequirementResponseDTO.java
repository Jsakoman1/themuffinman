package com.themuffinman.app.business.dto;

public record BusinessResourceRequirementResponseDTO(Long id, Long businessOfferingId,
                                                     String offeringTitle, Long resourcePoolId,
                                                     String resourceType, int requiredCount,
                                                     String assignmentMode) {}
