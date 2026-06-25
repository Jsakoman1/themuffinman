package com.themuffinman.app.social.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CircleConnectionsQueryDTO {
    @Nullable
    private String q;
    @Nullable
    private Long circleId;
    @Nullable
    private Boolean unassigned;
    @Nullable
    private Integer page;
    @Nullable
    private Integer size;
}
