package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.workmarket.dto.QuestDetailRailItemDTO;
import com.themuffinman.app.workmarket.dto.QuestDetailResponseDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuestDetailReadServiceTest {

    @Test
    void detailContractCarriesBackendPreparedPropertyAndActivityRails() {
        QuestDetailResponseDTO detail = QuestDetailResponseDTO.builder()
                .propertyRail(List.of(QuestDetailRailItemDTO.builder().label("Reward").value("30 €").build()))
                .activityRail(List.of(QuestDetailRailItemDTO.builder().label("Current status").value("Open").build()))
                .build();

        assertEquals("30 €", detail.getPropertyRail().getFirst().value());
        assertEquals("Current status", detail.getActivityRail().getFirst().label());
    }
}
