package com.themuffinman.app.activity.service;

import com.themuffinman.app.activity.dto.WorkspaceCommandCatalogResponseDTO;
import com.themuffinman.app.activity.dto.WorkspaceCommandItemDTO;
import com.themuffinman.app.activity.dto.WorkspaceHomeResponseDTO;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceHomeReadService {
    private static final String CONTRACT_VERSION = "workspace-home-v1";
    private final WorkspaceCommandCatalogService commandCatalogService;

    public WorkspaceHomeResponseDTO getHome(AppUser user) {
        if (user == null) {
            throw new IllegalArgumentException("Authenticated viewer is required for workspace home");
        }
        WorkspaceCommandCatalogResponseDTO catalog = commandCatalogService.getCatalog(user);
        List<String> permittedRoutes = Stream.concat(safe(catalog.getNavigation()).stream(), safe(catalog.getCreate()).stream())
                .map(WorkspaceCommandItemDTO::getRoute)
                .toList();

        return WorkspaceHomeResponseDTO.builder()
                .contractVersion(CONTRACT_VERSION)
                .greetingName(user.getUsername())
                .launcherActions(Stream.of(
                                action("work", "Mini jobs & help", "Find or offer local help.", "/work/find", "work", "work"),
                                action("things", "Share & lend", "Borrow or list useful things.", "/things", "things", "share"),
                                action("services", "Book services", "Find a local service.", "/business/find", "business", "book"),
                                action("rides", "Car sharing", "Find or offer a ride.", "/rides", "rides", "ride"))
                        .filter(action -> permittedRoutes.contains(action.getRoute()))
                        .toList())
                .build();
    }

    private List<WorkspaceCommandItemDTO> safe(List<WorkspaceCommandItemDTO> items) {
        return items == null ? List.of() : items;
    }

    private WorkspaceHomeResponseDTO.LauncherAction action(String id, String label, String description, String route, String iconKey, String colourRole) {
        return WorkspaceHomeResponseDTO.LauncherAction.builder()
                .id(id).label(label).description(description).route(route).iconKey(iconKey).colourRole(colourRole).build();
    }
}
