package com.themuffinman.app.identity.service;
import com.themuffinman.app.common.errors.ServiceErrors;
import com.themuffinman.app.identity.dto.PersonalShortcutResponseDTO;
import com.themuffinman.app.identity.model.*;
import com.themuffinman.app.identity.repository.PersonalWorkspaceShortcutRepository;
import com.themuffinman.app.identity.repository.WorkspaceRailPreferenceRepository;
import com.themuffinman.app.identity.dto.WorkspaceRailPreferenceResponseDTO;
import com.themuffinman.app.workmarket.service.WorkmarketQuestReadService;
import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Propagation; import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class PersonalShortcutService {
 private static final String QUEST = "QUEST"; private static final int MAX_PERSONAL_SHORTCUTS = 12; private static final int DEFAULT_RAIL_WIDTH_PX = 240; private static final int MIN_RAIL_WIDTH_PX = 216; private static final int MAX_RAIL_WIDTH_PX = 280; private final PersonalWorkspaceShortcutRepository repository; private final WorkspaceRailPreferenceRepository railPreferences; private final WorkmarketQuestReadService questReadService;
 @Transactional(propagation = Propagation.NOT_SUPPORTED)
 public List<PersonalShortcutResponseDTO> getMine(AppUser user) { return repository.findByOwnerIdOrderByCreatedAtDesc(user.getId()).stream().map(item -> resolve(item, user)).flatMap(java.util.Optional::stream).toList(); }
 @Transactional public void pinQuest(long questId, AppUser user) { if (repository.findByOwnerIdAndTargetTypeAndTargetId(user.getId(), QUEST, questId).isPresent()) return; questReadService.getQuestResponseById(questId, user); if (repository.countByOwnerId(user.getId()) >= MAX_PERSONAL_SHORTCUTS) throw ServiceErrors.badRequest("You can keep up to " + MAX_PERSONAL_SHORTCUTS + " personal shortcuts"); var item = new PersonalWorkspaceShortcut(); item.setOwner(user); item.setTargetType(QUEST); item.setTargetId(questId); repository.save(item); }
 @Transactional public void unpinQuest(long questId, AppUser user) { repository.findByOwnerIdAndTargetTypeAndTargetId(user.getId(), QUEST, questId).ifPresent(repository::delete); }
 @Transactional(propagation = Propagation.NOT_SUPPORTED) public WorkspaceRailPreferenceResponseDTO getRailPreference(AppUser user) { return WorkspaceRailPreferenceResponseDTO.builder().railWidthPx(railPreferences.findByOwnerId(user.getId()).map(item -> item.getRailWidthPx()).orElse(DEFAULT_RAIL_WIDTH_PX)).build(); }
 @Transactional public WorkspaceRailPreferenceResponseDTO updateRailPreference(int railWidthPx, AppUser user) { if (railWidthPx < MIN_RAIL_WIDTH_PX || railWidthPx > MAX_RAIL_WIDTH_PX) throw ServiceErrors.badRequest("Workspace rail width must be between " + MIN_RAIL_WIDTH_PX + " and " + MAX_RAIL_WIDTH_PX + " pixels"); var preference = railPreferences.findByOwnerId(user.getId()).orElseGet(() -> { var created = new WorkspaceRailPreference(); created.setOwner(user); return created; }); preference.setRailWidthPx(railWidthPx); return WorkspaceRailPreferenceResponseDTO.builder().railWidthPx(railPreferences.save(preference).getRailWidthPx()).build(); }
 private java.util.Optional<PersonalShortcutResponseDTO> resolve(PersonalWorkspaceShortcut item, AppUser user) { if (!QUEST.equals(item.getTargetType())) return java.util.Optional.empty(); try { var quest = questReadService.getQuestResponseById(item.getTargetId(), user); return java.util.Optional.of(PersonalShortcutResponseDTO.builder().targetId(quest.getId()).targetType(QUEST).title(quest.getTitle()).route("/work/quests/" + quest.getId()).build()); } catch (RuntimeException inaccessible) { return java.util.Optional.empty(); } }
}
