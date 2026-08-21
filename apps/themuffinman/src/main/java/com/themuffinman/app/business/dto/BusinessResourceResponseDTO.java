package com.themuffinman.app.business.dto;

import java.util.Map;

public record BusinessResourceResponseDTO(Long id, Long resourcePoolId, String resourceKey,
                                          String label, String resourceType, String publicLabel,
                                          boolean active, Map<String, Object> metadata) {}
