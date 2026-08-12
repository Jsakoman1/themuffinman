# frozen_string_literal: true

require "open3"
require "rbconfig"
require "yaml"

require_relative "idc_triage"

module Dora
  # Local-only adapter around the existing IDC renderer. The owner/Codex starts it
  # explicitly; this class neither discovers inputs nor receives Bridge authority.
  class IdcOwnerStartedRenderer
    def self.run!(triage_path:, request_path:, manifest_path:, dossier_path:, output_path:, root: File.expand_path("../..", __dir__))
      triage = load_yaml!(triage_path, "triage request")
      readback = IdcTriage.evaluate!(request: triage)
      fail!("IDC local render requires current explicit owner authorization") unless readback.fetch("outcome") == "IDC_OWNER_AUTHORIZED_LOCAL_RENDER"

      inputs = [request_path, manifest_path, dossier_path].map { |path| input_path!(path) }
      output = output_path!(output_path, inputs)
      entrypoint = File.join(File.expand_path(root), "idc/bin/idc")
      fail!("IDC renderer entrypoint is unavailable") unless File.file?(entrypoint) && !File.symlink?(entrypoint)

      _stdout, _stderr, status = Open3.capture3(RbConfig.ruby, entrypoint, "render", "--request", inputs.fetch(0), "--manifest", inputs.fetch(1), "--dossier", inputs.fetch(2), "--out", output)
      fail!("IDC advisory renderer failed") unless status.success?

      {"kind" => "dora_idc_local_render_result", "version" => 1, "status" => "rendered", "profile" => readback.fetch("profile"), "output" => File.basename(output), "completion_boundary" => "The local advisory dossier was rendered from explicit inputs only. It is not a Dora decision, plan, lifecycle record, evidence record, verified status, or promotion."}.freeze
    end

    def self.load_yaml!(path, label)
      absolute = input_path!(path)
      document = YAML.load_file(absolute)
      fail!("#{label} must be a YAML mapping") unless document.is_a?(Hash)

      document
    rescue Psych::Exception
      fail!("#{label} YAML is invalid")
    end
    private_class_method :load_yaml!

    def self.input_path!(path)
      fail!("IDC input path is invalid") unless path.is_a?(String) && !path.strip.empty? && File.file?(path) && !File.symlink?(path)

      File.realpath(path)
    rescue Errno::ENOENT
      fail!("IDC input path is invalid")
    end
    private_class_method :input_path!

    def self.output_path!(path, inputs)
      fail!("IDC output path is invalid") unless path.is_a?(String) && !path.strip.empty? && !File.directory?(path) && !File.symlink?(path)
      absolute = File.expand_path(path)
      parent = File.dirname(absolute)
      fail!("IDC output directory is unavailable") unless Dir.exist?(parent)
      resolved = File.join(File.realpath(parent), File.basename(absolute))
      fail!("IDC output path must differ from every input") if inputs.include?(resolved)

      resolved
    rescue Errno::ENOENT
      fail!("IDC output path is invalid")
    end
    private_class_method :output_path!

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
