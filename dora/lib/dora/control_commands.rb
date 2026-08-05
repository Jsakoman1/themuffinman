# frozen_string_literal: true

require_relative "adapter"
require_relative "policy_cleanup"
require_relative "repository_map"
require_relative "retention_review"
require_relative "template_freshness"

module Dora
  class ControlCommands
    def self.retention(adapter_path, config_path, adapter_schema_path:)
      adapter, config = adapter_and_config(adapter_path, config_path, adapter_schema_path)
      RetentionReview.review!(config, project_root: adapter.fetch("root"))
    end

    def self.cleanup_dry_run(adapter_path, config_path, adapter_schema_path:)
      adapter, config = adapter_and_config(adapter_path, config_path, adapter_schema_path)
      PolicyCleanup.dry_run!(config, project_root: adapter.fetch("root"))
    end

    def self.cleanup_apply(adapter_path, config_path, approved_paths, adapter_schema_path:)
      adapter, config = adapter_and_config(adapter_path, config_path, adapter_schema_path)
      PolicyCleanup.apply!(config, project_root: adapter.fetch("root"), approved_paths: approved_paths)
    end

    def self.template_freshness(adapter_path, config_path, adapter_schema_path:)
      adapter, config = adapter_and_config(adapter_path, config_path, adapter_schema_path)
      TemplateFreshness.check!(config, project_root: adapter.fetch("root"))
    end

    def self.repository_map(adapter_path, config_path, adapter_schema_path:)
      adapter, config = adapter_and_config(adapter_path, config_path, adapter_schema_path)
      RepositoryMap.emit!(config, project_root: adapter.fetch("root"))
    end

    def self.adapter_and_config(adapter_path, relative_config_path, schema_path)
      adapter = Adapter.validate!(adapter_path, schema_path)
      fail!("control config path must be project-relative") unless relative_config_path.is_a?(String) && !relative_config_path.empty? && !relative_config_path.start_with?("/") && !relative_config_path.split("/").include?("..")
      config = File.join(adapter.fetch("root"), relative_config_path)
      fail!("control config is missing: #{relative_config_path}") unless File.file?(config)
      [adapter, config]
    end
    private_class_method :adapter_and_config

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
