# frozen_string_literal: true

require "open3"
require "time"
require "yaml"

module Dora
  class WorkspaceSnapshot
    def self.write!(config_path, destination_path, project_root:)
      snapshot = snapshot(config_path, project_root: project_root, excluded_paths: [destination_path])
      File.write(File.join(project_root, destination_path), YAML.dump(snapshot).sub(/\A---\n/, ""))
      snapshot
    end

    def self.snapshot(config_path, project_root:, excluded_paths: [])
      config = YAML.load_file(config_path)
      fail!("workspace snapshot config kind is invalid") unless config["kind"] == "dora_workspace_snapshot_config" && config["version"].to_i == 1
      roots = paths!(config["roots"], "roots")
      categories = Array(config["categories"])
      changes = git_changes(project_root).reject { |row| excluded_paths.include?(row.fetch("path")) }.select { |row| roots.any? { |root| row.fetch("path").start_with?(root.sub(%r{/$}, "") + "/") || row.fetch("path") == root } }.map do |row|
        row.merge("status_group" => status_group(row.fetch("status")), "category" => category(row.fetch("path"), categories))
      end.sort_by { |row| row.fetch("path") }
      {"kind" => "dora_workspace_change_snapshot", "version" => 1, "captured_at" => Time.now.utc.iso8601, "roots" => roots, "excluded_paths" => excluded_paths.sort, "summary" => {"changed_paths" => changes.length}, "changes" => changes}
    end

    def self.verify!(path)
      snapshot = YAML.load_file(path)
      fail!("workspace snapshot kind is invalid") unless snapshot["kind"] == "dora_workspace_change_snapshot" && snapshot["version"].to_i == 1
      changes = Array(snapshot["changes"])
      fail!("workspace snapshot paths are duplicated") unless changes.map { |row| row["path"] }.uniq.length == changes.length
      fail!("workspace snapshot paths are unsorted") unless changes.map { |row| row["path"] } == changes.map { |row| row["path"] }.sort
      fail!("workspace snapshot count differs") unless snapshot.dig("summary", "changed_paths") == changes.length
      snapshot
    end

    def self.git_changes(root)
      output, status = Open3.capture2e("git", "status", "--porcelain=v1", "--untracked-files=all", chdir: root)
      fail!("cannot inspect Git workspace: #{output}") unless status.success?
      output.lines.map do |line|
        next if line.length < 4
        {"path" => line[3..].strip, "status" => line[0, 2]}
      end.compact
    end
    private_class_method :git_changes

    def self.paths!(values, label)
      paths = Array(values)
      fail!("#{label} must be non-empty project-relative paths") if paths.empty? || paths.any? { |path| !path.is_a?(String) || path.empty? || path.start_with?("/") || path.split("/").include?("..") }
      paths
    end
    private_class_method :paths!

    def self.status_group(status)
      return "untracked" if status == "??"
      return "deleted" if status.include?("D")
      return "added" if status.include?("A")
      return "renamed" if status.include?("R")

      "modified"
    end
    private_class_method :status_group

    def self.category(path, categories)
      match = categories.find { |entry| Array(entry["path_prefixes"]).any? { |prefix| path.start_with?(prefix) } }
      match ? match.fetch("id") : "other"
    end
    private_class_method :category

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
