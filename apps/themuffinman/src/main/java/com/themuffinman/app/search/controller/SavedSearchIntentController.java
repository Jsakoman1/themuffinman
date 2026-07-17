package com.themuffinman.app.search.controller;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.search.dto.SavedSearchIntentRequestDTO;
import com.themuffinman.app.search.dto.SavedSearchIntentResponseDTO;
import com.themuffinman.app.search.service.SavedSearchIntentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequiredArgsConstructor @RequestMapping("/search/saved")
public class SavedSearchIntentController {
    private final SavedSearchIntentService service;
    @GetMapping public List<SavedSearchIntentResponseDTO> getMine(@AuthenticationPrincipal AppUser user) { return service.getMine(user); }
    @PostMapping public SavedSearchIntentResponseDTO create(@Valid @RequestBody SavedSearchIntentRequestDTO request, @AuthenticationPrincipal AppUser user) { return service.create(request, user); }
    @PutMapping("/{id}") public SavedSearchIntentResponseDTO update(@PathVariable Long id, @Valid @RequestBody SavedSearchIntentRequestDTO request, @AuthenticationPrincipal AppUser user) { return service.update(id, request, user); }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id, @AuthenticationPrincipal AppUser user) { service.delete(id, user); }
}
