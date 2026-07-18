package com.themuffinman.app.identity.service;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.dto.PersonalShortcutResponseDTO;
import com.themuffinman.app.identity.model.*;
import com.themuffinman.app.identity.repository.PersonalWorkspaceShortcutRepository;
import com.themuffinman.app.workmarket.service.WorkmarketQuestReadService;
import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Propagation; import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class PersonalShortcutService {
 private static final String QUEST = "QUEST"; private final PersonalWorkspaceShortcutRepository repository; private final WorkmarketQuestReadService questReadService;
 @Transactional(propagation = Propagation.NOT_SUPPORTED)
 public List<PersonalShortcutResponseDTO> getMine(AppUser user) { return repository.findByOwnerIdOrderByCreatedAtDesc(user.getId()).stream().map(item -> resolve(item, user)).flatMap(java.util.Optional::stream).toList(); }
 @Transactional public void pinQuest(long questId, AppUser user) { questReadService.getQuestResponseById(questId, user); repository.findByOwnerIdAndTargetTypeAndTargetId(user.getId(), QUEST, questId).orElseGet(() -> { var item = new PersonalWorkspaceShortcut(); item.setOwner(user); item.setTargetType(QUEST); item.setTargetId(questId); return repository.save(item); }); }
 @Transactional public void unpinQuest(long questId, AppUser user) { repository.findByOwnerIdAndTargetTypeAndTargetId(user.getId(), QUEST, questId).ifPresent(repository::delete); }
 private java.util.Optional<PersonalShortcutResponseDTO> resolve(PersonalWorkspaceShortcut item, AppUser user) { if (!QUEST.equals(item.getTargetType())) return java.util.Optional.empty(); try { var quest = questReadService.getQuestResponseById(item.getTargetId(), user); return java.util.Optional.of(PersonalShortcutResponseDTO.builder().targetId(quest.getId()).targetType(QUEST).title(quest.getTitle()).route("/work/quests/" + quest.getId()).build()); } catch (RuntimeException inaccessible) { return java.util.Optional.empty(); } }
}
