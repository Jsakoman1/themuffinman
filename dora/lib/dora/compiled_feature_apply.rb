# frozen_string_literal: true

require "fileutils"
require_relative "generated_feature_manifest"

module Dora
  class CompiledFeatureApply
    def self.apply!(manifest:, destination:, rendered_files:, dry_run:)
      declared = GeneratedFeatureManifest.validate!(manifest)
      fail!("compiled feature destination must be a non-empty relative path without traversal") unless safe_relative?(destination)
      expected_paths = declared.fetch("outputs").map { |entry| entry.fetch("path") }
      fail!("compiled feature rendered files must exactly match the manifest") unless rendered_files.is_a?(Hash) && rendered_files.keys.sort == expected_paths.sort && rendered_files.values.all? { |content| content.is_a?(String) }
      root = File.expand_path(destination)
      targets = expected_paths.map { |path| [path, safe_target!(root, path)] }
      collisions = targets.select { |_path, target| File.exist?(target) }
      unless collisions.empty?
        historic = collisions.select { |path, _target| path.match?(%r{/db/migration/V[0-9]+__}) }
        prefix = historic.empty? ? "existing paths" : "historic migration paths"
        fail!("compiled feature apply rejected #{prefix}: #{collisions.map(&:first).join(", ")}")
      end
      result = {"kind" => "dora_compiled_feature_apply", "version" => 1, "mode" => dry_run ? "dry_run" : "applied", "destination" => destination, "manifest_digest" => digest(declared), "files" => expected_paths, "completion_boundary" => dry_run ? "Dry-run validates manifest, paths, and collisions only; it writes no consumer files." : "Apply writes only the exact manifest files. It does not compile, run, validate a database, or prove acceptance."}
      return result.freeze if dry_run

      targets.each do |path, target|
        FileUtils.mkdir_p(File.dirname(target))
        File.write(target, rendered_files.fetch(path))
      end
      result.freeze
    end

    def self.safe_relative?(value); value.is_a?(String) && !value.empty? && !value.start_with?("/") && !value.split("/").include?(".."); end
    private_class_method :safe_relative?
    def self.safe_target!(root, path)
      fail!("compiled feature manifest path is unsafe: #{path}") unless safe_relative?(path)
      target = File.expand_path(path, root)
      fail!("compiled feature manifest path escapes destination: #{path}") unless target.start_with?("#{root}/")
      target
    end
    private_class_method :safe_target!
    def self.digest(manifest); require "digest"; Digest::SHA256.hexdigest(Marshal.dump(manifest)); end
    private_class_method :digest
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
