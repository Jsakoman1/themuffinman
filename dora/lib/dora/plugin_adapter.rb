# frozen_string_literal: true

require_relative "plugins/architecture_integrity"
require_relative "plugins/spring_configuration_drift"
require_relative "plugins/spring_mapper_usage"
require_relative "plugins/http_contract_linker"
require_relative "plugins/vue_navigation"
require_relative "plugins/vue_surface_hygiene"

module Dora
  class PluginAdapter
    SUPPORTED = %w[architecture-integrity spring-configuration-drift spring-mapper-usage http-contract-linker vue-navigation vue-surface-hygiene].freeze

    def self.run!(id:, root:, inputs:)
      fail!("plugin adapter is not supported: #{id}") unless SUPPORTED.include?(id)
      fail!("plugin adapter inputs must be a mapping") unless inputs.is_a?(Hash)
      result = case id
      when "architecture-integrity"
        {"paths_valid" => Plugins::ArchitectureIntegrity.validate_paths!(root: root, paths: inputs.fetch("paths", [])), "forbidden" => Plugins::ArchitectureIntegrity.scan_forbidden!(root: root, rules: inputs.fetch("rules", []))}
      when "spring-configuration-drift"
        Plugins::SpringConfigurationDrift.analyze!(root: root, properties_path: inputs.fetch("properties_path"), property_prefixes: inputs.fetch("property_prefixes"))
      when "spring-mapper-usage"
        Plugins::SpringMapperUsage.analyze!(root: root, mapper_glob: inputs.fetch("mapper_glob"), source_root: inputs.fetch("source_root"))
      when "http-contract-linker"
        Plugins::HttpContractLinker.endpoint_links!(root: root, controller_glob: inputs.fetch("controller_glob"), client_glob: inputs.fetch("client_glob"))
      when "vue-navigation"
        Plugins::VueNavigation.analyze!(root: root, router_path: inputs.fetch("router_path"), navigation_paths: inputs.fetch("navigation_paths"), required_surfaces: inputs.fetch("required_surfaces", []))
      when "vue-surface-hygiene"
        Plugins::VueSurfaceHygiene.scan!(root: root, source_glob: inputs.fetch("source_glob"), required_markers: inputs.fetch("required_markers", []))
      end
      {"plugin" => id, "findings" => result.is_a?(Array) ? result : [result]}
    rescue KeyError => error
      fail!("plugin adapter inputs are incomplete: #{error.message}")
    end

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
