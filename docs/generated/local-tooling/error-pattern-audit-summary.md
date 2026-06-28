# Error Pattern Audit

- Generated At: `2026-06-28T20:30:23Z`
## `hits`

- `{:pattern: "response_status_exception", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/common/controller/GlobalExceptionHandler.java", :line: 18, :snippet: "import org.springframework.web.server.ResponseStatusException;"}`
- `{:pattern: "response_status_exception", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/common/controller/GlobalExceptionHandler.java", :line: 26, :snippet: "@ExceptionHandler(ResponseStatusException.class)"}`
- `{:pattern: "response_status_exception", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/common/controller/GlobalExceptionHandler.java", :line: 27, :snippet: "public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex) {"}`
- `{:pattern: "response_status_exception", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/common/errors/ServiceErrors.java", :line: 4, :snippet: "import org.springframework.web.server.ResponseStatusException;"}`
- `{:pattern: "response_status_exception", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/common/errors/ServiceErrors.java", :line: 11, :snippet: "public static ResponseStatusException notFound(String message) {"}`
- `{:pattern: "response_status_exception", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/common/errors/ServiceErrors.java", :line: 12, :snippet: "return new ResponseStatusException(HttpStatus.NOT_FOUND, message);"}`
- `{:pattern: "response_status_exception", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/common/errors/ServiceErrors.java", :line: 15, :snippet: "public static ResponseStatusException badRequest(String message) {"}`
- `{:pattern: "response_status_exception", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/common/errors/ServiceErrors.java", :line: 16, :snippet: "return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);"}`
- `{:pattern: "response_status_exception", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/common/errors/ServiceErrors.java", :line: 19, :snippet: "public static ResponseStatusException conflict(String message) {"}`
- `{:pattern: "response_status_exception", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/common/errors/ServiceErrors.java", :line: 20, :snippet: "return new ResponseStatusException(HttpStatus.CONFLICT, message);"}`
- `{:pattern: "response_status_exception", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/common/errors/ServiceErrors.java", :line: 23, :snippet: "public static ResponseStatusException forbidden(String message) {"}`
- `{:pattern: "response_status_exception", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/common/errors/ServiceErrors.java", :line: 24, :snippet: "return new ResponseStatusException(HttpStatus.FORBIDDEN, message);"}`
- `{:pattern: "response_status_exception", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/common/errors/ServiceErrors.java", :line: 27, :snippet: "public static ResponseStatusException unauthorized(String message) {"}`
- `{:pattern: "response_status_exception", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/common/errors/ServiceErrors.java", :line: 28, :snippet: "return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);"}`
- `{:pattern: "lazy_init_risk", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/model/ChatConversation.java", :line: 31, :snippet: "@ManyToOne(fetch = FetchType.LAZY, optional = false)"}`
- `{:pattern: "lazy_init_risk", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/model/ChatConversation.java", :line: 35, :snippet: "@ManyToOne(fetch = FetchType.LAZY, optional = false)"}`
- `{:pattern: "lazy_init_risk", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/model/ChatConversation.java", :line: 48, :snippet: "@ManyToOne(fetch = FetchType.LAZY)"}`
- `{:pattern: "lazy_init_risk", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/model/ChatMessage.java", :line: 28, :snippet: "@ManyToOne(fetch = FetchType.LAZY, optional = false)"}`
- `{:pattern: "lazy_init_risk", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/model/ChatMessage.java", :line: 32, :snippet: "@ManyToOne(fetch = FetchType.LAZY, optional = false)"}`
- `{:pattern: "lazy_init_risk", :file: "apps/themuffinman/src/main/java/com/themuffinman/app/chat/model/ChatPresence.java", :line: 31, :snippet: "@ManyToOne(fetch = FetchType.LAZY, optional = false)"}`

