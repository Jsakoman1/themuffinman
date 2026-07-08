package com.themuffinman.app.workmarket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardApplicationGroupDTO {
    private String key;
    private String label;
    private String tone;
    private int count;
    private List<QuestApplicationResponseDTO> items;
}
