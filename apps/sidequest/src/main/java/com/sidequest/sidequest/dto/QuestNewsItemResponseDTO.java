package com.sidequest.sidequest.dto;

import com.sidequest.sidequest.model.QuestNewsType;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestNewsItemResponseDTO {
    private Long id;
    private QuestNewsType type;
    private String title;
    private String message;
    private Long questId;
    private String questTitle;
    private Long applicationId;
    private Long actorUserId;
    private String actorUsername;
    private Instant readAt;
    private Instant createdAt;
}
