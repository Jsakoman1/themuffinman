#!/usr/bin/env ruby
# frozen_string_literal: true

require "set"
require_relative "../audit_support"

module MapperUsageAudit
  extend self

  MethodCall = Struct.new(:file, :caller_class, :caller_method, :classification, :mapper_type, :mapper_field, :mapper_method, keyword_init: true)

  def run
    mapper_paths = AuditSupport.repo_glob("apps/themuffinman/src/main/java/com/themuffinman/app/*/mapper/*.java")
    java_paths = AuditSupport.repo_glob("apps/themuffinman/src/main/java/com/themuffinman/app/**/*.java")
    callsites = collect_callsites(java_paths, mapper_paths)
    report = {
      generated_at: Time.now.utc.iso8601,
      mapper_count: mapper_paths.size,
      mapper_usages: mapper_paths.map { |path| mapper_report(path, callsites) }
    }
    AuditSupport.write_json("docs/audit-output/mapper-usage-audit.json", report)
    AuditSupport.write_text("docs/audit-output/mapper-usage-audit-summary.md", markdown_summary(report))
    puts terminal_summary(report)
  end

  def mapper_report(path, callsites)
    content = AuditSupport.read(path)
    relative_path = AuditSupport.relative_path(path)
    mapper_type = File.basename(path, ".java")
    methods = content.scan(/public\s+[A-Za-z0-9_<>, ?\[\]]+\s+([a-zA-Z0-9_]+)\s*\(/).flatten
    relevant_calls = callsites.select { |call| call.mapper_type == mapper_type }
    {
      mapper: mapper_type,
      path: relative_path,
      domain: AuditSupport.domain_for_path(relative_path),
      methods: methods.uniq.sort,
      risk_flags: risk_flags(content),
      usage_count: relevant_calls.size,
      callers: relevant_calls.map do |call|
        {
          file: call.file,
          caller_class: call.caller_class,
          caller_method: call.caller_method,
          classification: call.classification,
          mapper_method: call.mapper_method
        }
      end.sort_by { |entry| [entry[:file], entry[:caller_method], entry[:mapper_method]] }
    }
  end

  def collect_callsites(java_paths, mapper_paths)
    mapper_types = mapper_paths.map { |path| File.basename(path, ".java") }.to_set
    calls = []
    java_paths.each do |path|
      next if path.include?("/mapper/")

      content = AuditSupport.read(path)
      mapper_fields = content.scan(/private final ([A-Za-z0-9_]+) ([a-zA-Z0-9_]+);/).select { |type, _field| mapper_types.include?(type) }
      next if mapper_fields.empty?

      extract_methods(content).each do |method|
        mapper_fields.each do |mapper_type, mapper_field|
          method.body.scan(/#{Regexp.escape(mapper_field)}\.([a-zA-Z0-9_]+)\s*\(/).flatten.each do |mapper_method|
            calls << MethodCall.new(
              file: AuditSupport.relative_path(path),
              caller_class: File.basename(path, ".java"),
              caller_method: method.name,
              classification: classify_caller(path, method.name),
              mapper_type: mapper_type,
              mapper_field: mapper_field,
              mapper_method: mapper_method
            )
          end
        end
      end
    end
    calls
  end

  def extract_methods(content)
    methods = []
    lines = content.lines
    index = 0
    pending_annotations = []
    while index < lines.length
      stripped = lines[index].strip
      if stripped.start_with?("@")
        pending_annotations << stripped
        index += 1
        next
      end

      signature_match = lines[index].match(/^\s*public\s+([A-Za-z0-9_<>, ?\[\]]+)\s+([a-zA-Z0-9_]+)\s*\(/)
      unless signature_match
        pending_annotations.clear unless stripped.empty?
        index += 1
        next
      end

      method_name = signature_match[2]
      body_lines, new_index = extract_body(lines, index)
      methods << Struct.new(:name, :body).new(method_name, body_lines.join)
      pending_annotations.clear
      index = new_index
    end
    methods
  end

  def extract_body(lines, start_index)
    body_lines = []
    brace_depth = 0
    index = start_index
    started = false
    while index < lines.length
      line = lines[index]
      body_lines << line
      brace_depth += line.count("{")
      brace_depth -= line.count("}")
      started ||= line.include?("{")
      index += 1
      break if started && brace_depth.zero?
    end
    [body_lines, index]
  end

  def classify_caller(path, method_name)
    return "controller_facing" if path.include?("/controller/")
    lowered = method_name.downcase
    return "mutating" if lowered.match?(/\A(create|update|delete|apply|approve|decline|withdraw|accept|block|start|complete|confirm|reject)/)
    return "read_oriented" if lowered.match?(/\A(get|find|search|list|open|read|resolve|lookup|build|to)/)

    "supporting"
  end

  def risk_flags(content)
    flags = []
    flags << "service_backed" if content.match?(/private final [A-Z][A-Za-z0-9_]+Service /)
    flags << "rich_text_sanitization" if content.include?("RichTextInputValidator")
    flags << "navigation_logic" if content.include?("NavigationTargetDTO")
    flags << "relation_dereference" if content.match?(/get[A-Z][A-Za-z0-9_]*\(\)\./)
    flags.uniq.sort
  end

  def markdown_summary(report)
    lines = []
    lines << "# Mapper Usage Audit"
    lines << ""
    lines << "- Generated at: `#{report[:generated_at]}`"
    lines << "- Mappers scanned: `#{report[:mapper_count]}`"
    lines << ""
    report[:mapper_usages].sort_by { |entry| [-entry[:usage_count], entry[:mapper]] }.first(8).each do |entry|
      lines << "## `#{entry[:mapper]}`"
      lines << ""
      lines << "- Risk flags: #{format_inline(entry[:risk_flags])}"
      lines << "- Usage count: `#{entry[:usage_count]}`"
      entry[:callers].first(5).each do |caller|
        lines << "- `#{caller[:caller_class]}.#{caller[:caller_method]}` in `#{caller[:file]}` -> `#{caller[:mapper_method]}` (`#{caller[:classification]}`)"
      end
      remaining = entry[:callers].length - 5
      lines << "- ... #{remaining} more callers" if remaining.positive?
      lines << ""
    end
    remaining_mappers = report[:mapper_usages].length - 8
    lines << "- ... #{remaining_mappers} more mappers" if remaining_mappers.positive?
    lines.join("\n")
  end

  def terminal_summary(report)
    lines = []
    lines << "Mapper usage audit"
    lines << "  mappers scanned: #{report[:mapper_count]}"
    report[:mapper_usages].sort_by { |entry| [-entry[:usage_count], entry[:mapper]] }.first(5).each do |entry|
      lines << "  - #{entry[:mapper]} usages=#{entry[:usage_count]} flags=#{entry[:risk_flags].join(',')}"
    end
    lines.join("\n")
  end

  def format_inline(items)
    return "none" if items.empty?

    items.map { |item| "`#{item}`" }.join(", ")
  end
end

MapperUsageAudit.run
