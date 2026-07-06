# Generated Artifact Freshness Audit

- Decision: `stale`
- Why: stale artifacts=3
- Next action: `ruby scripts/generate-agent-endpoint-inventory.rb`, `ruby scripts/generate-source-of-truth-audit.rb`, `ruby scripts/generate-backend-audit-inventory.rb`
- Evidence: artifacts=8

- `agent_endpoint_inventory` `stale` source=`apps/themuffinman/src/main/java/com/themuffinman/app/vision/controller/UserReviewController.java`
- `automation_read_model_inventory` `fresh` source=`apps/themuffinman/src/main/java/com/themuffinman/app/vision/dto/VisionLearningMemoryDTO.java`
- `source_of_truth_audit` `stale` source=`apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/mapper/WorkmarketUserReviewMgr.java`
- `backend_audit_inventory` `stale` source=`apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/mapper/WorkmarketUserReviewMgr.java`
- `frontend_generated_contract` `fresh` source=`apps/themuffinman/src/main/java/com/themuffinman/app/workmarket/mapper/WorkmarketUserReviewMgr.java`
