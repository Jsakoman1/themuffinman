package com.themuffinman.app.vision.dto;

import com.themuffinman.app.vision.model.QuestApplicationStatus;
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
