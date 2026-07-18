package com.themuffinman.app.activity.dto;
import lombok.Builder; import lombok.Value;
@Value @Builder public class WorkspaceCommandItemDTO { String id; String group; String label; String description; String route; String kind; }
