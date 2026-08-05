# frozen_string_literal: true

require "tempfile"
require "yaml"
require_relative "agent_project_profile"
require_relative "domain_library"
require_relative "product_brief"

module Dora
  class ProjectIntake
    def self.build!(answers)
      fail!("project intake must be a mapping") unless answers.is_a?(Hash)
      fail!("project intake kind is invalid") unless answers["kind"] == "dora_project_intake" && answers["version"].to_i == 1
      required = %w[product_brief domain_library agent_profile]
      missing = required.reject { |field| answers[field].is_a?(Hash) }
      fail!("project intake is missing #{missing.join(", ")}") unless missing.empty?

      {
        "kind" => "dora_project_knowledge", "version" => 1,
        "product_brief" => validate!(answers.fetch("product_brief"), ProductBrief),
        "domain_library" => validate!(answers.fetch("domain_library"), DomainLibrary),
        "agent_profile" => validate!(answers.fetch("agent_profile"), AgentProjectProfile),
        "invention" => "none"
      }.freeze
    end

    def self.validate!(document, loader)
      Tempfile.create(["dora-project-intake", ".yaml"]) do |file|
        file.write(YAML.dump(document))
        file.flush
        loader.load!(file.path)
      end
    end
    private_class_method :validate!

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
