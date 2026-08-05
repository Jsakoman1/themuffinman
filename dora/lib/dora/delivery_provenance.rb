# frozen_string_literal: true

require "open3"
require "time"
require "yaml"

module Dora
  class DeliveryProvenance
    def self.record!(config_path, destination_path, project_root:)
      config = YAML.load_file(config_path)
      fail!("delivery provenance config kind is invalid") unless config["kind"] == "dora_delivery_provenance" && config["version"].to_i == 1
      command = config["validation_command"]
      fail!("delivery provenance validation command is missing") unless command.is_a?(String) && !command.empty?
      evidence_paths = Array(config["evidence_paths"])
      fail!("delivery provenance evidence paths are missing") if evidence_paths.empty?
      evidence_paths.each { |relative| fail!("delivery provenance evidence path is missing: #{relative}") unless File.file?(File.join(project_root, relative)) }
      revision, status = Open3.capture2("git", "rev-parse", "HEAD", chdir: project_root)
      fail!("cannot read project Git revision") unless status.success?
      record = {"kind" => "dora_delivery_provenance_record", "version" => 1, "recorded_at" => Time.now.utc.iso8601, "revision" => revision.strip, "validation_command" => command, "evidence_paths" => evidence_paths, "claims" => ["Records declared validation and evidence inputs only."]}
      File.write(File.join(project_root, destination_path), YAML.dump(record).sub(/\A---\n/, ""))
      record
    end

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
