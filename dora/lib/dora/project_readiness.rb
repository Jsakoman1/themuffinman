# frozen_string_literal: true

require "open3"

module Dora
  class ProjectReadiness
    def self.report!(project_root:, initialize_git: false)
      root = File.expand_path(project_root)
      fail!("project root is missing") unless Dir.exist?(root)

      initialize_repository!(root) if initialize_git && !repository?(root)
      baseline, status = Open3.capture2e("git", "-C", root, "rev-parse", "HEAD") if repository?(root)

      if status&.success?
        return {"kind" => "dora_project_readiness", "version" => 1, "ready" => true, "state" => "ready", "project_root" => root, "baseline" => baseline.strip, "initialization" => initialize_git ? "completed_or_not_needed" : "not_requested", "remediation" => nil, "completion_boundary" => "Readiness reports Git baseline state only; it does not validate product behavior or completion."}.freeze
      end

      {"kind" => "dora_project_readiness", "version" => 1, "ready" => false, "state" => repository?(root) ? "missing_git_commit" : "missing_git_repository", "project_root" => root, "baseline" => nil, "initialization" => "not_requested", "remediation" => "Run dora readiness <project-root> --initialize-git to create a local Git baseline explicitly.", "completion_boundary" => "Readiness reports Git baseline state only; it does not validate product behavior or completion."}.freeze
    end

    def self.repository?(root)
      _output, status = Open3.capture2e("git", "-C", root, "rev-parse", "--is-inside-work-tree")
      status.success?
    end
    private_class_method :repository?

    def self.initialize_repository!(root)
      _output, status = Open3.capture2e("git", "init", "-q", root)
      fail!("cannot initialize local Git repository") unless status.success?
      _output, status = Open3.capture2e("git", "-C", root, "add", "-A")
      fail!("cannot stage local baseline") unless status.success?
      _output, status = Open3.capture2e("git", "-C", root, "-c", "user.name=Dora", "-c", "user.email=dora@local", "commit", "-qm", "Initialize Dora project baseline")
      fail!("cannot create local Git baseline") unless status.success?
    end
    private_class_method :initialize_repository!

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
