package com.themuffinman.app.workmarket.dto;

import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminApplicationsQueryDTO {
    @Nullable
    private String q;
    @Nullable
    private QuestApplicationStatus status;
    @Nullable
    private Integer page;
    @Nullable
    private Integer size;
}
