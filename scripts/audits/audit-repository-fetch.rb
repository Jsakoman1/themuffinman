#!/usr/bin/env ruby
# frozen_string_literal: true

require "set"
require_relative "../local_tooling_common"

module RepositoryFetchAudit
  extend self

  NON_RELATION_GETTERS = %w[first value id ids size class empty blank present].freeze

  RepositoryMethod = Struct.new(
    :repository,
    :path,
    :domain,
    :method,
    :return_type,
    :returned_entity,
    :annotations,
    keyword_init: true
  )

  CallerMethod = Struct.new(:name, :body, keyword_init: true)

  def run
    repository_paths = LocalToolingCommon.repo_glob("apps/themuffinman/src/main/java/com/themuffinman/app/*/repository/*.java")
    main_java_paths = LocalToolingCommon.repo_glob("apps/themuffinman/src/main/java/com/themuffinman/app/**/*.java")
    repository_methods = repository_paths.flat_map { |path| extract_repository_methods(path) }
    callsites = collect_callsites(main_java_paths, repository_methods)
    entries = repository_methods.map { |method| build_entry(method, callsites[callsite_key(method)] || []) }

    report = {
      generated_at: Time.now.utc.iso8601,
      repository_count: repository_paths.size,
      repository_method_count: repository_methods.size,
      high_risk_count: entries.count { |entry| entry[:risk_level] == "high" },
      medium_risk_count: entries.count { |entry| entry[:risk_level] == "medium" },
      repository_methods: entries.sort_by { |entry| [risk_rank(entry[:risk_level]), entry[:repository], entry[:method]] }
    }

    LocalToolingCommon.write_json("docs/generated/local-tooling/repository-fetch-audit.json", report)
    LocalToolingCommon.write_text("docs/generated/local-tooling/repository-fetch-audit-summary.md", markdown_summary(report))
    puts terminal_summary(report)
  end

  def extract_repository_methods(path)
    content = LocalToolingCommon.read(path)
    relative_path = LocalToolingCommon.relative_path(path)
    repository = File.basename(path, ".java")
    methods = []
    pending_annotations = []

    content.lines.each do |line|
      stripped = line.strip
      if stripped.start_with?("@")
        pending_annotations << stripped
        next
      end

      signature_match = stripped.match(/^(?:public\s+)?([A-Za-z0-9_<>, ?\[\]]+)\s+([a-zA-Z0-9_]+)\s*\([^;]*\)\s*;/)
      unless signature_match
        if pending_annotations.any? && !stripped.empty?
          pending_annotations << stripped
        else
          pending_annotations.clear
        end
        next
      end

      return_type = signature_match[1].strip
      methods << RepositoryMethod.new(
        repository: repository,
        path: relative_path,
        domain: LocalToolingCommon.domain_for_path(relative_path),
        method: signature_match[2],
        return_type: return_type,
        returned_entity: extract_returned_entity(return_type),
        annotations: pending_annotations.dup
      )
      pending_annotations.clear
    end

    methods
  end

  def extract_returned_entity(return_type)
    return nil if return_type.match?(/\A(?:void|boolean|int|long|double|float|short|byte|char|String|Integer|Long|Double|Float|Short|Byte|Character|BigDecimal)\z/)
    return nil if return_type.include?("Page<") || return_type.include?("Slice<") || return_type.include?("Map<")

    generic_match = return_type.match(/(?:Optional|List|Set|Collection|Iterable)\s*<\s*([A-Z][A-Za-z0-9_]*)\s*>/)
    if generic_match
      entity = generic_match[1]
      return nil if entity.end_with?("DTO", "Row")

      return entity
    end

    direct_match = return_type.match(/\A([A-Z][A-Za-z0-9_]*)\z/)
    return nil unless direct_match

    entity = direct_match[1]
    return nil if entity.end_with?("DTO", "Row")

    entity
  end

  def collect_callsites(java_paths, repository_methods)
    repository_index = repository_methods.group_by { |method| method.repository }
    callsites = Hash.new { |hash, key| hash[key] = [] }

    java_paths.each do |path|
      next if path.include?("/repository/")

      content = LocalToolingCommon.read(path)
      repository_fields = content.scan(/private final ([A-Za-z0-9_]+Repository) ([a-zA-Z0-9_]+);/)
      next if repository_fields.empty?

      helper_fields = content.scan(/private final ([A-Za-z0-9_]+(?:Mgr|Assembler|Helper)) ([a-zA-Z0-9_]+);/).to_h
      extract_body_methods(content).each do |method|
        repository_fields.each do |repository_type, repository_field|
          method.body.scan(/#{Regexp.escape(repository_field)}\.([a-zA-Z0-9_]+)\s*\(/).flatten.each do |repository_method_name|
            repository_method = repository_index.fetch(repository_type, []).find { |entry| entry.method == repository_method_name }
            next unless repository_method

            helper_calls = helper_fields.keys.select { |field| method.body.include?("#{field}.") }.map { |field| helper_fields[field] }
            dereferences = method.body.scan(/get([A-Z][A-Za-z0-9_]*)\(\)\./).flatten
              .map { |name| uncapitalize(name) }
              .reject { |name| NON_RELATION_GETTERS.include?(name) }
              .uniq
              .sort
            callsites[callsite_key(repository_method)] << {
              file: LocalToolingCommon.relative_path(path),
              caller_class: File.basename(path, ".java"),
              caller_method: method.name,
              caller_classification: classify_caller(path, method.name),
              helper_types: helper_calls.uniq.sort,
              relation_dereferences: dereferences,
              body_signals: callsite_signals(method.body)
            }
          end
        end
      end
    end

    callsites
  end

  def extract_body_methods(content)
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

      body_lines, new_index = extract_body(lines, index)
      methods << CallerMethod.new(name: signature_match[2], body: body_lines.join)
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

  def build_entry(method, callsites)
    fetch_relations = fetch_relations_for(method.annotations)
    likely_lazy_relations = callsites.flat_map { |callsite| callsite[:relation_dereferences] }.uniq.sort - fetch_relations
    risk_level = risk_level_for(method, fetch_relations, callsites, likely_lazy_relations)

    {
      repository: method.repository,
      path: method.path,
      domain: method.domain,
      method: method.method,
      return_type: method.return_type,
      returned_entity: method.returned_entity,
      query_style: query_style(method.annotations),
      fetch_relations: fetch_relations,
      fetch_coverage: fetch_relations.empty? ? "none_detected" : "explicit_fetch",
      caller_count: callsites.size,
      likely_lazy_relations_touched_downstream: likely_lazy_relations,
      risk_level: risk_level,
      high_risk_call_chains: callsites.select { |callsite| high_risk_callsite?(callsite, fetch_relations) }.map do |callsite|
        {
          caller: "#{callsite[:caller_class]}.#{callsite[:caller_method]}",
          file: callsite[:file],
          classification: callsite[:caller_classification],
          helper_types: callsite[:helper_types],
          likely_lazy_relations: callsite[:relation_dereferences] - fetch_relations,
          body_signals: callsite[:body_signals]
        }
      end,
      callers: callsites.sort_by { |callsite| [caller_rank(callsite[:caller_classification]), callsite[:file], callsite[:caller_method]] }
    }
  end

  def fetch_relations_for(annotations)
    annotation_blob = annotations.join("\n")
    query_text = extract_query_text(annotation_blob)
    fetches = query_text.scan(/(?:left\s+join\s+fetch|join\s+fetch)\s+[A-Za-z0-9_]+\.(\w+)/i).flatten

    if annotation_blob.include?("@EntityGraph")
      annotation_blob.scan(/attributePaths\s*=\s*\{([^}]*)\}/m).flatten.each do |paths_blob|
        fetches.concat(paths_blob.scan(/"([^"]+)"/).flatten.map { |path| path.split(".").first })
      end
      annotation_blob.scan(/attributePaths\s*=\s*"([^"]+)"/).flatten.each do |path|
        fetches << path.split(".").first
      end
    end

    fetches.uniq.sort
  end

  def extract_query_text(annotation_blob)
    heredoc_match = annotation_blob.match(/@Query\(\s*"""(.*?)"""/m)
    return heredoc_match[1] if heredoc_match

    string_match = annotation_blob.match(/@Query\(\s*"([^"]*)"/m)
    return string_match[1] if string_match

    ""
  end

  def query_style(annotations)
    annotation_blob = annotations.join("\n")
    return "entity_graph" if annotation_blob.include?("@EntityGraph")
    return "native_query" if annotation_blob.include?("nativeQuery = true")
    return "jpql_query" if annotation_blob.include?("@Query")

    "derived_query"
  end

  def risk_level_for(method, fetch_relations, callsites, likely_lazy_relations)
    return "low" if method.returned_entity.nil?
    return "low" if query_style(method.annotations) == "native_query"
    return "low" if callsites.empty?
    return "high" if callsites.any? { |callsite| high_risk_callsite?(callsite, fetch_relations) }
    return "high" if fetch_relations.empty? && callsites.any? { |callsite| callsite[:helper_types].any? } && !likely_lazy_relations.empty?
    return "medium" if fetch_relations.empty? && read_oriented_callsite?(callsites)
    return "medium" if !fetch_relations.empty? && !likely_lazy_relations.empty?
    return "medium" if fetch_relations.empty? && !likely_lazy_relations.empty?

    "low"
  end

  def read_oriented_callsite?(callsites)
    callsites.any? { |callsite| %w[controller_facing read_oriented].include?(callsite[:caller_classification]) }
  end

  def high_risk_callsite?(callsite, fetch_relations)
    %w[controller_facing read_oriented].include?(callsite[:caller_classification]) &&
      !(callsite[:relation_dereferences] - fetch_relations).empty?
  end

  def callsite_signals(body)
    signals = []
    signals << "stream_mapping" if body.include?(".stream()")
    signals << "dto_builder" if body.include?("DTO.builder()")
    signals << "mapper_or_assembler" if body.match?(/[a-zA-Z0-9_]+(?:Mgr|Assembler|Helper)\.[a-zA-Z0-9_]+\(/)
    signals << "relation_dereference" if body.match?(/get[A-Z][A-Za-z0-9_]*\(\)\./)
    signals.uniq.sort
  end

  def classify_caller(path, method_name)
    return "controller_facing" if path.include?("/controller/")

    lowered = method_name.downcase
    return "mutating" if lowered.match?(/\A(create|update|delete|apply|approve|decline|withdraw|accept|block|start|complete|confirm|reject|save|sync)/)
    return "read_oriented" if lowered.match?(/\A(get|find|search|list|open|read|resolve|lookup|build|to)/)

    "supporting"
  end

  def callsite_key(method)
    "#{method.repository}##{method.method}"
  end

  def markdown_summary(report)
    lines = []
    lines << "# Repository Fetch Audit"
    lines << ""
    lines << "- Generated at: `#{report[:generated_at]}`"
    lines << "- Repository methods scanned: `#{report[:repository_method_count]}`"
    lines << "- High risk: `#{report[:high_risk_count]}`"
    lines << "- Medium risk: `#{report[:medium_risk_count]}`"
    lines << ""
    lines << "## High-risk and medium-risk methods"
    lines << ""
    report[:repository_methods].first(12).each do |entry|
      lines << "- `#{entry[:repository]}.#{entry[:method]}` | risk=`#{entry[:risk_level]}` | query=`#{entry[:query_style]}` | fetch=#{format_inline(entry[:fetch_relations])} | lazy=#{format_inline(entry[:likely_lazy_relations_touched_downstream])}"
      entry[:high_risk_call_chains].first(3).each do |chain|
        lines << "  - `#{chain[:caller]}` in `#{chain[:file]}` | helper=#{format_inline(chain[:helper_types])} | lazy=#{format_inline(chain[:likely_lazy_relations])}"
      end
    end
    remaining = report[:repository_methods].size - 12
    lines << "- ... #{remaining} more methods" if remaining.positive?
    lines << ""
    lines << "## High-risk sample"
    lines << ""
    report[:repository_methods].select { |entry| entry[:risk_level] == "high" }.first(5).each do |entry|
      lines << "- `#{entry[:repository]}.#{entry[:method]}` -> `#{entry[:returned_entity] || 'non-entity'}`"
    end
    lines.join("\n")
  end

  def terminal_summary(report)
    lines = []
    lines << "Repository fetch audit"
    lines << "  repositories scanned: #{report[:repository_count]}"
    lines << "  repository methods: #{report[:repository_method_count]}"
    lines << "  high risk: #{report[:high_risk_count]}"
    lines << "  medium risk: #{report[:medium_risk_count]}"
    report[:repository_methods].first(10).each do |entry|
      lines << "  - #{entry[:repository]}.#{entry[:method]} risk=#{entry[:risk_level]} fetch=#{entry[:fetch_relations].join(',')}"
    end
    lines.join("\n")
  end

  def risk_rank(level)
    { "high" => 0, "medium" => 1, "low" => 2 }.fetch(level, 3)
  end

  def caller_rank(level)
    { "controller_facing" => 0, "read_oriented" => 1, "mutating" => 2, "supporting" => 3 }.fetch(level, 4)
  end

  def format_inline(items)
    return "none" if items.empty?

    items.map { |item| "`#{item}`" }.join(", ")
  end

  def uncapitalize(value)
    value[0].downcase + value[1..]
  end
end

RepositoryFetchAudit.run
