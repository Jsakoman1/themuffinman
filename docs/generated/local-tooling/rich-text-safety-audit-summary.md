# Rich Text Safety Audit

- Generated At: `2026-06-30T11:28:35Z`
## `backend_fields`

- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/AdminAgentPlaygroundService.java", :line: 158, :snippet: "warnings.add(\"Batch quest generation must keep titles and descriptions meaningfully unique.\");"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/AdminAgentPlaygroundService.java", :line: 163, :snippet: "unresolvedInputs.add(\"unique quest descriptions\");"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/AdminAgentPlaygroundService.java", :line: 695, :snippet: "|| normalizedPrompt.contains(\"change my bio\")"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/agent/service/AdminAgentPlaygroundService.java", :line: 700, :snippet: "|| normalizedPrompt.contains(\"promijeni moj bio\")"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/business/dto/BusinessProfileRequestDTO.java", :line: 31, :snippet: "@Size(max = 4000, message = \"Business description must be 4000 characters or less\")"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/business/dto/BusinessProfileRequestDTO.java", :line: 32, :snippet: "private String description;"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/business/dto/BusinessProfileResponseDTO.java", :line: 17, :snippet: "private String description;"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/business/mapper/BusinessProfileMgr.java", :line: 23, :snippet: ".description(RichTextInputValidator.sanitize(profile.getDescription()))"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/business/model/BusinessProfile.java", :line: 44, :snippet: "private String description;"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/business/service/BusinessProfileService.java", :line: 70, :snippet: "profile.setDescription(RichTextInputValidator.sanitize(dto.getDescription()));"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/dto/ChatContactDTO.java", :line: 19, :snippet: "private String profileDescription;"}`
- `{:file: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/dto/ChatConversationSummaryDTO.java", :line: 21, :snippet: "private String otherUserProfileDescription;"}`

## `frontend_renderers`

- `{:file: "apps/themuffinman/frontend/src/components/editor/AsyncRichTextEditor.vue", :line: 3, :snippet: "import RichTextEditorLoading from \"./RichTextEditorLoading.vue\""}`
- `{:file: "apps/themuffinman/frontend/src/components/editor/AsyncRichTextEditor.vue", :line: 15, :snippet: "const RichTextEditor = defineAsyncComponent({"}`
- `{:file: "apps/themuffinman/frontend/src/components/editor/AsyncRichTextEditor.vue", :line: 16, :snippet: "loader: () :  import(\"./RichTextEditor.vue\"),"}`
- `{:file: "apps/themuffinman/frontend/src/components/editor/AsyncRichTextEditor.vue", :line: 17, :snippet: "loadingComponent: RichTextEditorLoading,"}`
- `{:file: "apps/themuffinman/frontend/src/components/editor/AsyncRichTextEditor.vue", :line: 23, :snippet: "<RichTextEditor"}`
- `{:file: "apps/themuffinman/frontend/src/components/editor/RichTextEditor.vue", :line: 11, :snippet: "import {richTextHasContent, sanitizeRichTextHtml} from \"../../shared/richText.ts\""}`
- `{:file: "apps/themuffinman/frontend/src/components/editor/RichTextEditor.vue", :line: 48, :snippet: "? sanitizeRichTextHtml(text)"}`
- `{:file: "apps/themuffinman/frontend/src/components/editor/RichTextEditor.vue", :line: 53, :snippet: "const sanitized = sanitizeRichTextHtml(value)"}`
- `{:file: "apps/themuffinman/frontend/src/components/editor/RichTextEditor.vue", :line: 54, :snippet: "return richTextHasContent(sanitized) ? sanitized : \"\""}`
- `{:file: "apps/themuffinman/frontend/src/components/editor/RichTextEditor.vue", :line: 112, :snippet: "const isEmpty = computed(() :  editor.value?.isEmpty ?? !richTextHasContent(props.modelValue))"}`
- `{:file: "apps/themuffinman/frontend/src/components/profile/ProfileEntityCard.vue", :line: 4, :snippet: "import ProfileBio from \"./ProfileBio.vue\""}`
- `{:file: "apps/themuffinman/frontend/src/components/profile/ProfileEntityCard.vue", :line: 51, :snippet: "<ProfileBio v-if=\"description || descriptionPlaceholder\" :text=\"description\" :placeholder=\"descriptionPlaceholder\" />"}`

