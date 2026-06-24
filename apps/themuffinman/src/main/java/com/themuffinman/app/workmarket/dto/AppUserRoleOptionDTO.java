package com.themuffinman.app.workmarket.dto;

import com.themuffinman.app.identity.model.AppUserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUserRoleOptionDTO {
    private AppUserRole value;
    private String label;
}
