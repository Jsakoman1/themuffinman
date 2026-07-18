package com.themuffinman.app.activity.dto;
import lombok.Builder; import lombok.Value; import java.util.List;
@Value @Builder public class WorkspaceCommandCatalogResponseDTO { List<WorkspaceCommandItemDTO> personal; List<WorkspaceCommandItemDTO> navigation; List<WorkspaceCommandItemDTO> create; List<WorkspaceCommandItemDTO> vision; }
