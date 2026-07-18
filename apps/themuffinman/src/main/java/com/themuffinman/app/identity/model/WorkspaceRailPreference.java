package com.themuffinman.app.identity.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity
@Table(name = "workspace_rail_preference")
public class WorkspaceRailPreference {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "owner_id", nullable = false, unique = true) private AppUser owner;
    @Column(name = "rail_width_px", nullable = false) private int railWidthPx;
}
