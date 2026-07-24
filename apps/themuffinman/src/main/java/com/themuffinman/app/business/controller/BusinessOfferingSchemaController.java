package com.themuffinman.app.business.controller;

import com.themuffinman.app.business.service.BusinessOfferingSchemaService;
import com.themuffinman.app.identity.model.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/business/offerings")
public class BusinessOfferingSchemaController {
    private final BusinessOfferingSchemaService schemaService;

    @GetMapping("/{offeringId}/schema/me")
    public Map<String, Object> get(@PathVariable Long offeringId, @AuthenticationPrincipal AppUser owner) {
        return schemaService.getOwnerSchema(offeringId, owner);
    }

    @PutMapping("/{offeringId}/schema/me")
    public Map<String, Object> replace(@PathVariable Long offeringId, @RequestBody Map<String, Object> request, @AuthenticationPrincipal AppUser owner) {
        return schemaService.replaceOwnerSchema(offeringId, request, owner);
    }
}
