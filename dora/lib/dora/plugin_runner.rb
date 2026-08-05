# frozen_string_literal: true

require "open3"
require "digest"
require "timeout"
require "yaml"
require_relative "analysis_cache"
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
      execution_policy = execution_policy_for(plugin)
      cache = AnalysisCache.fetch!(cache_root: File.join(root, ".dora/cache"), key: "plugin-#{plugin_id}", input: cache_input(root, plugin)) { execute_plugin(root, plugin, plugin_id, execution_policy) }
      result = cache.fetch("value")
      report = PluginReport.build!(plugin_id: plugin_id, inputs: plugin.fetch("inputs"), findings: result.fetch("findings"), output: plugin.fetch("output").merge("path" => report_path), finding_context: {"source_roots" => plugin.fetch("source_roots").map { |source| source.fetch("path") }}, execution_boundary: result.fetch("execution").fetch("policy"), read_boundary: {"declared_source_roots" => plugin.fetch("source_roots").map { |source| source.slice("id", "path") }, "report_path" => report_path, "actual_enforcement" => "Dora validates declared source-root paths for built-ins and passes them to built-in analysis. Custom entrypoints remain project-trusted processes and are not filesystem-sandboxed.", "unsupported_guarantees" => PluginContract::REQUIRED_UNSUPPORTED_GUARANTEES})
      ReportWriter.write_json!(root: root, relative_path: report_path, payload: report)
      report.merge(result.fetch("execution")).merge("source_roots" => plugin.fetch("source_roots"), "cache" => cache.fetch("cache"))
    end

    def self.execute_plugin(root, plugin, plugin_id, execution_policy)
      if plugin["builtin"]
        builtin = plugin.fetch("builtin")
        result = within_timeout!(plugin_id, execution_policy) { BuiltinPluginRunner.run!(id: builtin, root: root, source_roots: plugin.fetch("source_roots"), inputs: plugin.fetch("inputs")) }
        {"findings" => result.fetch("findings"), "execution" => {"builtin" => builtin, "policy" => execution_policy}}
      else
        entrypoint = plugin.fetch("entrypoint")
        path = File.expand_path(entrypoint, root)
        fail!("plugin entrypoint is missing") unless path.start_with?("#{root}/") && File.file?(path)
        output, status = run_entrypoint!(path, root, plugin_id, execution_policy)
        fail!("plugin failed: #{plugin_id}\n#{output}") unless status.success?
        {"findings" => [{"id" => "plugin-process", "output" => output}], "execution" => {"entrypoint" => entrypoint, "policy" => execution_policy}}
      end
    end
    private_class_method :execute_plugin

    def self.execution_policy_for(plugin)
      declared = plugin["execution_policy"]
      return {"status" => "legacy_unreviewed", "timeout_seconds" => nil, "isolation" => "none", "unsupported_guarantees" => PluginContract::REQUIRED_UNSUPPORTED_GUARANTEES, "enforcement" => "legacy manifest version 1; migrate to version 2 for a declared timeout and trust boundary"} unless declared

      declared.merge("status" => "declared_and_enforced", "enforcement" => "Dora enforces only the declared wall-clock timeout. It does not sandbox process, filesystem, network, credentials, or resources.")
    end
    private_class_method :execution_policy_for

    def self.within_timeout!(plugin_id, policy)
      return yield unless policy["timeout_seconds"]

      Timeout.timeout(policy.fetch("timeout_seconds")) { yield }
    rescue Timeout::Error
      fail!("plugin timed out after #{policy.fetch("timeout_seconds")} seconds: #{plugin_id}")
    end
    private_class_method :within_timeout!

    def self.run_entrypoint!(path, root, plugin_id, policy)
      return Open3.capture2e({"DORA_PLUGIN_RUNNER" => "1"}, "ruby", path, chdir: root) unless policy["timeout_seconds"]

      output = +""
      stdin, stream, wait = Open3.popen2e({"DORA_PLUGIN_RUNNER" => "1"}, "ruby", path, chdir: root)
      stdin.close
      begin
        Timeout.timeout(policy.fetch("timeout_seconds")) { output << stream.read; [output, wait.value] }
      rescue Timeout::Error
        terminate_process!(wait)
        fail!("plugin timed out after #{policy.fetch("timeout_seconds")} seconds: #{plugin_id}")
      ensure
        stream.close unless stream.closed?
      end
    end
    private_class_method :run_entrypoint!

    def self.terminate_process!(wait)
      Process.kill("TERM", wait.pid)
    rescue Errno::ESRCH
      nil
    ensure
      begin
        Timeout.timeout(1) { wait.value }
      rescue Timeout::Error
        Process.kill("KILL", wait.pid) rescue nil
        wait.value
      end
    end
    private_class_method :terminate_process!

    def self.cache_input(root, plugin)
      entrypoint = plugin["entrypoint"]
      entrypoint_identity = entrypoint ? {"path" => entrypoint, "digest" => Digest::SHA256.file(File.expand_path(entrypoint, root)).hexdigest} : nil
      {"plugin" => plugin.slice("id", "builtin", "entrypoint", "inputs", "execution_policy"), "entrypoint_identity" => entrypoint_identity, "dora_implementation_identity" => Digest::SHA256.file(__FILE__).hexdigest, "sources" => Array(plugin.fetch("source_roots")).flat_map { |source| source_snapshot(root, source.fetch("path")) }}
    end
    private_class_method :cache_input

    def self.source_snapshot(root, relative)
      path = File.expand_path(relative, root)
      Dir[File.join(path, "**/*")].select { |candidate| File.file?(candidate) }.sort.map { |candidate| {"path" => candidate.delete_prefix("#{root}/"), "digest" => Digest::SHA256.file(candidate).hexdigest} }
    end
    private_class_method :source_snapshot

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
