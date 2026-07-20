package com.themuffinman.app.identity.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity
@Table(name = "appearance_preference")
public class AppearancePreference {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "owner_id", nullable = false, unique = true) private AppUser owner;
    @Column(name = "theme", nullable = false, length = 16) private String theme;
}
