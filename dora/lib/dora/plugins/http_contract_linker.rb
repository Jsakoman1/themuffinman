# frozen_string_literal: true

module Dora
  module Plugins
    class HttpContractLinker
      HTTP = {"get" => "GET", "post" => "POST", "put" => "PUT", "patch" => "PATCH", "delete" => "DELETE"}.freeze

      def self.analyze!(root:, inputs:)
        result = {
          "endpoint_links" => endpoint_links!(root: root, controller_glob: inputs.fetch("controller_glob"), client_glob: inputs.fetch("client_glob"))
        }
        if inputs["dto_glob"] || inputs["contract_path"] || inputs["frontend_glob"]
          %w[dto_glob contract_path frontend_glob].each { |field| fail!("HTTP contract input is missing #{field}") unless inputs[field].is_a?(String) && !inputs[field].empty? }
          result["dto_drift"] = dto_drift!(root: root, dto_glob: inputs.fetch("dto_glob"), contract_path: inputs.fetch("contract_path"), frontend_glob: inputs.fetch("frontend_glob"))
        end
        result
      end

      def self.dto_drift!(root:, dto_glob:, contract_path:, frontend_glob:)
        root = File.expand_path(root)
        validate_paths!(dto_glob, contract_path, frontend_glob)
        dto_fields = Dir[File.join(root, dto_glob)].select { |path| File.file?(path) }.to_h do |path|
          [File.basename(path, ".java"), File.read(path).scan(/^\s*private\s+(?!static\b)[A-Za-z0-9_<>, ?\[\]]+\s+([a-zA-Z0-9_]+);/).flatten.uniq.sort]
        end
        contracts = interfaces(File.read(File.join(root, contract_path)))
        usage = Hash.new(0)
        Dir[File.join(root, frontend_glob)].select { |path| File.file?(path) && path != File.join(root, contract_path) }.each do |path|
          File.read(path).scan(/\.([a-zA-Z0-9_]+)\b|\[['"]([a-zA-Z0-9_]+)['"]\]/).each { |dot, bracket| usage[dot || bracket] += 1 }
        end
        dto_fields.keys.sort.map do |dto|
          fields = dto_fields[dto]
          contract = contracts.fetch(dto, [])
          {"dto" => dto, "generated_contract_present" => contracts.key?(dto), "fields" => fields.map do |field|
            {"field" => field, "in_generated_contract" => contract.include?(field), "frontend_usage_count" => usage[field], "drift_category" => !contract.include?(field) ? "missing_in_generated_contract" : usage[field].zero? ? "unused_in_frontend" : "used"}
          end}
        end
      end

      def self.endpoint_links!(root:, controller_glob:, client_glob:)
        root = File.expand_path(root)
        validate_paths!(controller_glob, client_glob)
        endpoints = Dir[File.join(root, controller_glob)].select { |path| File.file?(path) }.flat_map { |path| endpoints_in(path, root) }
        clients = Dir[File.join(root, client_glob)].select { |path| File.file?(path) }.flat_map { |path| clients_in(path, root) }
        endpoints.map do |endpoint|
          matches = clients.select { |client| client["method"] == endpoint["method"] && paths_match?(client["path"], endpoint["path"]) }
          endpoint.merge("client_matches" => matches)
        end
      end

      def self.interfaces(content)
        content.scan(/export interface ([A-Za-z0-9_]+) \{(.*?)^\}/m).to_h do |name, body|
          [name, body.scan(/^\s*([a-zA-Z0-9_?]+):/).flatten.map { |field| field.delete_suffix("?") }.uniq.sort]
        end
      end
      private_class_method :interfaces

      def self.endpoints_in(path, root)
        content = File.read(path)
        base = normalize(content[/@RequestMapping\("([^"]+)"\)/, 1].to_s)
        annotations = []
        content.lines.each_with_object([]) do |line, result|
          stripped = line.strip
          if stripped.start_with?("@")
            annotations << stripped
            next
          end
          signature = stripped.match(/^public\s+.*?\s+([a-zA-Z0-9_]+)\s*\(/)
          unless signature
            annotations.clear unless stripped.empty?
            next
          end
          mapping = annotations.find { |item| item.match?(/@(Get|Post|Put|Patch|Delete)Mapping/) }
          if mapping
            verb = mapping[/@(Get|Post|Put|Patch|Delete)Mapping/, 1].upcase
            suffix = mapping[/\("([^"]*)"\)/, 1].to_s
            result << {"method" => verb, "path" => normalize("#{base}/#{suffix}"), "file" => relative(path, root), "handler" => signature[1]}
          end
          annotations.clear
        end
      end
      private_class_method :endpoints_in

      def self.clients_in(path, root)
        content = File.read(path)
        HTTP.flat_map do |call, method|
          content.scan(/api\.#{call}(?:<[^>]+>)?\((`[^`]+`|"[^"]+")/m).map do |raw|
            {"method" => method, "path" => normalize(raw.first[1..-2].gsub(/\$\{[^}]+\}/, ":value")), "file" => relative(path, root)}
          end
        end
      end
      private_class_method :clients_in

      def self.paths_match?(left, right)
        normalize(left).gsub(/:[^\/]+/, ":value") == normalize(right).gsub(/\{[^}]+\}/, ":value")
      end
      private_class_method :paths_match?

      def self.normalize(path)
        "/#{path}".gsub(%r{/+}, "/").sub(%r{/$}, "")
      end
      private_class_method :normalize

      def self.relative(path, root)
        path.delete_prefix("#{root}/")
      end
      private_class_method :relative

      def self.validate_paths!(*paths)
        paths.flatten.each { |path| fail!("declared path is invalid") unless path.is_a?(String) && !path.empty? && !path.start_with?("/") && !path.split("/").include?("..") }
      end
      private_class_method :validate_paths!

      def self.fail!(message)
        raise ArgumentError, message
      end
      private_class_method :fail!
    end
  end
end
