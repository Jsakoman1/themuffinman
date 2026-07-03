# Rich Text Safety Audit

- Generated At: `2026-07-03T09:17:42Z`
## `backend_fields`

- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/AdminAgentExecutionService.java", :line: 97, :snippet: ".description(buildQuestDescription(plan.getTopic(), targetUser, index + 1, effectiveCount))"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/AdminAgentExecutionService.java", :line: 175, :snippet: "private String buildQuestDescription(String topic, AppUser targetUser, int itemIndex, int totalCount) {"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/business/dto/BusinessProfileRequestDTO.java", :line: 31, :snippet: "@Size(max = 4000, message = \"Business description must be 4000 characters or less\")"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/business/dto/BusinessProfileRequestDTO.java", :line: 32, :snippet: "private String description;"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/business/dto/BusinessProfileResponseDTO.java", :line: 17, :snippet: "private String description;"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/business/mapper/BusinessProfileMgr.java", :line: 23, :snippet: ".description(RichTextInputValidator.sanitize(profile.getDescription()))"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/business/model/BusinessProfile.java", :line: 44, :snippet: "private String description;"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/business/service/BusinessProfileService.java", :line: 70, :snippet: "profile.setDescription(RichTextInputValidator.sanitize(dto.getDescription()));"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/dto/ChatContactDTO.java", :line: 19, :snippet: "private String profileDescription;"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/dto/ChatConversationSummaryDTO.java", :line: 21, :snippet: "private String otherUserProfileDescription;"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/service/ChatService.java", :line: 259, :snippet: ".otherUserProfileDescription(otherUser.getProfileDescription())"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/service/ChatService.java", :line: 282, :snippet: ".profileDescription(contact.getProfileDescription())"}`

## `frontend_renderers`


