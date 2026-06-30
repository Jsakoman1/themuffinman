package com.themuffinman.app.vision.repository;

import com.themuffinman.app.vision.model.VisionConversation;
import com.themuffinman.app.vision.model.VisionTurn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisionTurnRepository extends JpaRepository<VisionTurn, Long> {
    long countByConversation(VisionConversation conversation);
}
