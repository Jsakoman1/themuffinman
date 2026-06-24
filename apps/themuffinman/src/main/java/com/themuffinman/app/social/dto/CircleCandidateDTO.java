package com.themuffinman.app.social.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CircleCandidateDTO {
    private Long id;
    private String username;
    private String profileDescription;
    private String profileAvatarDataUrl;
    private String email;
}
