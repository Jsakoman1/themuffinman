package com.themuffinman.app.identity.controller;

import com.themuffinman.app.identity.dto.PersonalShortcutResponseDTO;
import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.identity.service.PersonalShortcutService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequiredArgsConstructor @RequestMapping("/personal-shortcuts")
public class PersonalShortcutController {
    private final PersonalShortcutService service;
    @GetMapping("/me") public List<PersonalShortcutResponseDTO> getMine(@AuthenticationPrincipal AppUser user) { return service.getMine(user); }
    @PutMapping("/me/quests/{questId}") public void pinQuest(@PathVariable long questId, @AuthenticationPrincipal AppUser user) { service.pinQuest(questId, user); }
    @DeleteMapping("/me/quests/{questId}") public void unpinQuest(@PathVariable long questId, @AuthenticationPrincipal AppUser user) { service.unpinQuest(questId, user); }
}
