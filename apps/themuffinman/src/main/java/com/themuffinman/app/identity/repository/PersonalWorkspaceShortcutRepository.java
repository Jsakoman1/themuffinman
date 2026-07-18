package com.themuffinman.app.identity.repository;
import com.themuffinman.app.identity.model.PersonalWorkspaceShortcut;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface PersonalWorkspaceShortcutRepository extends JpaRepository<PersonalWorkspaceShortcut, Long> {
 List<PersonalWorkspaceShortcut> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
 Optional<PersonalWorkspaceShortcut> findByOwnerIdAndTargetTypeAndTargetId(Long ownerId, String targetType, Long targetId);
 long countByOwnerId(Long ownerId);
}
