#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "../audit_support"

module ReadSurfaceInventory
  extend self

  READ_PREFIXES = %w[get find search list load open read resolve lookup build to].freeze
  MUTATION_PREFIXES = %w[create update delete apply approve decline withdraw accept block start complete confirm reject save persist].freeze

  MethodEntry = Struct.new(:name, :return_type, :annotations, :body, keyword_init: true)

  def run
    services = AuditSupport.repo_glob("apps/themuffinman/src/main/java/com/themuffinman/app/*/service/*.java")
    entries = services.flat_map { |path| inventory_for_service(path) }
    report = {
      generated_at: Time.now.utc.iso8601,
      service_count: services.size,
      read_surface_count: entries.size,
      read_surfaces: entries,
      transaction_relevant_read_surface_count: entries.count { |entry| entry[:transaction_relevant] },
      missing_read_only_transaction_count: entries.count { |entry| entry[:transaction_relevant] && !entry[:transaction_read_only] }
    }
    AuditSupport.write_json("docs/audit-output/read-surface-inventory.json", report)
    AuditSupport.write_text("docs/audit-output/read-surface-inventory-summary.md", markdown_summary(report))
    puts terminal_summary(report)
  end

  def inventory_for_service(path)
    content = AuditSupport.read(path)
    relative_path = AuditSupport.relative_path(path)
    class_annotations = content.lines.take_while { |line| !line.include?("class ") }.join
    class_read_only = class_annotations.include?("@Transactional(readOnly = true)")
    repositories = content.scan(/private final ([A-Za-z0-9_<>]+Repository) ([a-zA-Z0-9_]+);/).map { |type, name| [name, type] }.to_h
    helpers = content.scan(/private final ([A-Za-z0-9_<>]+(?:Mgr|Assembler|Helper)) ([a-zA-Z0-9_]+);/).map { |type, name| [name, type] }.to_h
    methods = extract_methods(content)
    method_lookup = methods.to_h { |method| [method.name, method] }

    methods.each_with_object([]) do |method, entries|
      next unless read_surface_candidate?(method, class_read_only)

      used_repositories = repositories.keys.select { |field| method.body.include?("#{field}.") }.map { |field| repositories[field] }
      used_helpers = helpers.keys.select { |field| method.body.include?("#{field}.") }.map { |field| helpers[field] }
      dto_types = method.body.scan(/([A-Z][A-Za-z0-9_]*DTO)\.builder/).flatten
      dto_types << method.return_type if method.return_type.include?("DTO")
      mixed_side_effect_flow = mixed_side_effect_flow?(method, method_lookup)
      entries << {
        service: File.basename(path, ".java"),
        path: relative_path,
        domain: AuditSupport.domain_for_path(relative_path),
        method: method.name,
        return_type: method.return_type,
        read_like: true,
        transaction_relevant: transaction_relevant?(path, method, used_repositories, dto_types, mixed_side_effect_flow),
        transaction_read_only: transaction_read_only?(method.annotations, class_read_only),
        transaction_source: transaction_source(method.annotations, class_read_only),
        mixed_side_effect_flow: mixed_side_effect_flow,
        repositories: used_repositories.uniq.sort,
        helper_types: used_helpers.uniq.sort,
        dto_types: dto_types.uniq.sort,
        line_hints: method.body.lines.first(3).map(&:strip).reject(&:empty?).first(2)
      }
    end
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

      return_type = signature_match[1].strip
      method_name = signature_match[2]
      body_lines, new_index = extract_body(lines, index)
      methods << MethodEntry.new(
        name: method_name,
        return_type: return_type,
        annotations: pending_annotations.dup,
        body: body_lines.join
      )
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

  def read_like_method?(method_name)
    lowered = method_name.downcase
    return false if MUTATION_PREFIXES.any? { |prefix| lowered.start_with?(prefix) }

    READ_PREFIXES.any? { |prefix| lowered.start_with?(prefix) }
  end

  def read_surface_candidate?(method, class_read_only)
    return false unless read_like_method?(method.name)

    annotations = method.annotations
    non_read_only_transaction = annotations.any? { |annotation| annotation == "@Transactional" }
    lowered_body = method.body.downcase
    return false if method.name.downcase.include?("mutation")
    return false if non_read_only_transaction && !class_read_only
    return false if lowered_body.include?(".save(") || lowered_body.include?(".delete(")
    return false if lowered_body.include?("notif") || lowered_body.include?("setstatus(")

    true
  end

  def transaction_read_only?(annotations, class_read_only)
    return true if annotations.any? { |annotation| annotation.include?("@Transactional(readOnly = true)") }
    return false if annotations.any? { |annotation| annotation == "@Transactional" }

    class_read_only
  end

  def transaction_source(annotations, class_read_only)
    return "method" if annotations.any? { |annotation| annotation.include?("@Transactional(readOnly = true)") }
    return "method_non_read_only" if annotations.any? { |annotation| annotation == "@Transactional" }
    return "class" if class_read_only

    "none"
  end

  def transaction_relevant?(path, method, repositories, dto_types, mixed_side_effect_flow)
    service_name = File.basename(path, ".java")
    return false if service_name.end_with?("Helper", "Assembler", "Client")
    return false if service_name.include?("AccessPolicy")
    return false if mixed_side_effect_flow
    return false if pure_dto_assembly_helper?(method, repositories, dto_types)

    return true unless repositories.empty?
    return true if !dto_types.empty? && !service_name.end_with?("Helper", "Assembler")
    return true if method.return_type.include?("DTO") && !service_name.end_with?("Helper", "Assembler")

    false
  end

  def mixed_side_effect_flow?(method, method_lookup)
    return true if explicit_side_effect_signal?(method.body)

    invoked_method_names(method.body).any? do |invoked_name|
      invoked_method = method_lookup[invoked_name]
      invoked_method && explicit_side_effect_signal?(invoked_method.body)
    end
  end

  def explicit_side_effect_signal?(body)
    lowered = body.downcase
    return true if lowered.include?(".save(") || lowered.include?(".delete(")

    %w[record persist publish notify track incrementandget].any? { |signal| lowered.include?(signal) }
  end

  def invoked_method_names(body)
    body.scan(/\b([a-z][A-Za-z0-9_]*)\s*\(/).flatten.uniq
  end

  def pure_dto_assembly_helper?(method, repositories, dto_types)
    return false unless method.name.start_with?("to")
    return false unless method.return_type.include?("DTO")
    return false unless repositories.empty?

    !dto_types.empty?
  end

  def markdown_summary(report)
    lines = []
    lines << "# Read Surface Inventory"
    lines << ""
    lines << "- Generated at: `#{report[:generated_at]}`"
    lines << "- Read surfaces: `#{report[:read_surface_count]}`"
    lines << "- Transaction-relevant read surfaces: `#{report[:transaction_relevant_read_surface_count]}`"
    lines << "- Missing explicit or inherited read-only coverage: `#{report[:missing_read_only_transaction_count]}`"
    lines << ""
    grouped = report[:read_surfaces].group_by { |entry| entry[:service] }.sort_by { |service, entries| [-entries.size, service] }
    lines << "## Top services"
    lines << ""
    grouped.first(8).each do |service, entries|
      lines << "- `#{service}`: `#{entries.size}` surfaces, tx-relevant=`#{entries.count { |entry| entry[:transaction_relevant] }}`"
      entries.first(3).each do |entry|
        lines << "  - `#{entry[:method]}` -> `#{entry[:return_type]}` | tx=`#{entry[:transaction_source]}` | repos=#{format_inline(entry[:repositories])} | dto=#{format_inline(entry[:dto_types])}"
      end
    end
    remaining = grouped.size - 8
    lines << "- ... #{remaining} more services" if remaining.positive?
    lines << ""
    lines << "## Read surface sample"
    lines << ""
    report[:read_surfaces].first(10).each do |entry|
      lines << "- `#{entry[:service]}.#{entry[:method]}` -> `#{entry[:return_type]}` | tx=`#{entry[:transaction_source]}` | relevant=`#{entry[:transaction_relevant]}`"
    end
    lines.join("\n")
  end

  def terminal_summary(report)
    lines = []
    lines << "Read surface inventory"
    lines << "  services scanned: #{report[:service_count]}"
    lines << "  read surfaces: #{report[:read_surface_count]}"
    lines << "  transaction-relevant: #{report[:transaction_relevant_read_surface_count]}"
    lines << "  read-like without readOnly tx: #{report[:missing_read_only_transaction_count]}"
    report[:read_surfaces].first(10).each do |entry|
      lines << "  - #{entry[:service]}.#{entry[:method]} -> #{entry[:return_type]} (tx=#{entry[:transaction_source]})"
    end
    lines.join("\n")
  end

  def format_inline(items)
    return "none" if items.empty?

    items.join(",")
  end
end

ReadSurfaceInventory.run
