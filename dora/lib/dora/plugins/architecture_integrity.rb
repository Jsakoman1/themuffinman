# frozen_string_literal: true

module Dora
  module Plugins
    class ArchitectureIntegrity
      def self.analyze!(root:, source_roots:, inputs:)
        declared_paths = inputs.fetch("paths", Array(source_roots).map { |source_root| source_root.fetch("path") })
        rules = inputs.fetch("rules", [])
        validate_paths!(root: root, paths: declared_paths)
        {
          "paths" => declared_paths,
          "forbidden" => scan_forbidden!(root: root, rules: rules)
        }
      rescue KeyError => error
        fail!("architecture analysis inputs are incomplete: #{error.message}")
      end

      def self.validate_paths!(root:, paths:)
        root = File.expand_path(root)
        Array(paths).each do |path|
          fail!("declared architecture path is invalid") unless safe_relative_path?(path)
          fail!("declared architecture path is missing: #{path}") unless File.exist?(File.join(root, path))
        end
        true
      end

      def self.scan_forbidden!(root:, rules:)
        root = File.expand_path(root)
        Array(rules).flat_map do |rule|
          glob = rule["source_glob"]; pattern = rule["forbidden_pattern"]
          fail!("architecture rule is incomplete") unless safe_relative_path?(glob) && pattern.is_a?(String) && !pattern.empty?
          Dir[File.join(root, glob)].select { |path| File.file?(path) && File.read(path).match?(Regexp.new(pattern)) }.map { |path| {"rule" => rule["id"], "path" => path.delete_prefix("#{root}/")} }
        end
      end

      def self.safe_relative_path?(path)
        path.is_a?(String) && !path.empty? && !path.start_with?("/") && !path.split("/").include?("..")
      end
      private_class_method :safe_relative_path?
      def self.fail!(message); raise ArgumentError, message; end
      private_class_method :fail!
    end
  end
end
