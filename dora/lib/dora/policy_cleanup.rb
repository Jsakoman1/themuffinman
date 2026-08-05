# frozen_string_literal: true

require_relative "retention_review"

module Dora
  class PolicyCleanup
    def self.dry_run!(policy_path, project_root:)
      RetentionReview.review!(policy_path, project_root: project_root).select { |item| item.fetch("classification") == "cleanup_candidate" }.map { |item| item.fetch("path") }
    end

    def self.apply!(policy_path, project_root:, approved_paths:)
      candidates = dry_run!(policy_path, project_root: project_root)
      fail!("approved paths must exactly match the dry-run targets") unless approved_paths.sort == candidates.sort
      candidates.each do |relative|
        absolute = File.expand_path(relative, project_root)
        fail!("cleanup target escapes project root") unless absolute.start_with?("#{File.expand_path(project_root)}/")
        File.delete(absolute) if File.file?(absolute)
      end
      candidates
    end

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
