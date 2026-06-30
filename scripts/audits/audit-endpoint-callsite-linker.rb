#!/usr/bin/env ruby
# frozen_string_literal: true

require "set"
require_relative "../local_tooling_common"

module EndpointCallsiteLinker
  extend self

  Endpoint = Struct.new(:controller, :path, :http_method, :full_path, :controller_method, :file, :domain, keyword_init: true)
  FrontendClientMethod = Struct.new(:client_name, :client_object, :method_name, :http_method, :path, :file, keyword_init: true)

  HTTP_CALLS = {
    "get" => "GET",
    "post" => "POST",
    "put" => "PUT",
    "patch" => "PATCH",
    "delete" => "DELETE"
  }.freeze

  def run
    endpoint_files = LocalToolingCommon.repo_glob("apps/themuffinman/src/main/java/com/themuffinman/app/*/controller/*Controller.java")
    frontend_api_files = LocalToolingCommon.repo_glob("apps/themuffinman/frontend/src/modules/**/*Api.ts")
    frontend_source_files = LocalToolingCommon.repo_glob(
      "apps/themuffinman/frontend/src/modules/**/*.ts",
      "apps/themuffinman/frontend/src/modules/**/*.vue"
    )

    endpoints = endpoint_files.flat_map { |path| extract_endpoints(path) }
    client_methods = frontend_api_files.flat_map { |path| extract_frontend_client_methods(path) }
    import_graph = build_reverse_import_graph(frontend_source_files)
    callsite_index = build_callsite_index(frontend_source_files, client_methods, import_graph)
    entries = endpoints.map { |endpoint| build_entry(endpoint, client_methods, callsite_index) }

    report = {
      generated_at: Time.now.utc.iso8601,
      endpoint_count: endpoints.size,
      frontend_client_method_count: client_methods.size,
      linked_endpoint_count: entries.count { |entry| !entry[:frontend_matches].empty? },
      unlinked_endpoint_count: entries.count { |entry| entry[:frontend_matches].empty? },
      endpoints: entries.sort_by { |entry| [entry[:frontend_matches].empty? ? 0 : 1, entry[:full_path], entry[:http_method]] }
    }

    LocalToolingCommon.write_json("docs/generated/local-tooling/endpoint-callsite-linker.json", report)
    LocalToolingCommon.write_text("docs/generated/local-tooling/endpoint-callsite-linker-summary.md", markdown_summary(report))
    puts terminal_summary(report)
  end

  def extract_endpoints(path)
    content = LocalToolingCommon.read(path)
    relative_path = LocalToolingCommon.relative_path(path)
    controller = File.basename(path, ".java")
    class_base = normalize_backend_path(content[/@RequestMapping\("([^"]+)"\)/, 1] || "")
    pending_annotations = []
    endpoints = []

    content.lines.each do |line|
      stripped = line.strip
      if stripped.start_with?("@")
        pending_annotations << stripped
        next
      end

      signature_match = stripped.match(/^public\s+[A-Za-z0-9_<>, ?\[\]]+\s+([a-zA-Z0-9_]+)\s*\(/)
      unless signature_match
        pending_annotations.clear unless stripped.empty?
        next
      end

      mapping = pending_annotations.find { |annotation| annotation.match?(/@(Get|Post|Put|Patch|Delete)Mapping/) }
      if mapping
        http_method = mapping[/@(Get|Post|Put|Patch|Delete)Mapping/, 1].upcase
        sub_path = mapping[/\("([^"]*)"\)/, 1] || ""
        full_path = normalize_backend_path("#{class_base}/#{sub_path}")
        endpoints << Endpoint.new(
          controller: controller,
          path: sub_path,
          http_method: http_method,
          full_path: full_path,
          controller_method: signature_match[1],
          file: relative_path,
          domain: LocalToolingCommon.domain_for_path(relative_path)
        )
      end

      pending_annotations.clear
    end

    endpoints
  end

  def extract_frontend_client_methods(path)
    content = LocalToolingCommon.read(path)
    relative_path = LocalToolingCommon.relative_path(path)
    client_object = content[/export const (\w+) = \{/, 1]
    return [] unless client_object

    client_name = File.basename(path, ".ts")
    methods = []
    lines = content.lines
    index = 0

    while index < lines.length
      signature = lines[index].match(/^\s*async\s+([a-zA-Z0-9_]+)\s*\(/)
      unless signature
        index += 1
        next
      end

      method_name = signature[1]
      body_lines, new_index = extract_body(lines, index)
      body = body_lines.join
      http_method, raw_path = extract_http_call(body)
      if http_method && raw_path
        methods << FrontendClientMethod.new(
          client_name: client_name,
          client_object: client_object,
          method_name: method_name,
          http_method: http_method,
          path: normalize_frontend_path(raw_path),
          file: relative_path
        )
      end
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

  def extract_http_call(body)
    HTTP_CALLS.each do |client_call, http_method|
      match = body.match(/api\.#{client_call}<[^>]+>\((`[^`]+`|"[^"]+")/m) || body.match(/api\.#{client_call}\((`[^`]+`|"[^"]+")/m)
      next unless match

      return [http_method, match[1]]
    end
    nil
  end

  def build_reverse_import_graph(files)
    graph = Hash.new { |hash, key| hash[key] = Set.new }
    files.each do |path|
      relative_path = LocalToolingCommon.relative_path(path)
      extract_relative_imports(path).each do |target|
        graph[target] << relative_path
      end
    end
    graph
  end

  def extract_relative_imports(path)
    content = LocalToolingCommon.read(path)
    source_dir = File.dirname(path)
    content.scan(/from\s+"([^"]+)"|from\s+'([^']+)'/).flatten.compact.map do |import_path|
      next unless import_path.start_with?(".")

      resolve_frontend_import(source_dir, import_path)
    end.compact
  end

  def resolve_frontend_import(source_dir, import_path)
    base = File.expand_path(import_path, source_dir)
    candidates = [
      base,
      "#{base}.ts",
      "#{base}.vue",
      "#{base}/index.ts",
      "#{base}/index.vue"
    ]
    candidate = candidates.find { |value| File.file?(value) }
    candidate && LocalToolingCommon.relative_path(candidate)
  end

  def build_callsite_index(frontend_source_files, client_methods, import_graph)
    index = Hash.new { |hash, key| hash[key] = [] }
    frontend_source_files.each do |path|
      relative_path = LocalToolingCommon.relative_path(path)
      content = LocalToolingCommon.read(path)
      next if relative_path.include?("/api/")

      client_methods.each do |client_method|
        next unless content.include?("#{client_method.client_object}.#{client_method.method_name}")

        index[client_key(client_method)] << {
          file: relative_path,
          category: LocalToolingCommon.path_category(relative_path),
          ui_surfaces: discover_ui_surfaces(relative_path, import_graph)
        }
      end
    end
    index.transform_values do |entries|
      entries.uniq { |entry| entry[:file] }
    end
  end

  def discover_ui_surfaces(start_file, import_graph)
    return [start_file] if ui_surface?(start_file)

    visited = Set.new([start_file])
    queue = [start_file]
    surfaces = Set.new
    depth = 0

    while queue.any? && depth < 5
      next_queue = []
      queue.each do |file|
        import_graph[file].each do |importer|
          next if visited.include?(importer)

          visited << importer
          if ui_surface?(importer)
            surfaces << importer
          else
            next_queue << importer
          end
        end
      end
      queue = next_queue
      depth += 1
    end

    surfaces.to_a.sort
  end

  def ui_surface?(relative_path)
    return true if relative_path.match?(%r{/pages/})
    return true if relative_path.match?(%r{/views/})

    false
  end

  def build_entry(endpoint, client_methods, callsite_index)
    frontend_matches = client_methods.select do |client_method|
      client_method.http_method == endpoint.http_method &&
        paths_match?(endpoint.full_path, client_method.path)
    end

    {
      endpoint_id: "#{endpoint.http_method} #{endpoint.full_path}",
      domain: endpoint.domain,
      backend_controller: endpoint.controller,
      backend_method: endpoint.controller_method,
      backend_file: endpoint.file,
      http_method: endpoint.http_method,
      full_path: endpoint.full_path,
      frontend_matches: frontend_matches.map do |client_method|
        {
          client_object: client_method.client_object,
          client_method: client_method.method_name,
          client_file: client_method.file,
          client_path: client_method.path,
          callsites: (callsite_index[client_key(client_method)] || []).map do |entry|
            {
              file: entry[:file],
              category: entry[:category],
              ui_surfaces: entry[:ui_surfaces]
            }
          end
        }
      end.sort_by { |entry| [entry[:client_file], entry[:client_method]] }
    }
  end

  def paths_match?(backend_path, frontend_path)
    backend_path == frontend_path
  end

  def client_key(client_method)
    "#{client_method.client_object}##{client_method.method_name}"
  end

  def normalize_backend_path(path)
    normalized = "/#{path}".gsub(%r{/+}, "/")
    normalized = normalized.gsub(/\{[^}]+\}/, ":param")
    trim_trailing_slash(normalized)
  end

  def normalize_frontend_path(raw)
    value = raw.delete_prefix('"').delete_suffix('"').delete_prefix("`").delete_suffix("`")
    normalized = value.gsub(/\$\{[^}]+\}/, ":param")
    trim_trailing_slash("/#{normalized}".gsub(%r{/+}, "/"))
  end

  def trim_trailing_slash(path)
    return "/" if path == "/"

    path.sub(%r{/\z}, "")
  end

  def markdown_summary(report)
    lines = []
    lines << "# Endpoint Callsite Linker"
    lines << ""
    lines << "- Generated at: `#{report[:generated_at]}`"
    lines << "- Backend endpoints: `#{report[:endpoint_count]}`"
    lines << "- Frontend client methods: `#{report[:frontend_client_method_count]}`"
    lines << "- Linked endpoints: `#{report[:linked_endpoint_count]}`"
    lines << "- Unlinked endpoints: `#{report[:unlinked_endpoint_count]}`"
    lines << ""
    lines << "## Endpoint sample"
    lines << ""
    report[:endpoints].first(12).each do |entry|
      lines << "- `#{entry[:endpoint_id]}` | backend=`#{entry[:backend_controller]}.#{entry[:backend_method]}` | frontend_matches=#{entry[:frontend_matches].size}"
      if entry[:frontend_matches].empty?
        lines << "  - frontend: _no direct client match detected_"
      else
        entry[:frontend_matches].first(2).each do |match|
          lines << "  - `#{match[:client_object]}.#{match[:client_method]}` in `#{match[:client_file]}`"
          match[:callsites].first(3).each do |callsite|
            lines << "    - `#{callsite[:file]}` -> surfaces=#{format_inline(callsite[:ui_surfaces])}"
          end
        end
      end
    end
    remaining = report[:endpoints].size - 12
    lines << "- ... #{remaining} more endpoints" if remaining.positive?
    lines.join("\n")
  end

  def terminal_summary(report)
    lines = []
    lines << "Endpoint callsite linker"
    lines << "  backend endpoints: #{report[:endpoint_count]}"
    lines << "  frontend client methods: #{report[:frontend_client_method_count]}"
    lines << "  linked endpoints: #{report[:linked_endpoint_count]}"
    lines << "  unlinked endpoints: #{report[:unlinked_endpoint_count]}"
    report[:endpoints].first(10).each do |entry|
      lines << "  - #{entry[:endpoint_id]} frontend_matches=#{entry[:frontend_matches].size}"
    end
    lines.join("\n")
  end

  def format_inline(items)
    return "none" if items.empty?

    items.map { |item| "`#{item}`" }.join(", ")
  end
end

EndpointCallsiteLinker.run
