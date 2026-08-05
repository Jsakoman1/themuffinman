# frozen_string_literal: true

require_relative "plugin_adapter"

module Dora
  class BuiltinPluginRunner
    EXECUTION_BOUNDARY = "A built-in plugin runs Dora-owned static analysis against declared source roots; its report is diagnostic evidence only."

    def self.run!(id:, root:, source_roots:, inputs:)
      fail!("builtin plugin is not supported: #{id}") unless PluginAdapter::SUPPORTED.include?(id)
      validate_source_roots!(root: root, source_roots: source_roots)
      result = if id == "http-contract-linker"
        {"plugin" => id, "findings" => [Plugins::HttpContractLinker.analyze!(root: root, inputs: inputs)]}
      elsif id == "spring-configuration-drift"
        {"plugin" => id, "findings" => [Plugins::SpringConfigurationDrift.analyze_declared!(root: root, inputs: inputs)]}
      elsif id == "spring-mapper-usage"
        {"plugin" => id, "findings" => Plugins::SpringMapperUsage.analyze_declared!(root: root, inputs: inputs)}
      elsif id == "vue-navigation"
        {"plugin" => id, "findings" => [Plugins::VueNavigation.analyze_declared!(root: root, inputs: inputs)]}
      elsif id == "vue-surface-hygiene"
        {"plugin" => id, "findings" => [Plugins::VueSurfaceHygiene.analyze_declared!(root: root, inputs: inputs)]}
      else
        PluginAdapter.run!(id: id, root: root, inputs: inputs)
      end
      result.merge("execution_boundary" => EXECUTION_BOUNDARY)
    end

    def self.validate_source_roots!(root:, source_roots:)
      project_root = File.expand_path(root)
      Array(source_roots).each do |source_root|
        path = source_root.fetch("path")
        destination = File.expand_path(path, project_root)
        fail!("builtin plugin source root is missing: #{path}") unless destination.start_with?("#{project_root}/") && File.exist?(destination)
      end
    end
    private_class_method :validate_source_roots!

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
