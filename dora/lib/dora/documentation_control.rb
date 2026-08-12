# frozen_string_literal: true

require "yaml"
module Dora
  class DocumentationControl
    def self.validate!(system_map:, documentation_evidence:)
      map = YAML.load_file(system_map); evidence = YAML.load_file(documentation_evidence)
      validate_system_map!(map); validate_evidence!(evidence); true
    rescue Psych::Exception => error
      raise ArgumentError, "documentation control YAML is invalid: #{error.message}"
    end
    def self.validate_system_map!(map)
      fail!("system map is invalid") unless map.is_a?(Hash) && map["kind"] == "dora_system_map" && map["version"].to_i == 1
      nodes = Array(map["nodes"]); ids = nodes.map { |node| node.is_a?(Hash) ? node["id"] : nil }
      fail!("system map node ids are invalid") if ids.empty? || !ids.all? { |id| identifier?(id) } || ids.uniq.length != ids.length
      Array(map["edges"]).each { |edge| fail!("system map edge endpoint is unknown") unless edge.is_a?(Hash) && ids.include?(edge["from"]) && ids.include?(edge["to"]) }
    end
    private_class_method :validate_system_map!
    def self.validate_evidence!(evidence)
      fail!("documentation evidence is invalid") unless evidence.is_a?(Hash) && evidence["kind"] == "dora_documentation_evidence" && evidence["version"].to_i == 1
      claims = Array(evidence["claims"]); ids = claims.map { |claim| fail!("documentation evidence claim is invalid") unless claim.is_a?(Hash) && identifier?(claim["id"]) && statement?(claim["match"]) && claim["evidence"].is_a?(Array) && !claim["evidence"].empty?; claim["id"] }
      fail!("documentation evidence claim ids are invalid") if ids.empty? || ids.uniq.length != ids.length
    end
    private_class_method :validate_evidence!
    def self.identifier?(value); value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]{0,63}\z/); end
    def self.statement?(value); value.is_a?(String) && !value.strip.empty?; end
    private_class_method :identifier?, :statement?
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
