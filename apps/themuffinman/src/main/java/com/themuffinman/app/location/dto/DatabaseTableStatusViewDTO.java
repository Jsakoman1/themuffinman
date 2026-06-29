package com.themuffinman.app.location.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseTableStatusViewDTO {
    private String tableName;
    private long rowCount;
}
