#!/usr/bin/env ruby
# frozen_string_literal: true

require "time"
require "set"
require_relative "../local_tooling_common"

OUT_JSON = "docs/generated/local-tooling/contract-test-gaps.json"
OUT_MD = "docs/generated/local-tooling/contract-test-gaps-summary.md"

def rel_glob(*patterns)
  LocalToolingCommon.repo_glob(*patterns).map { |path| LocalToolingCommon.relative_path(path) }
end

def read(path)
  File.read(File.join(LocalToolingCommon::REPO_ROOT, path))
rescue Errno::ENOENT
  ""
end

def endpoint_entries
  rel_glob("apps/themuffinman/src/main/java/com/themuffinman/app/*/controller/*Controller.java").flat_map do |path|
    content = read(path)
    root = normalize_path(content[/@RequestMapping\("([^"]+)"\)/, 1] || "")
    controller = File.basename(path, ".java")
    content.scan(/@(Get|Post|Put|Patch|Delete)Mapping(?:\("([^"]*)"\))?.{0,900}?public\s+[^{]+\{/m).map do |method, sub_path|
      block = Regexp.last_match(0)
      dtos = block.scan(/\b([A-Z][A-Za-z0-9_]*(?:DTO|Request|Response))\b/).flatten.uniq
      {
        controller: controller,
        file: path,
        method: method.upcase,
        path: normalize_path("#{root}/#{sub_path}"),
        dtos: dtos,
        domain: domain_for(path)
      }
    end
  end
end

def normalize_path(path)
  "/" + path.to_s.gsub(%r{/+}, "/").sub(%r{\A/}, "").sub(%r{/\z}, "")
end

def domain_for(path)
  path.split("/app/", 2).last.to_s.split("/", 2).first || "shared"
end

def backend_tests
  rel_glob("apps/themuffinman/src/test/java/**/*Test.java")
end

def generated_contract_interfaces
  read("apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts").scan(/export interface ([A-Z][A-Za-z0-9_]+)/).flatten.to_set
end

def frontend_files
  rel_glob("apps/themuffinman/frontend/src/**/*.{ts,vue}").reject { |path| path.include?("/contracts/generated/") }
end

def docs_text
  ["docs/business-logic.md", "docs/domain-technical.md", "docs/agent-operating-model.md"].map { |path| read(path) }.join("\n")
end

def nearest_tests(entry, tests)
  terms = ([entry[:controller].sub(/Controller\z/, ""), entry[:domain]] + entry[:dtos]).map(&:downcase)
  tests.select do |test|
    compact = test.downcase
    terms.any? { |term| !term.empty? && compact.include?(term.sub(/dto\z/, "")) }
  end.first(12)
end

def frontend_usage(entry, files)
  needles = ([entry[:path], entry[:path].split("/").last] + entry[:dtos]).compact.reject(&:empty?)
  files.select do |path|
    content = read(path)
    needles.any? { |needle| content.include?(needle) }
  end.first(12)
end

def documented_refs(entry, docs)
  refs = ([entry[:path], entry[:controller]] + entry[:dtos]).select { |needle| docs.include?(needle) }
  refs.uniq
end

def automation_relevant?(entry)
  text = ([entry[:controller], entry[:path], entry[:domain]] + entry[:dtos]).join(" ")
  text.match?(/Agent|Admin|Dashboard|Quest|Application|Circle|Chat|Location|User/i)
end

def gap_entry(entry, tests, contract_interfaces, frontend, docs)
  test_matches = nearest_tests(entry, tests)
  frontend_matches = frontend_usage(entry, frontend)
  missing_contract_dtos = entry[:dtos].reject { |dto| contract_interfaces.include?(dto) }
  doc_refs = documented_refs(entry, docs)
  gaps = []
  gaps << "missing_backend_test_signal" if test_matches.empty?
  gaps << "missing_frontend_or_contract_usage_signal" if frontend_matches.empty?
  gaps << "missing_generated_contract_dto" if missing_contract_dtos.any?
  gaps << "missing_doc_reference_signal" if doc_refs.empty?
  {
    endpoint: "#{entry[:method]} #{entry[:path]}",
    controller: entry[:controller],
    file: entry[:file],
    domain: entry[:domain],
    dtos: entry[:dtos],
    automation_relevant: automation_relevant?(entry),
    backend_tests: test_matches,
    frontend_usage: frontend_matches,
    missing_generated_contract_dtos: missing_contract_dtos,
    documented_refs: doc_refs,
    gaps: gaps,
    priority: priority_for(gaps, automation_relevant?(entry))
  }
end

def priority_for(gaps, automation_relevant)
  return "none" if gaps.empty?
  return "high" if automation_relevant && gaps.include?("missing_backend_test_signal")
  return "medium" if automation_relevant || gaps.size > 1

  "low"
end

tests = backend_tests
contracts = generated_contract_interfaces
frontend = frontend_files
docs = docs_text
entries = endpoint_entries.map { |entry| gap_entry(entry, tests, contracts, frontend, docs) }
review_needed = entries.select { |entry| entry[:priority] != "none" }.sort_by { |entry| [%w[high medium low].index(entry[:priority]) || 9, entry[:endpoint]] }
report = {
  generated_at: Time.now.utc.iso8601,
  endpoint_count: entries.size,
  review_needed_count: review_needed.size,
  high_priority_count: review_needed.count { |entry| entry[:priority] == "high" },
  entries: entries,
  review_needed: review_needed.first(80)
}

lines = ["# Contract Test Gaps", ""]
lines << "- Generated at: `#{report[:generated_at]}`"
lines << "- Endpoints scanned: `#{report[:endpoint_count]}`"
lines << "- Review needed: `#{report[:review_needed_count]}`"
lines << "- High priority: `#{report[:high_priority_count]}`"
lines << ""
review_needed.first(25).each do |entry|
  lines << "- `#{entry[:priority]}` `#{entry[:endpoint]}` #{entry[:controller]} gaps=#{entry[:gaps].join(',')}"
end

LocalToolingCommon.write_json(OUT_JSON, report)
LocalToolingCommon.write_text(OUT_MD, lines.join("\n") + "\n")
puts "Contract test gaps"
puts "  endpoints scanned: #{report[:endpoint_count]}"
puts "  review needed: #{report[:review_needed_count]}"
puts "  high priority: #{report[:high_priority_count]}"
