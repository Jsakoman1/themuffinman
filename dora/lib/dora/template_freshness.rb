# frozen_string_literal: true

require "yaml"

module Dora
  class TemplateFreshness
    def self.check!(config_path, project_root:)
      config = YAML.load_file(config_path)
      fail!("template freshness config kind is invalid") unless config["kind"] == "dora_template_freshness" && config["version"].to_i == 1
      templates = Array(config["templates"])
      fail!("template freshness config has no templates") if templates.empty?
      templates.map { |entry| check_template(entry, project_root) }
    end

    def self.check_template(entry, root)
      path = safe_path!(entry.fetch("path"), root)
      missing = []
      retired = []
      unless File.file?(path)
        return {"path" => entry.fetch("path"), "fresh" => false, "missing_references" => ["template file"], "retired_references" => []}
      end
      content = File.read(path)
      Array(entry.fetch("required_references", [])).each do |reference|
        target = safe_path!(reference, root)
        missing << reference unless content.include?(reference) && File.file?(target)
      end
      Array(entry.fetch("retired_references", [])).each { |reference| retired << reference if content.include?(reference) }
      {"path" => entry.fetch("path"), "fresh" => missing.empty? && retired.empty?, "missing_references" => missing, "retired_references" => retired}
    end
    private_class_method :check_template

    def self.safe_path!(relative, root)
      fail!("template reference must be project-relative") unless relative.is_a?(String) && !relative.empty? && !relative.start_with?("/") && !relative.split("/").include?("..")
      File.join(root, relative)
    end
    private_class_method :safe_path!

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
