# frozen_string_literal: true

require "yaml"
require_relative "change_check"

module Dora
  class ChangeImpact
    def self.assess!(config_path, paths)
      config = YAML.load_file(config_path)
      aggregate = ChangeCheck.check!(config_path, paths)
      rules = Array(config.fetch("rules"))
      path_impacts = paths.uniq.sort.map do |path|
        matching = rules.select { |rule| Array(rule["path_prefixes"]).any? { |prefix| path.start_with?(prefix) } }
        {"path" => path, "rules" => matching.map { |rule| rule.fetch("id") }, "validations" => matching.flat_map { |rule| rule.fetch("validations") }.uniq, "documentation" => matching.flat_map { |rule| rule.fetch("documentation") }.uniq, "runtime_evidence" => matching.flat_map { |rule| rule.fetch("runtime_evidence") }.uniq, "decisions" => matching.flat_map { |rule| rule.fetch("decisions") }.uniq}
      end
      {"kind" => "dora_change_impact", "version" => 1, "path_impacts" => path_impacts, "validations" => aggregate.fetch("validations"), "documentation" => aggregate.fetch("documentation"), "runtime_evidence" => aggregate.fetch("runtime_evidence"), "decisions" => aggregate.fetch("decisions"), "unmatched_paths" => aggregate.fetch("unmatched_paths"), "completion_boundary" => "Declared impact is planning guidance only and does not prove implementation, runtime acceptance, or release readiness."}.freeze
    rescue Psych::Exception => error
      fail!("change impact YAML is invalid: #{error.message}")
    end

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
