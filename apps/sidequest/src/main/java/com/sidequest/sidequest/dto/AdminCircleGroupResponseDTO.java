package com.sidequest.sidequest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCircleGroupResponseDTO {
    private Long id;
    private String name;
    private Long ownerId;
    private String ownerUsername;
    private int memberCount;
    private List<CircleMemberDTO> members;
}
