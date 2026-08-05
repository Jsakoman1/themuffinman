#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/artifact_policy"

Dir.mktmpdir("dora-artifact-policy") do |sandbox|
  alpha = File.join(sandbox, "alpha.yaml")
  beta = File.join(sandbox, "beta.yaml")
  File.write(alpha, YAML.dump({"kind" => "dora_artifact_policy", "version" => 1, "generated_roots" => ["reports"], "deletion_authority" => "project_authorized_command"}))
  File.write(beta, YAML.dump({"kind" => "dora_artifact_policy", "version" => 1, "generated_roots" => ["evidence/tmp", "build"], "deletion_authority" => "project_authorized_command"}))
  abort "alpha policy was not loaded" unless Dora::ArtifactPolicy.load!(alpha) == ["reports"]
  abort "beta policy was not loaded" unless Dora::ArtifactPolicy.load!(beta) == ["evidence/tmp", "build"]
end
puts "Dora artifact policy test passed (two project policies)."
