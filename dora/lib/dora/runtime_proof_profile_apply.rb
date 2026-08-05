# frozen_string_literal: true

require "fileutils"
require "yaml"
require_relative "runtime_proof_profile"

module Dora
  class RuntimeProofProfileApply
    TEMPLATE_ROOT = File.expand_path("../../templates/runtime-profiles/playwright", __dir__)
    PACK_PATH = File.expand_path("../../packs/playwright-runtime-proof.yaml", __dir__)

    def self.apply!(destination:, dry_run:)
      fail!("runtime-proof destination must be a relative path without traversal") unless destination.is_a?(String) && !destination.start_with?("/") && !destination.split("/").include?("..")
      root = File.expand_path(destination)
      fail!("runtime-proof destination must be empty") if File.exist?(root) && !Dir.children(root).empty?
      profile = RuntimeProofProfile.validate!(YAML.load_file(PACK_PATH).fetch("profile"))
      files = Dir.glob(File.join(TEMPLATE_ROOT, "**", "*"), File::FNM_DOTMATCH).select { |path| File.file?(path) }.map { |path| path.delete_prefix("#{TEMPLATE_ROOT}/") }.sort
      return {"kind" => "dora_runtime_profile_apply", "version" => 1, "mode" => "dry_run", "destination" => destination, "files" => files, "profile" => profile.fetch("id"), "completion_boundary" => "Dry-run reports files only; it does not create a runtime profile or run a browser."}.freeze if dry_run
      FileUtils.mkdir_p(root)
      FileUtils.cp_r(File.join(TEMPLATE_ROOT, "."), root)
      {"kind" => "dora_runtime_profile_apply", "version" => 1, "mode" => "applied", "destination" => destination, "files" => files, "profile" => profile.fetch("id"), "completion_boundary" => "Apply creates only the neutral technical profile; it does not install a browser, run a server, or prove a consumer."}.freeze
    end
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
