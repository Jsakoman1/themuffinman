# frozen_string_literal: true

require "digest"
require "yaml"
require_relative "bootstrap_source"

module Dora
  class ProjectUpgrade
    def self.preview!(project_root:, source_descriptor_path:)
      root = File.expand_path(project_root)
      record_path = File.join(root, ".dora/bootstrap-source.yaml")
      fail!("consumer bootstrap source record is missing") unless File.file?(record_path)
      record = YAML.load_file(record_path)
      fail!("consumer bootstrap source record is invalid") unless record.is_a?(Hash) && record["kind"] == "dora_bootstrap_record" && record["version"].to_i == 1 && record["source"].is_a?(Hash)
      package_path = record["package_path"]
      fail!("consumer Dora package path is invalid") unless safe_relative_path?(package_path) && Dir.exist?(File.join(root, package_path))
      target = BootstrapSource.load!(source_descriptor_path)
      migrations = compare(File.join(root, package_path), target.fetch("path"))
      {"kind" => "dora_project_upgrade_preview", "version" => 1, "read_only" => true, "consumer" => {"root" => root, "package_path" => package_path, "current_ref" => record.dig("source", "ref"), "current_checksum" => record.dig("source", "checksum")}, "target" => target.slice("ref", "checksum", "integrity"), "migrations" => migrations, "rollback" => "No files were changed. Keep the current pinned package and source record until a reviewed upgrade is explicitly applied.", "completion_boundary" => "Upgrade preview is advisory only and does not copy source, update a consumer pin, or prove release readiness."}.freeze
    rescue Psych::Exception => error
      fail!("upgrade preview YAML is invalid: #{error.message}")
    end

    def self.compare(current_root, target_root)
      current = files(current_root)
      target = files(target_root)
      {"added" => (target.keys - current.keys).sort, "changed" => (target.keys & current.keys).select { |path| target.fetch(path) != current.fetch(path) }.sort, "removed" => (current.keys - target.keys).sort}
    end
    private_class_method :compare

    def self.files(root)
      Dir[File.join(root, "**/*")].select { |path| File.file?(path) && !path.delete_prefix("#{root}/").split("/").include?(".git") }.to_h { |path| [path.delete_prefix("#{root}/"), Digest::SHA256.file(path).hexdigest] }
    end
    private_class_method :files

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
