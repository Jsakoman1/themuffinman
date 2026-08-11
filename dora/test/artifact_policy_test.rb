#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/artifact_policy"

Dir.mktmpdir("dora-artifact-policy") do |sandbox|
  alpha = File.join(sandbox, "alpha.yaml")
  beta = File.join(sandbox, "beta.yaml")
  File.write(alpha, YAML.dump({"kind" => "dora_artifact_policy", "version" => 1, "generated_roots" => ["reports"], "deletion_authority" => "project_authorized_command"}))
  File.write(beta, YAML.dump({"kind" => "dora_artifact_policy", "version" => 1, "generated_roots" => ["evidence/tmp", "build"], "deletion_authority" => "project_authorized_command", "work_artifact_audit" => {"paths" => ["docs/work"], "non_executable_records" => [{"path" => "docs/work/historical-review.yaml", "reason" => "Historical narrative record."}]}}))
  abort "alpha policy was not loaded" unless Dora::ArtifactPolicy.load!(alpha) == ["reports"]
  abort "beta policy was not loaded" unless Dora::ArtifactPolicy.load!(beta) == ["evidence/tmp", "build"]
  audit = Dora::ArtifactPolicy.work_artifact_audit_config!(beta)
  abort "non-executable historical record was not explicitly classified" unless audit == {"paths" => ["docs/work"], "non_executable_paths" => ["docs/work/historical-review.yaml"]}
end
puts "Dora artifact policy test passed (two project policies)."
