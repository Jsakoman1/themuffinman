#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"
require_relative "../lib/dora/compiled_feature_renderers"
require_relative "../lib/dora/generated_feature_manifest"
require_relative "../lib/dora/generated_feature_safety"

ROOT = File.expand_path("..", __dir__)
document = YAML.load_file(File.join(ROOT, "test/fixtures/related-resource-feature.yaml"))
feature = document.fetch("feature")
outputs = Dora::CompiledFeatureRenderers.render_related_resource_packet!(document: document)
trace_path = Dora::GeneratedFeatureManifest.related_trace_path(feature)
abort "related resource renderer did not add relation trace" unless outputs.key?(trace_path)
abort "related resource renderer did not preserve Java source" unless outputs.keys.any? { |path| path.end_with?("LocationEntry.java") }
trace = YAML.load(outputs.fetch(trace_path))
report = Dora::GeneratedFeatureSafety.inspect_related_trace!(trace: trace, feature: feature)
abort "related resource safety did not accept aligned trace" unless report.fetch("safe_to_continue") && report.fetch("trace_path") == trace_path

invalid = Marshal.load(Marshal.dump(document)); invalid.fetch("relation")["target_table"] = "other"
begin
  Dora::CompiledFeatureRenderers.render_related_resource_packet!(document: invalid)
  abort "related resource renderer accepted inconsistent relation"
rescue ArgumentError => error
  abort error.message unless error.message.include?("foreign key")
end

puts "Dora related resource feature renderer test passed (deterministic additive sources and relation trace reject inconsistent declared relations)."
