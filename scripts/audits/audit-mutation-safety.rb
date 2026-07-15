#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "set"
require "time"
require_relative "../audit_support"

OUT_JSON = "docs/audit-output/mutation-safety.json"
OUT_MD = "docs/audit-output/mutation-safety-summary.md"

MUTATING_HTTP_METHODS = %w[Post Put Patch Delete].freeze
MUTATING_METHOD_PATTERN = /\b(create|update|delete|approve|decline|withdraw|block|unblock|start|complete|confirm|reject|cancel|assign|apply|mark|read|submit|request|accept)\w*\s*\(/i
PERMISSION_PATTERN = /permission|authorize|authority|access|admin|owner|creator|applicant|viewer|can[A-Z]|require[A-Z]|policy/i
OWNERSHIP_PATTERN = /owner|ownership|creator|applicant|my[A-Z]|belongs|owned/i
TRANSITION_PATTERN = /status|state|transition|workflow|pending|approved|declined|assigned|progress|complete|cancel|withdraw|confirm|reject/i
SIDE_EFFECT_PATTERN = /notify|notification|news|event|publish|emit|mailService|websocket/i

def rel_glob(*patterns)
  AuditSupport.repo_glob(*patterns).map { |path| AuditSupport.relative_path(path) }
end

def read(path)
  File.read(File.join(AuditSupport::REPO_ROOT, path))
rescue Errno::ENOENT
  ""
end

def domain_for(path)
  return "workmarket" if path.include?("/workmarket/") || path.include?("workmarket")
  return "social" if path.include?("/social/") || path.include?("circle")
  return "identity" if path.include?("/identity/") || path.include?("user")
  return "location" if path.include?("/location/")
  return "chat" if path.include?("/chat/")
  return "business" if path.include?("/business/")
  return "things" if path.include?("/things/")
  return "rides" if path.include?("/rides/")
  return "common" if path.include?("/common/") || path.include?("/config/")
  return "agent" if path.include?("/agent") || path.include?("agent-")

  "shared"
end

def normalize_path(path)
  "/" + path.to_s.gsub(%r{/+}, "/").sub(%r{\A/}, "").sub(%r{/\z}, "")
end

def method_body(content, match_start)
  open_index = content.index("{", match_start)
  return "" unless open_index

  depth = 0
  content[open_index..].chars.each_with_index do |char, index|
    depth += 1 if char == "{"
    depth -= 1 if char == "}"
    return content[open_index, index + 1] if depth.zero?
  end
  content[open_index..] || ""
end

def controller_surfaces
  rel_glob("apps/themuffinman/src/main/java/com/themuffinman/app/**/*Controller.java").flat_map do |path|
    content = read(path)
    root = content[/@RequestMapping\("([^"]+)"\)/, 1] || ""
    content.to_enum(:scan, /@(#{MUTATING_HTTP_METHODS.join('|')})Mapping(?:\("([^"]*)"\))?.{0,700}?public\s+[^{]+\{/m).map do
      http_method = Regexp.last_match(1).upcase
      sub_path = Regexp.last_match(2).to_s
      signature_block = Regexp.last_match(0)
      start_index = Regexp.last_match.begin(0)
      body = method_body(content, start_index)
      method_name = signature_block[/\s([a-z][A-Za-z0-9_]*)\s*\(/, 1] || "unknown"
      {
        id: "endpoint:#{http_method}:#{normalize_path("#{root}/#{sub_path}")}",
        type: "endpoint",
        name: "#{http_method} #{normalize_path("#{root}/#{sub_path}")}",
        method: method_name,
        file: path,
        domain: domain_for(path),
        body: "#{signature_block}\n#{body}"
      }
    end
  end
end

def service_surfaces
  rel_glob("apps/themuffinman/src/main/java/com/themuffinman/app/**/*Service.java", "apps/themuffinman/src/main/java/com/themuffinman/app/**/*UseCase.java").flat_map do |path|
    content = read(path)
    content.to_enum(:scan, /(?:public|protected|private)\s+[A-Za-z0-9_<>, ?\[\]]+\s+([a-z][A-Za-z0-9_]*)\s*\([^)]*\)\s*\{/m).map do
      method_name = Regexp.last_match(1)
      start_index = Regexp.last_match.begin(0)
      body = method_body(content, start_index)
      next nil unless method_name.match?(MUTATING_METHOD_PATTERN) || body.match?(/\.save\(|\.delete|setStatus|transition/i)

      {
        id: "service:#{File.basename(path, '.java')}:#{method_name}",
        type: "service",
        name: "#{File.basename(path, '.java')}##{method_name}",
        method: method_name,
        file: path,
        domain: domain_for(path),
        body: body
      }
    end.compact
  end
end

def backend_tests
  rel_glob("apps/themuffinman/src/test/java/**/*Test.java").map do |path|
    {path: path, content: read(path).downcase}
  end
end

def scenario_catalog_text
  [
    "docs/regression-scenario-catalog.yaml",
    "docs/agent-operating-model/sections/intents.yaml",
    "docs/agent-operating-model.yaml"
  ].map { |path| read(path).downcase }.join("\n")
end

def risk_factors(surface)
  text = "#{surface[:name]} #{surface[:method]} #{surface[:file]} #{surface[:body]}"
  factors = []
  factors << "destructive_endpoint" if surface[:name].start_with?("DELETE ")
  factors << "permission_or_authority" if text.match?(PERMISSION_PATTERN)
  factors << "ownership_sensitive" if text.match?(OWNERSHIP_PATTERN)
  factors << "state_transition" if text.match?(TRANSITION_PATTERN)
  factors << "side_effect" if text.match?(SIDE_EFFECT_PATTERN)
  factors << "admin_surface" if text.match?(/admin/i)
  factors.uniq
end

def search_terms(surface)
  class_term = File.basename(surface[:file], ".java").sub(/Controller\z/, "").sub(/Service\z/, "").sub(/UseCase\z/, "")
  path_terms = surface[:name].scan(/[A-Za-z][A-Za-z0-9]+/).map(&:downcase)
  ([class_term, surface[:method], surface[:domain]] + path_terms).map(&:to_s).map(&:downcase).reject { |term| term.length < 4 }.uniq
end

def matching_test_rows(surface, tests)
  terms = search_terms(surface)
  tests.select do |test|
    basename = File.basename(test[:path], ".java").downcase
    terms.any? { |term| basename.include?(term) || test[:content].include?(term) }
  end.uniq { |test| test[:path] }.first(12)
end

def matching_scenario_signals(surface, catalog)
  terms = search_terms(surface)
  hits = []
  hits << "domain:#{surface[:domain]}" if catalog.include?(surface[:domain])
  %w[permission ownership status transition workflow notification side_effect admin mutation].each do |term|
    hits << term if catalog.include?(term.tr("_", " ")) && terms.any? { |surface_term| catalog.include?(surface_term) }
  end
  hits.uniq
end

def missing_signals(_surface, factors, test_rows, scenarios)
  test_text = test_rows.map { |test| "#{test[:path]} #{test[:content]}" }.join(" ").downcase
  missing = []
  if (factors & %w[permission_or_authority ownership_sensitive admin_surface]).any?
    missing << "missing_permission_or_ownership_test_signal" unless test_text.match?(/policy|permission|access|owner|admin|authority|contract/)
  end
  if factors.include?("state_transition")
    missing << "missing_invalid_transition_test_signal" unless test_text.match?(/workflow|transition|status|state|contract/)
  end
  if factors.include?("side_effect")
    missing << "missing_side_effect_test_signal" unless test_text.match?(/event|news|notification|message/)
  end
  missing << "missing_scenario_test_signal" if scenarios.empty?
  missing
end

def priority_for(factors, missing)
  return "none" if missing.empty?
  return "high" if (factors & %w[destructive_endpoint state_transition permission_or_authority ownership_sensitive admin_surface]).size >= 2 && missing.size >= 2
  return "medium" if (factors & %w[state_transition permission_or_authority ownership_sensitive side_effect admin_surface]).any?

  "low"
end

tests = backend_tests
catalog = scenario_catalog_text
surfaces = (controller_surfaces + service_surfaces).uniq { |surface| surface[:id] }.sort_by { |surface| [surface[:domain], surface[:type], surface[:name]] }
entries = surfaces.map do |surface|
  factors = risk_factors(surface)
  matched_test_rows = matching_test_rows(surface, tests)
  scenarios = matching_scenario_signals(surface, catalog)
  missing = missing_signals(surface, factors, matched_test_rows, scenarios)
  {
    id: surface[:id],
    type: surface[:type],
    name: surface[:name],
    method: surface[:method],
    file: surface[:file],
    domain: surface[:domain],
    risk_factors: factors,
    backend_tests: matched_test_rows.map { |test| test[:path] },
    scenario_signals: scenarios,
    missing_signals: missing,
    priority: priority_for(factors, missing)
  }
end

review_needed = entries.reject { |entry| entry[:priority] == "none" }.sort_by do |entry|
  [%w[high medium low].index(entry[:priority]) || 9, entry[:domain], entry[:name]]
end

report = {
  generated_at: Time.now.utc.iso8601,
  mutation_surface_count: entries.size,
  endpoint_count: entries.count { |entry| entry[:type] == "endpoint" },
  service_method_count: entries.count { |entry| entry[:type] == "service" },
  review_needed_count: review_needed.size,
  high_priority_count: review_needed.count { |entry| entry[:priority] == "high" },
  medium_priority_count: review_needed.count { |entry| entry[:priority] == "medium" },
  entries: entries,
  review_needed: review_needed.first(100),
  notes: [
    "This report is advisory and uses conservative static signals rather than semantic proof.",
    "Treat high-priority rows as candidates for focused scenario or contract tests before changing mutation workflows."
  ]
}

lines = ["# Mutation Safety", ""]
lines << "- Generated at: `#{report[:generated_at]}`"
lines << "- Mutation surfaces scanned: `#{report[:mutation_surface_count]}`"
lines << "- Endpoints: `#{report[:endpoint_count]}`"
lines << "- Service methods: `#{report[:service_method_count]}`"
lines << "- Review needed: `#{report[:review_needed_count]}`"
lines << "- High priority: `#{report[:high_priority_count]}`"
lines << "- Medium priority: `#{report[:medium_priority_count]}`"
lines << ""
review_needed.first(30).each do |entry|
  lines << "- `#{entry[:priority]}` `#{entry[:name]}` #{entry[:file]} missing=#{entry[:missing_signals].join(',')}"
end
lines << ""
lines << "Advisory only: static signals choose review candidates; they do not prove test absence."

AuditSupport.write_json(OUT_JSON, report)
AuditSupport.write_text(OUT_MD, lines.join("\n") + "\n")
puts "Mutation safety"
puts "  mutation surfaces scanned: #{report[:mutation_surface_count]}"
puts "  review needed: #{report[:review_needed_count]}"
puts "  high priority: #{report[:high_priority_count]}"
