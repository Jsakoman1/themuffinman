package com.themuffinman.app.workmarket.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QuestSearchRequestDTOTest {

    @Test
    void modelAttributeFieldsCanBeBoundBySpring() {
        QuestSearchRequestDTO request = new QuestSearchRequestDTO();

        request.setQ("kitchen tap");
        request.setPage(1);
        request.setSize(3);

        assertThat(request.getQ()).isEqualTo("kitchen tap");
        assertThat(request.getPage()).isEqualTo(1);
        assertThat(request.getSize()).isEqualTo(3);
    }
}
