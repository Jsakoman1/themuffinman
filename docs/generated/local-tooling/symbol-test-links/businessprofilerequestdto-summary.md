# Symbol Test Links BusinessProfileRequestDTO

- Next action: `cd apps/themuffinman && ./mvnw test -Dtest=BusinessProfileServiceTest,BusinessBookingControllerTest,BusinessAvailabilityComputationServiceTest,BusinessBookingPolicyServiceTest,BusinessBookingPresentationServiceTest,BusinessBookingReadServiceTest,BusinessBookingReadSupportTest,BusinessBookingValidationServiceTest`, `cd apps/themuffinman && ./mvnw test -Dtest=BusinessProfileServiceTest`

## Details

- Direct tests: apps/themuffinman/src/test/java/com/themuffinman/app/business/service/BusinessProfileServiceTest.java
- Nearby tests: apps/themuffinman/src/test/java/com/themuffinman/app/business/controller/BusinessBookingControllerTest.java | apps/themuffinman/src/test/java/com/themuffinman/app/business/service/BusinessAvailabilityComputationServiceTest.java | apps/themuffinman/src/test/java/com/themuffinman/app/business/service/BusinessBookingPolicyServiceTest.java
- Nearby tests more: 11
- Scenario hits: id: business-profile-public-directory | domain: business | risk: medium | scenario: Business profiles must derive stable slugs, reject duplicate slugs, expose active directory entries, and hide inactive public profiles. | test_files: apps/themuffinman/src/test/java/com/themuffinman/app/business/service/BusinessProfileServiceTest.java | commands: cd apps/themuffinman && ./mvnw test -Dtest=BusinessProfileServiceTest
