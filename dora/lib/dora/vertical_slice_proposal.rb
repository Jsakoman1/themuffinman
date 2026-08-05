# frozen_string_literal: true

module Dora
  class VerticalSliceProposal
    REQUIRED_SURFACES = %w[migration backend api frontend tests runtime_evidence documentation].freeze

    def self.validate!(proposal)
      fail!("vertical-slice proposal is invalid") unless proposal.is_a?(Hash) && proposal["kind"] == "dora_vertical_slice_proposal" && proposal["version"].to_i == 1
      validate_capability!(proposal["capability"])
      validate_surfaces!(proposal["surfaces"])
      validate_atomic_work!(proposal["atomic_work"])
      fail!("vertical-slice proposal gaps must be an array") unless proposal["gaps"].is_a?(Array) && proposal["gaps"].all? { |gap| statement?(gap) }
      fail!("vertical-slice proposal must not contain generated source") if contains_implementation_content?(proposal)

      proposal.slice("kind", "version", "capability", "surfaces", "atomic_work", "gaps").merge("completion_boundary" => "This is a reviewable proposal only. Product-owned decisions, implementation, test results, runtime evidence, and acceptance remain unproven.")
    end

    def self.validate_capability!(capability)
      fail!("vertical-slice proposal capability is invalid") unless capability.is_a?(Hash) && identifier?(capability["id"]) && statement?(capability["title"]) && capability["confirmed"] == true
    end
    private_class_method :validate_capability!

    def self.validate_surfaces!(surfaces)
      fail!("vertical-slice proposal surfaces are invalid") unless surfaces.is_a?(Hash) && surfaces.keys.sort == REQUIRED_SURFACES.sort
      REQUIRED_SURFACES.each do |name|
        paths = surfaces.fetch(name)
        fail!("vertical-slice proposal #{name} paths are invalid") unless paths.is_a?(Array) && !paths.empty? && paths.all? { |path| safe_relative_path?(path) }
      end
    end
    private_class_method :validate_surfaces!

    def self.validate_atomic_work!(work)
      fail!("vertical-slice proposal atomic work is invalid") unless work.is_a?(Hash) && safe_relative_path?(work["plan"]) && work["task"].is_a?(Hash)
      task = work.fetch("task")
      fail!("vertical-slice proposal atomic task is invalid") unless identifier?(task["id"]) && statement?(task["title"]) && statement?(task["observable_outcome"]) && task["required_paths"].is_a?(Array) && !task["required_paths"].empty? && task["required_paths"].all? { |path| safe_relative_path?(path) } && statement?(task["validation"]) && task["evidence_boundary"].is_a?(Array) && !task["evidence_boundary"].empty? && task["evidence_boundary"].all? { |entry| statement?(entry) }
    end
    private_class_method :validate_atomic_work!

    def self.contains_implementation_content?(value)
      case value
      when Hash
        value.any? { |key, entry| %w[source source_code sql content command].include?(key.to_s) || contains_implementation_content?(entry) }
      when Array
        value.any? { |entry| contains_implementation_content?(entry) }
      else
        false
      end
    end
    private_class_method :contains_implementation_content?

    def self.identifier?(value); value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/); end
    private_class_method :identifier?
    def self.statement?(value); value.is_a?(String) && !value.strip.empty?; end
    private_class_method :statement?
    def self.safe_relative_path?(value); statement?(value) && !value.start_with?("/") && !value.split("/").include?(".."); end
    private_class_method :safe_relative_path?
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
