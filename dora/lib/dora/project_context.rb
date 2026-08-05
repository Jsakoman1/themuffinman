# frozen_string_literal: true

module Dora
  class ProjectContext
    PATH_KEYS = %w[docs work_plans audit_output runtime_evidence].freeze

    attr_reader :id, :root

    def self.from_validated_adapter(adapter)
      new(
        id: adapter.fetch("project"),
        root: adapter.fetch("root"),
        paths: adapter.fetch("paths"),
        commands: adapter.fetch("commands"),
        extensions: adapter.fetch("extensions"),
        generated_output_paths: adapter.fetch("context").fetch("generated_output_paths")
      )
    end

    def initialize(id:, root:, paths:, commands:, extensions:, generated_output_paths:)
      @id = id.freeze
      @root = root.freeze
      @paths = paths.transform_keys(&:to_s).transform_values(&:dup).freeze
      @commands = commands.transform_keys(&:to_s).transform_values(&:dup).freeze
      @extension_count = extensions
      @generated_output_paths = generated_output_paths.map(&:to_s).freeze
    end

    def docs_root
      absolute_path("docs")
    end

    def work_plans_root
      absolute_path("work_plans")
    end

    def audit_output_root
      absolute_path("audit_output")
    end

    def runtime_evidence_root
      absolute_path("runtime_evidence")
    end

    def generated_output_roots
      @generated_output_paths.map { |key| absolute_path(key) }.freeze
    end

    def command(name)
      @commands.fetch(name.to_s) { raise ArgumentError, "project command is not declared: #{name}" }.dup
    end

    def command_names
      @commands.keys.freeze
    end

    def extension_count
      @extension_count
    end

    private

    def absolute_path(key)
      relative_path = @paths.fetch(key) { raise ArgumentError, "project path is not declared: #{key}" }
      File.expand_path(relative_path, root)
    end
  end
end
