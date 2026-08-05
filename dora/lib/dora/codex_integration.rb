# frozen_string_literal: true

require "fileutils"

module Dora
  class CodexIntegration
    FILES = %w[README.md session-discovery.md].freeze

    def self.install!(project_root:, template_root: File.expand_path("../../templates/codex-integration", __dir__))
      root = File.expand_path(project_root)
      fail!("Codex integration project root is missing") unless Dir.exist?(root)
      source_root = File.expand_path(template_root)
      fail!("Codex integration templates are missing") unless Dir.exist?(source_root)
      destination_root = File.join(root, ".dora", "codex-integration")
      FileUtils.mkdir_p(destination_root)
      created = []
      preserved = []

      FILES.each do |relative|
        source = File.join(source_root, relative)
        destination = File.join(destination_root, relative)
        fail!("Codex integration template is missing: #{relative}") unless File.file?(source)
        if File.exist?(destination)
          fail!("Codex integration refuses to overwrite changed file: .dora/codex-integration/#{relative}") unless File.binread(destination) == File.binread(source)
          preserved << ".dora/codex-integration/#{relative}"
        else
          FileUtils.cp(source, destination)
          created << ".dora/codex-integration/#{relative}"
        end
      end

      {"kind" => "dora_codex_integration", "version" => 1, "created" => created, "preserved" => preserved, "user_owned_instructions" => ["AGENTS.md"], "authority_boundary" => "The integration supplies project-local navigation guidance only. It does not overwrite user-owned instructions, start work, grant approval, or prove completion."}.freeze
    end

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
