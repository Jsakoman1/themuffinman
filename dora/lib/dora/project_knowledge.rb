# frozen_string_literal: true

require_relative "agent_project_profile"
require_relative "domain_library"
require_relative "product_brief"

module Dora
  class ProjectKnowledge
    ARTIFACTS = {
      "product-brief" => "docs/product-brief.yaml",
      "domain-library" => "docs/domain-library.yaml",
      "agent-profile" => ".dora/agent-project-profile.yaml",
      "agent-entrypoint" => "AGENTS.md"
    }.freeze

    def self.validate!(project_root)
      root = File.expand_path(project_root)
      paths = ARTIFACTS.transform_values { |relative| File.join(root, relative) }
      missing = paths.reject { |_id, path| File.file?(path) }.keys
      fail!("project knowledge artifacts are missing: #{missing.join(", ")}") unless missing.empty?
      brief = ProductBrief.load!(paths.fetch("product-brief"))
      domain = DomainLibrary.load!(paths.fetch("domain-library"))
      profile = AgentProjectProfile.load!(paths.fetch("agent-profile"))
      canonical_paths = Array(profile.fetch("canonical_knowledge")).map { |item| item.fetch("path") }
      expected_canonical = %w[docs/product-brief.yaml docs/domain-library.yaml]
      missing_canonical = expected_canonical - canonical_paths
      fail!("agent profile omits canonical knowledge: #{missing_canonical.join(", ")}") unless missing_canonical.empty?
      Array(profile.fetch("entrypoints")).each do |entrypoint|
        path = File.expand_path(entrypoint.fetch("path"), root)
        fail!("agent profile entrypoint is missing: #{entrypoint.fetch("path")}") unless path.start_with?("#{root}/") && File.file?(path)
      end
      {"product_brief" => brief, "domain_library" => domain, "agent_profile" => profile, "paths" => ARTIFACTS.dup}.freeze
    end

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
