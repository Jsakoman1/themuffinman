package com.themuffinman.app.social.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextPageQueryDTO {
    @Nullable
    private String q;
    @Nullable
    private Integer page;
    @Nullable
    private Integer size;
}
