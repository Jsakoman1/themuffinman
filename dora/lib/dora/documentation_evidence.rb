# frozen_string_literal: true

require "yaml"

module Dora
  class DocumentationEvidence
    def self.review!(config_path)
      config = YAML.load_file(config_path)
      raise ArgumentError, "documentation evidence config kind is invalid" unless config["kind"] == "dora_documentation_evidence" && config["version"].to_i == 1
      claims = Array(config["claims"])
      raise ArgumentError, "documentation evidence config has no claims" if claims.empty?
      claims.map do |claim|
        evidence = Array(claim.fetch("evidence"))
        missing = evidence.reject { |path| File.file?(path) && File.read(path).include?(claim.fetch("match")) }
        {"id" => claim.fetch("id"), "matched" => missing.empty?, "missing_evidence" => missing}
      end
    end
  end
end
