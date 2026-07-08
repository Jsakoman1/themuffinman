package com.themuffinman.app.workmarket.service;

import com.themuffinman.app.identity.model.AppUser;
import com.themuffinman.app.workmarket.dto.DashboardVoiceTranscriptionDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface WorkmarketDashboardVoiceService {

    DashboardVoiceTranscriptionDTO transcribe(MultipartFile audioFile, AppUser currentUser);

    ResponseEntity<byte[]> synthesize(String text, AppUser currentUser);
}
