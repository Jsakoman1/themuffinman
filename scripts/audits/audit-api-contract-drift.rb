#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../local_tooling_common"

module ApiContractDriftAudit
  extend self

  def run
    backend_dtos = backend_dto_fields
    contract_dtos = generated_contract_fields
    frontend_usage = frontend_field_usage

    entries = backend_dtos.keys.sort.map do |dto_name|
      backend_fields = backend_dtos[dto_name]
      contract_fields = contract_dtos.fetch(dto_name, [])
      field_entries = backend_fields.map do |field|
        usage_count = frontend_usage.fetch(field, 0)
        {
          field: field,
          in_generated_contract: contract_fields.include?(field),
          frontend_usage_count: usage_count,
          drift_category: drift_category(contract_fields.include?(field), usage_count)
        }
      end

      {
        dto: dto_name,
        backend_field_count: backend_fields.size,
        generated_contract_present: contract_dtos.key?(dto_name),
        generated_field_count: contract_fields.size,
        fields: field_entries
      }
    end

    report = {
      generated_at: Time.now.utc.iso8601,
      dto_count: entries.size,
      missing_contract_count: entries.count { |entry| !entry[:generated_contract_present] },
      zero_usage_field_count: entries.sum { |entry| entry[:fields].count { |field| field[:drift_category] == "unused_in_frontend" } },
      dtos: entries
    }
    LocalToolingCommon.write_json("docs/generated/local-tooling/api-contract-drift.json", report)
    LocalToolingCommon.write_text("docs/generated/local-tooling/api-contract-drift-summary.md", markdown_summary(report))
    puts terminal_summary(report)
  end

  def backend_dto_fields
    dto_paths = LocalToolingCommon.repo_glob("apps/themuffinman/src/main/java/com/themuffinman/app/**/*DTO.java")
    dto_paths.each_with_object({}) do |path, result|
      content = LocalToolingCommon.read(path)
      dto_name = File.basename(path, ".java")
      fields = content.scan(/^\s*private\s+(?!static\b)([A-Za-z0-9_<>, ?\[\]]+)\s+([a-zA-Z0-9_]+);/).map { |_type, name| name }
      result[dto_name] = fields.uniq.sort
    end
  end

  def generated_contract_fields
    contract_path = File.join(LocalToolingCommon::REPO_ROOT, "apps/themuffinman/frontend/src/contracts/generated/themuffinmanContract.ts")
    content = LocalToolingCommon.read(contract_path)
    result = {}
    content.scan(/export interface ([A-Za-z0-9_]+) \{(.*?)^\}/m).each do |dto_name, body|
      fields = body.scan(/^\s*([a-zA-Z0-9_?]+):/).flatten.map { |name| name.delete_suffix("?") }
      result[dto_name] = fields.uniq.sort
    end
    result
  end

  def frontend_field_usage
    usage = Hash.new(0)
    frontend_paths = LocalToolingCommon.repo_glob("apps/themuffinman/frontend/src/**/*.{ts,vue}")
    frontend_paths.each do |path|
      next if path.include?("/contracts/generated/")

      content = LocalToolingCommon.read(path)
      content.scan(/\.([a-zA-Z0-9_]+)\b/).flatten.each { |field| usage[field] += 1 }
      content.scan(/\[['"]([a-zA-Z0-9_]+)['"]\]/).flatten.each { |field| usage[field] += 1 }
      content.scan(/\b([a-zA-Z0-9_]+):/).flatten.each { |field| usage[field] += 1 if field.match?(/[a-z]/) }
    end
    usage
  end

  def drift_category(in_contract, usage_count)
    return "missing_in_generated_contract" unless in_contract
    return "unused_in_frontend" if usage_count.zero?

    "used"
  end

  def markdown_summary(report)
    lines = []
    lines << "# API Contract Drift Audit"
    lines << ""
    lines << "- Generated at: `#{report[:generated_at]}`"
    lines << "- Backend DTOs scanned: `#{report[:dto_count]}`"
    lines << "- DTOs missing generated contracts: `#{report[:missing_contract_count]}`"
    lines << "- Zero-usage backend fields: `#{report[:zero_usage_field_count]}`"
    lines << ""
    report[:dtos].select { |entry| !entry[:generated_contract_present] || entry[:fields].any? { |field| field[:drift_category] != "used" } }.first(25).each do |entry|
      lines << "## `#{entry[:dto]}`"
      lines << ""
      lines << "- Generated contract present: `#{entry[:generated_contract_present]}`"
      entry[:fields].select { |field| field[:drift_category] != "used" }.first(20).each do |field|
        lines << "- `#{field[:field]}` -> `#{field[:drift_category]}` | usage=`#{field[:frontend_usage_count]}`"
      end
      lines << ""
    end
    lines.join("\n")
  end

  def terminal_summary(report)
    lines = []
    lines << "API contract drift audit"
    lines << "  backend DTOs scanned: #{report[:dto_count]}"
    lines << "  DTOs missing contracts: #{report[:missing_contract_count]}"
    lines << "  zero-usage fields: #{report[:zero_usage_field_count]}"
    report[:dtos].first(10).each do |entry|
      suspicious = entry[:fields].count { |field| field[:drift_category] != "used" }
      lines << "  - #{entry[:dto]} suspicious_fields=#{suspicious}"
    end
    lines.join("\n")
  end
end

ApiContractDriftAudit.run
