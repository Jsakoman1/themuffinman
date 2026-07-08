package com.themuffinman.app.chat.repository;

import com.themuffinman.app.chat.model.ChatAttachmentUpload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatAttachmentUploadRepository extends JpaRepository<ChatAttachmentUpload, Long> {

    Optional<ChatAttachmentUpload> findByStorageKey(String storageKey);
}
