# frozen_string_literal: true

require "yaml"

module Dora
  class ChangeImpact
    def self.assess!(config_path, paths)
      config = YAML.load_file(config_path)
      rules = validate_config!(config)
      validate_paths!(paths)
      path_impacts = paths.uniq.sort.map do |path|
        matching = rules.select { |rule| Array(rule["path_prefixes"]).any? { |prefix| path.start_with?(prefix) } }
        {"path" => path, "rules" => matching.map { |rule| rule.fetch("id") }, "validations" => matching.flat_map { |rule| rule.fetch("validations") }.uniq, "documentation" => matching.flat_map { |rule| rule.fetch("documentation") }.uniq, "runtime_evidence" => matching.flat_map { |rule| rule.fetch("runtime_evidence") }.uniq, "decisions" => matching.flat_map { |rule| rule.fetch("decisions") }.uniq}
      end
      matched = path_impacts.reject { |impact| impact.fetch("rules").empty? }
      {"kind" => "dora_change_impact", "version" => 1, "changed_paths" => paths.uniq.sort, "classifications" => matched.flat_map { |impact| impact.fetch("rules") }.uniq, "path_impacts" => path_impacts, "validations" => matched.flat_map { |impact| impact.fetch("validations") }.uniq, "documentation" => matched.flat_map { |impact| impact.fetch("documentation") }.uniq, "runtime_evidence" => matched.flat_map { |impact| impact.fetch("runtime_evidence") }.uniq, "decisions" => matched.flat_map { |impact| impact.fetch("decisions") }.uniq, "unmatched_paths" => path_impacts.select { |impact| impact.fetch("rules").empty? }.map { |impact| impact.fetch("path") }, "completion_boundary" => "Declared impact is planning guidance only and does not prove implementation, runtime acceptance, or release readiness."}.freeze
    rescue Psych::Exception => error
      fail!("change impact YAML is invalid: #{error.message}")
    end

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!

    def self.validate_config!(config)
      fail!("change impact config kind is invalid") unless config.is_a?(Hash) && config["kind"] == "dora_change_impact" && config["version"].to_i == 1
      rules = Array(config["rules"])
      fail!("change impact config has no rules") if rules.empty?
      rules.each { |rule| validate_rule!(rule) }
      rules
    end
    private_class_method :validate_config!

    def self.validate_rule!(rule)
      %w[id path_prefixes validations documentation runtime_evidence decisions].each do |field|
        value = rule[field]
        fail!("change impact rule is missing #{field}") unless field == "id" ? value.is_a?(String) && !value.empty? : value.is_a?(Array)
      end
    end
    private_class_method :validate_rule!

    def self.validate_paths!(paths)
      fail!("changed paths must be a non-empty list") unless paths.is_a?(Array) && !paths.empty? && paths.all? { |path| safe_relative_path?(path) }
    end
    private_class_method :validate_paths!

    def self.safe_relative_path?(path)
      path.is_a?(String) && !path.empty? && !path.start_with?("/") && !path.split("/").include?("..")
    end
    private_class_method :safe_relative_path?
  end
end
