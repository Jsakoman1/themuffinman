# frozen_string_literal: true

require "digest"
require "fileutils"
require "yaml"
require_relative "bootstrap_source"
require_relative "approval_record"
require_relative "package_copy"

module Dora
  class ProjectUpgrade
    def self.apply!(project_root:, source_descriptor_path:, approval:)
      preview = preview!(project_root: project_root, source_descriptor_path: source_descriptor_path)
      root = File.expand_path(project_root)
      ApprovalRecord.validate!(approval, operation: "upgrade_apply", scope: root)
      package = File.join(root, preview.dig("consumer", "package_path"))
      record_path = File.join(root, ".dora/bootstrap-source.yaml")
      backup = File.join(root, ".dora/backups", "upgrade-#{Time.now.utc.strftime('%Y%m%d%H%M%S')}")
      pin_backup = "#{backup}.bootstrap-source.yaml"
      FileUtils.mkdir_p(File.dirname(backup)); FileUtils.cp_r(package, backup); FileUtils.cp(record_path, pin_backup)
      target = BootstrapSource.load!(source_descriptor_path)
      FileUtils.rm_rf(package); FileUtils.mkdir_p(package); PackageCopy.copy!(source_root: target.fetch("path"), destination: package)
      persist_target_pin!(root: root, target: target)
      {"kind" => "dora_project_upgrade_apply", "version" => 1, "applied" => true, "backup" => backup.delete_prefix("#{root}/"), "target" => target.slice("ref", "checksum"), "rollback" => "Restore the recorded backup directory explicitly.", "migrations" => preview.fetch("migrations")}.freeze
    end

    def self.rollback!(project_root:, backup:, approval:)
      root = File.expand_path(project_root)
      ApprovalRecord.validate!(approval, operation: "upgrade_rollback", scope: root)
      fail!("upgrade rollback backup path is invalid") unless safe_relative_path?(backup)
      source = File.expand_path(backup, root)
      fail!("upgrade rollback backup escapes project root") unless source.start_with?("#{root}/")
      fail!("upgrade rollback backup is missing") unless source.start_with?(File.join(root, ".dora/backups/")) && Dir.exist?(source)
      record = YAML.load_file(File.join(root, ".dora/bootstrap-source.yaml"))
      package_path = record.is_a?(Hash) && record["package_path"]
      fail!("consumer Dora package path is invalid") unless safe_relative_path?(package_path)
      package = File.join(root, package_path)
      backup_pin = "#{source}.bootstrap-source.yaml"
      fail!("upgrade rollback bootstrap source record is missing") unless File.file?(backup_pin)
      FileUtils.rm_rf(package)
      FileUtils.mkdir_p(package)
      PackageCopy.copy!(source_root: source, destination: package)
      FileUtils.cp(backup_pin, File.join(root, ".dora/bootstrap-source.yaml"))
      {"kind" => "dora_project_upgrade_rollback", "version" => 1, "rolled_back" => true, "backup" => backup, "restored_package" => package_path, "restored_source_pin" => true, "completion_boundary" => "Rollback restores only the recorded local Dora package and source pin; it does not prove consumer compatibility or release readiness."}.freeze
    rescue Psych::Exception => error
      fail!("upgrade rollback YAML is invalid: #{error.message}")
    end
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

    def self.persist_target_pin!(root:, target:)
      record_path = File.join(root, ".dora/bootstrap-source.yaml")
      record = YAML.load_file(record_path)
      fail!("consumer bootstrap source record is invalid") unless record.is_a?(Hash) && record["kind"] == "dora_bootstrap_record" && record["version"].to_i == 1 && record["source"].is_a?(Hash)

      record["source"] = target.slice("ref", "checksum")
      temporary_path = "#{record_path}.tmp-#{Process.pid}"
      File.write(temporary_path, YAML.dump(record).sub(/\A---\n/, ""))
      File.rename(temporary_path, record_path)
    ensure
      FileUtils.rm_f(temporary_path) if defined?(temporary_path) && File.exist?(temporary_path)
    end
    private_class_method :persist_target_pin!

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
