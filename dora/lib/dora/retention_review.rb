# frozen_string_literal: true

require "yaml"

module Dora
  class RetentionReview
    def self.review!(policy_path, project_root:)
      policy = YAML.load_file(policy_path)
      fail!("retention policy kind is invalid") unless policy["kind"] == "dora_retention_policy" && policy["version"].to_i == 1
      roots = paths!(policy["generated_roots"], "generated_roots")
      retained = paths!(policy["retained_paths"], "retained_paths", allow_empty: true)
      candidates = paths!(policy["cleanup_candidate_globs"], "cleanup_candidate_globs", allow_empty: true)
      files = roots.flat_map { |root| Dir[File.join(project_root, root, "**", "*")].select { |path| File.file?(path) } }.sort
      files.map do |absolute|
        relative = absolute.delete_prefix("#{File.expand_path(project_root)}/")
        status = if matches?(relative, retained)
                   "retained"
                 elsif matches?(relative, candidates)
                   "cleanup_candidate"
                 else
                   "retained"
                 end
        {"path" => relative, "classification" => status}
      end
    end

    def self.paths!(values, label, allow_empty: false)
      values = Array(values)
      fail!("#{label} must not be empty") if values.empty? && !allow_empty
      fail!("#{label} contains an invalid project-relative path") if values.any? { |value| !value.is_a?(String) || value.empty? || value.start_with?("/") || value.split("/").include?("..") }
      values
    end
    private_class_method :paths!

    def self.matches?(relative, patterns)
      patterns.any? { |pattern| File.fnmatch?(pattern, relative, File::FNM_PATHNAME | File::FNM_EXTGLOB) }
    end
    private_class_method :matches?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
