# frozen_string_literal: true

require "open3"
require "yaml"
require_relative "builtin_plugin_runner"
require_relative "plugin_contract"
require_relative "plugin_report"
require_relative "report_writer"

module Dora
  class PluginRunner
    def self.run!(manifest_path, plugin_id:, project_root: Dir.pwd)
      PluginContract.validate!(manifest_path)
      manifest = YAML.load_file(manifest_path)
      plugin = Array(manifest["plugins"]).find { |candidate| candidate["id"] == plugin_id }
      fail!("plugin is not declared: #{plugin_id}") unless plugin

      root = File.expand_path(project_root)
      report_path = plugin.fetch("output").fetch("path", "docs/audit-output/dora-plugin-#{plugin_id}.json")
      if plugin["builtin"]
        builtin = plugin.fetch("builtin")
        result = BuiltinPluginRunner.run!(id: builtin, root: root, source_roots: plugin.fetch("source_roots"), inputs: plugin.fetch("inputs"))
        report = PluginReport.build!(plugin_id: plugin_id, inputs: plugin.fetch("inputs"), findings: result.fetch("findings"), output: plugin.fetch("output").merge("path" => report_path))
        execution = {"builtin" => builtin}
      else
        entrypoint = plugin.fetch("entrypoint")
        path = File.expand_path(entrypoint, root)
        fail!("plugin entrypoint is missing") unless path.start_with?("#{root}/") && File.file?(path)

        output, status = Open3.capture2e({"DORA_PLUGIN_RUNNER" => "1"}, "ruby", path, chdir: root)
        fail!("plugin failed: #{plugin_id}\n#{output}") unless status.success?
        report = PluginReport.build!(plugin_id: plugin_id, inputs: plugin.fetch("inputs"), findings: [{"id" => "plugin-process", "output" => output}], output: plugin.fetch("output").merge("path" => report_path))
        execution = {"entrypoint" => entrypoint}
      end
      ReportWriter.write_json!(root: root, relative_path: report_path, payload: report)
      report.merge(execution).merge("source_roots" => plugin.fetch("source_roots"))
    end

    def self.safe_relative_path?(path)
      path.is_a?(String) && !path.empty? && !path.start_with?("/") && !path.split("/").include?("..")
    end
    private_class_method :safe_relative_path?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
