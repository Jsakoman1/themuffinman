# frozen_string_literal: true

require "fileutils"
require "yaml"
require_relative "feature_skeleton"

module Dora
  class FeatureSkeletonApply
    TEMPLATE_ROOT = File.expand_path("../../templates/feature-skeleton", __dir__)

    def self.apply!(document:, destination:, dry_run:)
      fail!("feature skeleton destination must be a non-empty relative path without traversal") unless safe_relative_path?(destination)
      preview = FeatureSkeleton.preview!(document)
      root = File.expand_path(destination)
      files = preview.fetch("proposed_files")
      targets = files.map { |proposal| safe_target!(root, proposal.fetch("path")) }
      collisions = targets.select { |target| File.exist?(target) }
      fail!("feature skeleton apply rejected existing paths: #{collisions.map { |path| relative_to(root, path) }.join(", ")}") unless collisions.empty?

      result = {
        "kind" => "dora_feature_skeleton_apply",
        "version" => 1,
        "mode" => dry_run ? "dry_run" : "applied",
        "destination" => destination,
        "capability" => preview.fetch("capability"),
        "files" => files.map { |proposal| proposal.fetch("path") },
        "completion_boundary" => dry_run ? "Dry-run checks only confirmed inputs and target collisions; it creates no files." : "Apply creates reviewable placeholders only. It does not implement behavior, validate a built application, or prove acceptance."
      }
      return result.freeze if dry_run

      context = {
        "capability" => preview.fetch("capability"),
        "entity" => preview.dig("confirmed_model", "entity"),
        "model_yaml" => YAML.dump(preview.fetch("confirmed_model")).sub(/\A---\n/, "")
      }
      files.zip(targets).each do |proposal, target|
        FileUtils.mkdir_p(File.dirname(target))
        File.write(target, render(template_for(proposal.fetch("path")), context))
      end
      result.freeze
    end

    def self.safe_relative_path?(path)
      path.is_a?(String) && !path.empty? && !path.start_with?("/") && !path.split("/").include?("..")
    end
    private_class_method :safe_relative_path?

    def self.safe_target!(root, path)
      fail!("feature skeleton proposal path is unsafe: #{path}") unless safe_relative_path?(path)
      target = File.expand_path(path, root)
      fail!("feature skeleton proposal escapes its destination: #{path}") unless target.start_with?("#{root}/")
      target
    end
    private_class_method :safe_target!

    def self.relative_to(root, path); path.delete_prefix("#{root}/"); end
    private_class_method :relative_to

    def self.template_for(path)
      return "migration.sql" if path.end_with?(".sql")
      return "api-client.ts" if path.end_with?("api-client.ts")
      return "feature-view.vue" if path.end_with?("feature-view.vue")
      return "backend-test.java" if path.include?("/src/test/")
      return "frontend-test.ts" if path.end_with?(".spec.ts")
      return "api.yaml" if path.start_with?("docs/api/")
      return "runtime-evidence.yaml" if path.start_with?("docs/runtime-evidence/")
      return "capability.yaml" if path.start_with?("docs/capabilities/")

      "backend.java"
    end
    private_class_method :template_for

    def self.render(template, context)
      template_path = File.join(TEMPLATE_ROOT, template)
      fail!("feature skeleton template is missing: #{template}") unless File.file?(template_path)
      context.reduce(File.read(template_path)) { |content, (key, value)| content.gsub("{{#{key}}}", value.to_s) }
    end
    private_class_method :render

    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
