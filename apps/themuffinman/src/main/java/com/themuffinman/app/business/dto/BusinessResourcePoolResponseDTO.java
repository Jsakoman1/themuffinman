package com.themuffinman.app.business.dto;

public record BusinessResourcePoolResponseDTO(Long id, String poolKey, String label,
                                              String resourceType, int capacity,
                                              String publicLabel, boolean active) {}
