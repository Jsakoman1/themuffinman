# Generated Artifact Freshness Audit

- Decision: `stale`
- Why: stale artifacts=3
- Next action: `ruby scripts/generate-automation-read-model-inventory.rb`, `ruby scripts/generate-source-of-truth-audit.rb`, `ruby scripts/generate-backend-audit-inventory.rb`
- Evidence: artifacts=8

- `agent_endpoint_inventory` `fresh` source=`docs/agent-operating-model/sections/intents.yaml`
- `automation_read_model_inventory` `stale` source=`apps/themuffinman/src/main/java/com/themuffinman/app/vision/dto/VisionCanvasBlockDTO.java`
- `source_of_truth_audit` `stale` source=`apps/themuffinman/src/test/java/com/themuffinman/app/vision/service/VisionSearchDiscoveryServiceTest.java`
- `backend_audit_inventory` `stale` source=`apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSearchDiscoveryService.java`
- `frontend_generated_contract` `fresh` source=`apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSearchDiscoveryService.java`
