package com.themuffinman.app.search.repository;

import com.themuffinman.app.search.model.SavedSearchIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SavedSearchIntentRepository extends JpaRepository<SavedSearchIntent, Long> {
    @Query("select intent from SavedSearchIntent intent join fetch intent.owner owner where owner.id = :ownerId order by intent.updatedAt desc, intent.id desc")
    List<SavedSearchIntent> findByOwnerId(Long ownerId);
    @Query("select intent from SavedSearchIntent intent join fetch intent.owner owner where intent.id = :id and owner.id = :ownerId")
    Optional<SavedSearchIntent> findOwnedById(Long id, Long ownerId);
}
