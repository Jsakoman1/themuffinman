package com.themuffinman.app.workmarket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestListResponseDTO {
    private List<QuestResponseDTO> items;
    private int page;
    private int size;
    private long totalItems;
    private int totalPages;
    private QuestListPresentationDTO presentation;
}
