# Generated Artifact Freshness Audit

- Decision: `stale`
- Why: stale artifacts=6
- Next action: `ruby scripts/generate-agent-endpoint-inventory.rb`, `ruby scripts/generate-automation-read-model-inventory.rb`, `ruby scripts/generate-source-of-truth-audit.rb`
- Evidence: artifacts=6

- `agent_endpoint_inventory` `stale` source=`docs/agent-operating-model/sections/intents.yaml`
- `automation_read_model_inventory` `stale` source=`apps/themuffinman/src/main/java/com/themuffinman/app/vision/dto/VisionConversationTurnRequestDTO.java`
- `source_of_truth_audit` `stale` source=`docs/source-of-truth-inventory.md`
- `backend_audit_inventory` `stale` source=`apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSlotService.java`
- `frontend_generated_contract` `stale` source=`apps/themuffinman/src/main/java/com/themuffinman/app/vision/service/VisionSlotService.java`
