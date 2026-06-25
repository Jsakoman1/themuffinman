package com.themuffinman.app.workmarket.dto;

import com.themuffinman.app.workmarket.model.QuestAudience;
import com.themuffinman.app.workmarket.model.QuestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestSearchRequestDTO {
    @Nullable
    private String q;
    @Nullable
    private QuestStatus status;
    @Nullable
    private QuestAudience audience;
    @Nullable
    private LocalDate dateFrom;
    @Nullable
    private LocalDate dateTo;
    @Nullable
    private Boolean excludeMine;
    @Nullable
    private Boolean withImages;
    @Nullable
    private Boolean scheduledOnly;
    @Nullable
    private String sort;
    @Nullable
    private Integer page;
    @Nullable
    private Integer size;
}
