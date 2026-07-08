# Symbol Test Links ChatController

- Next action: `cd apps/themuffinman && ./mvnw test -Dtest=ChatServiceTest,VisionChatExecutionServiceTest`, `cd apps/themuffinman && ./mvnw test -Dtest=CircleServiceTest,CircleRelationServiceTest,ChatServiceTest`

## Details

- Nearby tests: apps/themuffinman/src/test/java/com/themuffinman/app/chat/service/ChatServiceTest.java | apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionChatExecutionServiceTest.java
- Scenario hits: id: social-chat-relation-access | domain: social | risk: high | scenario: Circle membership, accepted relation state, blocking, discovery, and chat eligibility must not leak pending or stale contacts. | test_files: apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleServiceTest.java, apps/themuffinman/src/test/java/com/themuffinman/app/social/service/CircleRelationServiceTest.java, apps/themuffinman/src/test/java/com/themuffinman/app/chat/service/ChatServiceTest.java | commands: cd apps/themuffinman && ./mvnw test -Dtest=CircleServiceTest,CircleRelationServiceTest,ChatServiceTest
