package com.themuffinman.app.business.dto;

import java.util.List;

public record BusinessResourceConfigurationDTO(
        Long businessProfileId,
        List<BusinessResourcePoolResponseDTO> pools,
        List<BusinessResourceResponseDTO> resources,
        List<BusinessResourceRequirementResponseDTO> requirements
) {}
