#!/usr/bin/env ruby
# frozen_string_literal: true
require "tmpdir"
require "yaml"
require_relative "../lib/dora/documentation_evidence"
Dir.mktmpdir("dora-doc-evidence") do |sandbox|
  alpha_evidence = File.join(sandbox, "alpha.txt"); beta_evidence = File.join(sandbox, "beta.txt")
  File.write(alpha_evidence, "alpha invariant\n"); File.write(beta_evidence, "beta invariant\n")
  alpha = File.join(sandbox, "alpha.yaml"); beta = File.join(sandbox, "beta.yaml")
  File.write(alpha, YAML.dump({"kind" => "dora_documentation_evidence", "version" => 1, "claims" => [{"id" => "alpha", "match" => "alpha invariant", "evidence" => [alpha_evidence]}]}))
  File.write(beta, YAML.dump({"kind" => "dora_documentation_evidence", "version" => 1, "claims" => [{"id" => "beta", "match" => "beta invariant", "evidence" => [beta_evidence]}]}))
  abort "alpha evidence was not matched" unless Dora::DocumentationEvidence.review!(alpha).first.fetch("matched")
  abort "beta evidence was not matched" unless Dora::DocumentationEvidence.review!(beta).first.fetch("matched")
end
puts "Dora documentation evidence test passed (two project evidence sets)."
