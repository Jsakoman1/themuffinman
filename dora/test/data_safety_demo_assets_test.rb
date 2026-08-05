#!/usr/bin/env ruby
# frozen_string_literal: true

require "yaml"

ROOT = File.expand_path("..", __dir__)
pack = YAML.load_file(File.join(ROOT, "packs/data-safety-demo.yaml"))
abort "data safety pack is invalid" unless pack["kind"] == "dora_data_safety_demo_pack" && pack["external_actions"] == "explicit_user_approval_required"
template_root = File.join(ROOT, pack.fetch("template_root"))
required = %w[README.md data-safety-profile.yaml demo-data-policy.yaml]
abort "data safety templates are incomplete" unless required.all? { |name| File.file?(File.join(template_root, name)) }
content = required.map { |name| File.read(File.join(template_root, name)) }.join("\n")
abort "templates allow real data" unless content.include?("real_data_excluded: true")
abort "templates contain an executable external operation" if content.match?(/pg_dump|psql\s|curl\s|rm\s+-rf|docker\s+compose/i)
puts "Dora data safety demo assets test passed (static templates require approval and keep demo data separate from real data)."
