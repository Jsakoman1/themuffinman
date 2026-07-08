package com.themuffinman.app.business.controller;

import com.themuffinman.app.business.dto.BusinessOfferingListResponseDTO;
import com.themuffinman.app.business.dto.BusinessPublicPageDTO;
import com.themuffinman.app.business.service.BusinessPublicReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business/public")
@RequiredArgsConstructor
public class BusinessPublicController {

    private final BusinessPublicReadService businessPublicReadService;

    @GetMapping("/{slug}")
    public BusinessPublicPageDTO getPublicBusinessPage(@PathVariable String slug) {
        return businessPublicReadService.getPublicBusinessPage(slug);
    }

    @GetMapping("/{slug}/offerings")
    public BusinessOfferingListResponseDTO getPublicOfferings(@PathVariable String slug) {
        return businessPublicReadService.getPublicOfferings(slug);
    }
}
