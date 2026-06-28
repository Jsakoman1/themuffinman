#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "set"
require_relative "../local_tooling_common"

module FrontendRouteSurfaceInventory
  extend self

  RouteEntry = Struct.new(:path, :component_symbol, :component_file, :redirect, :requires_auth, :requires_admin, :module_key, keyword_init: true)

  def run
    router_path = File.join(LocalToolingCommon::REPO_ROOT, "apps/themuffinman/frontend/src/router.ts")
    endpoint_linker_path = File.join(LocalToolingCommon::REPO_ROOT, "docs/generated/local-tooling/endpoint-callsite-linker.json")
    endpoint_linker = JSON.parse(File.read(endpoint_linker_path))
    endpoint_links = flatten_endpoint_links(endpoint_linker)
    endpoint_links_by_client_method = endpoint_links.group_by { |link| link[:client_method] }

    frontend_files = LocalToolingCommon.repo_glob(
      "apps/themuffinman/frontend/src/**/*.ts",
      "apps/themuffinman/frontend/src/**/*.vue"
    )
    import_graph = build_reverse_import_graph(frontend_files)
    routes = extract_routes(router_path)

    entries = routes.map do |route|
      build_entry(route, import_graph, endpoint_links, endpoint_links_by_client_method)
    end

    report = {
      generated_at: Time.now.utc.iso8601,
      route_count: entries.size,
      surface_route_count: entries.count { |entry| entry[:surface_file] },
      redirect_route_count: entries.count { |entry| entry[:redirect] },
      placeholder_route_count: entries.count { |entry| entry[:placeholder_module] },
      routes: entries
    }

    LocalToolingCommon.write_json("docs/generated/local-tooling/frontend-route-surface-inventory.json", report)
    LocalToolingCommon.write_text("docs/generated/local-tooling/frontend-route-surface-inventory-summary.md", markdown_summary(report))
    puts terminal_summary(report)
  end

  def extract_routes(router_path)
    content = LocalToolingCommon.read(router_path)
    component_imports = content.scan(/const\s+(\w+)\s*=\s*\(\)\s*=>\s*import\("([^"]+)"\);/).to_h
    route_blocks = content.scan(/\{\s*path:\s*'([^']+)'\s*,(.*?)\n\s*\}/m)

    route_blocks.map do |path, block|
      component_symbol = block[/component:\s*([A-Za-z0-9_]+)/, 1]
      redirect = block[/redirect:\s*'([^']+)'/, 1]
      module_key = block[/moduleKey:\s*'([^']+)'/, 1]
      component_file = component_symbol ? resolve_frontend_component(component_imports[component_symbol]) : nil
      RouteEntry.new(
        path: path,
        component_symbol: component_symbol,
        component_file: component_file,
        redirect: redirect,
        requires_auth: block.include?("requiresAuth: true"),
        requires_admin: block.include?("requiresAdmin: true"),
        module_key: module_key
      )
    end
  end

  def resolve_frontend_component(import_path)
    return nil unless import_path

    absolute = File.expand_path(import_path, File.join(LocalToolingCommon::REPO_ROOT, "apps/themuffinman/frontend/src"))
    candidate = absolute.end_with?(".vue") || absolute.end_with?(".ts") ? absolute : "#{absolute}.vue"
    return nil unless File.file?(candidate)

    LocalToolingCommon.relative_path(candidate)
  end

  def build_reverse_import_graph(frontend_files)
    graph = Hash.new { |hash, key| hash[key] = Set.new }
    frontend_files.each do |path|
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

      resolve_import(source_dir, import_path)
    end.compact
  end

  def resolve_import(source_dir, import_path)
    base = File.expand_path(import_path, source_dir)
    candidates = [
      base,
      "#{base}.ts",
      "#{base}.vue",
      "#{base}/index.ts",
      "#{base}/index.vue"
    ]
    match = candidates.find { |candidate| File.file?(candidate) }
    match && LocalToolingCommon.relative_path(match)
  end

  def flatten_endpoint_links(endpoint_linker)
    links = []
    endpoint_linker.fetch("endpoints", []).each do |endpoint|
      endpoint.fetch("frontend_matches", []).each do |match|
        if match.fetch("callsites", []).empty?
          links << {
            endpoint_id: endpoint["endpoint_id"],
            backend_controller: endpoint["backend_controller"],
            backend_method: endpoint["backend_method"],
            client_object: match["client_object"],
            client_method: match["client_method"],
            client_file: match["client_file"],
            caller_file: nil,
            ui_surfaces: []
          }
        end

        match.fetch("callsites", []).each do |callsite|
          links << {
            endpoint_id: endpoint["endpoint_id"],
            backend_controller: endpoint["backend_controller"],
            backend_method: endpoint["backend_method"],
            client_object: match["client_object"],
            client_method: match["client_method"],
            client_file: match["client_file"],
            caller_file: callsite["file"],
            ui_surfaces: callsite.fetch("ui_surfaces", [])
          }
        end
      end
    end
    links.uniq { |entry| [entry[:endpoint_id], entry[:client_object], entry[:client_method], entry[:caller_file]] }
  end

  def build_entry(route, import_graph, endpoint_links, endpoint_links_by_client_method)
    primary_composables = route.component_file ? discover_primary_composables(route.component_file, import_graph) : []
    reachable_files = route.component_file ? discover_reachable_files(route.component_file) : Set.new
    matched_links = endpoint_links.select do |link|
      link[:ui_surfaces].include?(route.component_file) || reachable_files.include?(link[:caller_file])
    end
    used_client_methods = scan_used_client_methods(reachable_files)
    used_client_methods.each do |client_method|
      matched_links.concat(endpoint_links_by_client_method.fetch(client_method, []))
    end
    matched_links = matched_links.uniq { |link| [link[:endpoint_id], link[:client_object], link[:client_method], link[:caller_file]] }
    api_clients = matched_links.map { |link| "#{link[:client_object]}.#{link[:client_method]}" }.uniq.sort
    backend_endpoints = matched_links.map { |link| link[:endpoint_id] }.uniq.sort

    {
      route_path: route.path,
      redirect: route.redirect,
      surface_file: route.component_file,
      component_symbol: route.component_symbol,
      requires_auth: route.requires_auth,
      requires_admin: route.requires_admin,
      placeholder_module: !route.module_key.nil?,
      module_key: route.module_key,
      primary_composables: primary_composables,
      api_clients: api_clients,
      backend_endpoints: backend_endpoints,
      endpoint_links: matched_links.sort_by { |link| [link[:client_file], link[:client_method], link[:endpoint_id]] }
    }
  end

  def discover_primary_composables(surface_file, import_graph)
    visited = Set.new([surface_file])
    queue = [surface_file]
    composables = Set.new
    depth = 0

    while queue.any? && depth < 3
      next_queue = []
      queue.each do |file|
        absolute = File.join(LocalToolingCommon::REPO_ROOT, file)
        next unless File.file?(absolute)

        extract_relative_imports(absolute).each do |imported|
          next if visited.include?(imported)

          visited << imported
          if imported.include?("/composables/")
            composables << imported
          else
            next_queue << imported if imported.end_with?(".ts", ".vue")
          end
        end
      end
      queue = next_queue
      depth += 1
    end

    composables.to_a.sort
  end

  def discover_reachable_files(surface_file)
    visited = Set.new([surface_file])
    queue = [surface_file]
    depth = 0

    while queue.any? && depth < 4
      next_queue = []
      queue.each do |file|
        absolute = File.join(LocalToolingCommon::REPO_ROOT, file)
        next unless File.file?(absolute)

        extract_relative_imports(absolute).each do |imported|
          next if visited.include?(imported)

          visited << imported
          next_queue << imported
        end
      end
      queue = next_queue
      depth += 1
    end

    visited
  end

  def scan_used_client_methods(reachable_files)
    methods = Set.new
    reachable_files.each do |file|
      absolute = File.join(LocalToolingCommon::REPO_ROOT, file)
      next unless File.file?(absolute)

      content = LocalToolingCommon.read(absolute)
      content.scan(/\.([a-zA-Z0-9_]+)\s*\(/).flatten.each do |method_name|
        methods << method_name
      end
    end
    methods
  end

  def ui_surface?(relative_path)
    relative_path.include?("/views/") || relative_path.include?("/pages/") || relative_path == "apps/themuffinman/frontend/src/views/ModulePlaceholderView.vue"
  end

  def markdown_summary(report)
    lines = []
    lines << "# Frontend Route Surface Inventory"
    lines << ""
    lines << "- Generated at: `#{report[:generated_at]}`"
    lines << "- Routes: `#{report[:route_count]}`"
    lines << "- Routes with concrete surfaces: `#{report[:surface_route_count]}`"
    lines << "- Redirect routes: `#{report[:redirect_route_count]}`"
    lines << "- Placeholder module routes: `#{report[:placeholder_route_count]}`"
    lines << ""
    report[:routes].each do |entry|
      lines << "## `#{entry[:route_path]}`"
      lines << ""
      lines << "- Surface: #{entry[:surface_file] ? "`#{entry[:surface_file]}`" : "_none_"}"
      lines << "- Redirect: #{entry[:redirect] ? "`#{entry[:redirect]}`" : "_none_"}"
      lines << "- Primary composables: #{format_inline(entry[:primary_composables])}"
      lines << "- API clients: #{format_inline(entry[:api_clients])}"
      lines << "- Backend endpoints: #{format_inline(entry[:backend_endpoints])}"
      lines << ""
    end
    lines.join("\n")
  end

  def terminal_summary(report)
    lines = []
    lines << "Frontend route surface inventory"
    lines << "  routes: #{report[:route_count]}"
    lines << "  concrete surfaces: #{report[:surface_route_count]}"
    lines << "  redirects: #{report[:redirect_route_count]}"
    lines << "  placeholders: #{report[:placeholder_route_count]}"
    report[:routes].first(10).each do |entry|
      lines << "  - #{entry[:route_path]} surface=#{entry[:surface_file] || 'none'} apis=#{entry[:api_clients].size} endpoints=#{entry[:backend_endpoints].size}"
    end
    lines.join("\n")
  end

  def format_inline(items)
    return "none" if items.empty?

    items.map { |item| "`#{item}`" }.join(", ")
  end
end

FrontendRouteSurfaceInventory.run
