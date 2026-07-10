package com.themuffinman.app.vision.repository;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.vision.model.VisionConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VisionConversationRepository extends JpaRepository<VisionConversation, Long> {
    Optional<VisionConversation> findByIdAndOwner(Long id, AppUser owner);
    Optional<VisionConversation> findFirstByOwnerAndLastClientRequestIdOrderByUpdatedAtDesc(AppUser owner, String lastClientRequestId);
    List<VisionConversation> findTop5ByOwnerOrderByUpdatedAtDesc(AppUser owner);
}
